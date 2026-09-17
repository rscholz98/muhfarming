package com.mobile.sap.data.repository

import com.google.gson.Gson
import com.mobile.sap.data.api.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Integration tests for [FieldRepository] driving the *real* Retrofit
 * [ApiService] (so request paths, JSON bodies and response parsing are actually
 * exercised) against a local [MockWebServer]. No live backend needed.
 */
class FieldRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: FieldRepository
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repository = FieldRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getFields merges fields with coordinates into ordered polygons`() = runTest {
        server.enqueueJson(
            """[{"ID":4,"name":"Coffee","fieldNotes":"shade","farmId":4,"regionId":1,
                 "region":{"ID":1,"name":"Centre","geoCode":"CE"}}]"""
        )
        server.enqueueJson(
            """[{"ID":3,"latitude":3.88,"longitude":11.51,"sequenceOrder":1,"fieldId":4},
                {"ID":2,"latitude":3.87,"longitude":11.51,"sequenceOrder":0,"fieldId":4}]"""
        )

        val result = repository.getFields()

        assertTrue(result.isSuccess)
        val fields = result.getOrThrow()
        assertEquals(1, fields.size)
        val f = fields.first()
        assertEquals("4", f.id)
        assertEquals("Centre", f.region)
        assertEquals("Coffee", f.cultivation?.cropType)
        // Ordered by sequenceOrder despite arriving out of order.
        assertEquals(3.87, f.coordinates[0].latitude, 0.0)
        assertEquals(3.88, f.coordinates[1].latitude, 0.0)

        assertEquals("GET", server.takeRequest().method)
        assertEquals("GET", server.takeRequest().method)
    }

    @Test
    fun `getFields returns failure on a server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.getFields()

        assertTrue(result.isFailure)
    }

    @Test
    fun `createField reuses an existing farm and posts field then coordinates`() = runTest {
        // ensureFarm -> GET /farms returns one farm (no POST /farms expected).
        server.enqueueJson("""[{"ID":9,"name":"My Farm","userId":6}]""")
        // POST /fields -> created field with an ID.
        server.enqueueJson("""{"ID":11,"name":"Maize","fieldNotes":"note","farmId":9,"regionId":2}""")
        // Three POST /field-coordinates.
        repeat(3) { server.enqueueJson("""{"ID":${it + 1}}""", code = 201) }

        val field = uiField(
            region = "2", crop = "Maize", guideline = "note",
            coordinates = listOf(
                com.mobile.sap.data.model.Coordinate(1.0, 2.0),
                com.mobile.sap.data.model.Coordinate(3.0, 4.0),
                com.mobile.sap.data.model.Coordinate(5.0, 6.0),
            )
        )

        val result = repository.createField(field)
        assertTrue(result.isSuccess)

        val getFarms = server.takeRequest()
        assertEquals("GET", getFarms.method)
        assertEquals("/farms", getFarms.path)

        val postField = server.takeRequest()
        assertEquals("POST", postField.method)
        assertEquals("/fields", postField.path)
        val body = gson.fromJson(postField.body.readUtf8(), Map::class.java)
        assertEquals("Maize", body["name"])
        assertEquals("note", body["fieldNotes"])
        assertEquals(9.0, body["farmId"]) // JSON numbers decode to Double
        assertEquals(2.0, body["regionId"])

        // Three coordinate posts, each carrying the created field id and its order.
        val orders = (0..2).map {
            val req = server.takeRequest()
            assertEquals("/field-coordinates", req.path)
            val c = gson.fromJson(req.body.readUtf8(), Map::class.java)
            assertEquals(11.0, c["fieldId"])
            (c["sequenceOrder"] as Double).toInt()
        }
        assertEquals(listOf(0, 1, 2), orders)
    }

    @Test
    fun `createField creates a farm when none exist`() = runTest {
        server.enqueueJson("[]")                                             // GET /farms -> empty
        server.enqueueJson("""{"ID":5,"name":"My Farm","userId":6}""", 201)  // POST /farms
        server.enqueueJson("""{"ID":7,"name":"Coffee","farmId":5,"regionId":1}""") // POST /fields
        server.enqueueJson("""{"ID":1}""", 201)                              // 1 coordinate

        val result = repository.createField(
            uiField(coordinates = listOf(com.mobile.sap.data.model.Coordinate(0.0, 0.0)))
        )
        assertTrue(result.isSuccess)

        assertEquals("/farms", server.takeRequest().path)   // GET
        val postFarm = server.takeRequest()
        assertEquals("POST", postFarm.method)
        assertEquals("/farms", postFarm.path)
        assertEquals("/fields", server.takeRequest().path)  // POST field
    }

    @Test
    fun `updateField issues a PUT to the field id`() = runTest {
        server.enqueueJson("""[{"ID":9,"name":"My Farm","userId":6}]""")     // ensureFarm
        server.enqueueJson("""{"ID":4,"name":"Arabica","farmId":9,"regionId":1}""")

        val result = repository.updateField(uiField(crop = "Arabica").copy(id = "4"))
        assertTrue(result.isSuccess)

        server.takeRequest() // GET /farms
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/fields/4", put.path)
    }

    @Test
    fun `updateField fails fast on a non-numeric id without any network call`() = runTest {
        val result = repository.updateField(uiField().copy(id = "not-a-number"))
        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `deleteField issues a DELETE and treats 204 as success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = repository.deleteField("4")
        assertTrue(result.isSuccess)

        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
        assertEquals("/fields/4", req.path)
    }

    @Test
    fun `deleteField fails on a non-numeric id`() = runTest {
        val result = repository.deleteField("abc")
        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }

    // --- helpers ---

    private fun MockWebServer.enqueueJson(body: String, code: Int = 200) {
        enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private fun uiField(
        region: String = "1",
        crop: String = "Coffee",
        guideline: String? = null,
        coordinates: List<com.mobile.sap.data.model.Coordinate> = listOf(
            com.mobile.sap.data.model.Coordinate(0.0, 0.0),
            com.mobile.sap.data.model.Coordinate(1.0, 1.0),
            com.mobile.sap.data.model.Coordinate(2.0, 2.0),
        ),
    ) = com.mobile.sap.data.model.Field(
        id = "0",
        region = region,
        coordinates = coordinates,
        cultivation = com.mobile.sap.data.model.Cultivation(cropType = crop, season = "", status = ""),
        cultivationGuideline = guideline,
    )

    @Suppress("unused")
    private fun RecordedRequest.jsonBody(): Map<*, *> = gson.fromJson(body.readUtf8(), Map::class.java)
}
