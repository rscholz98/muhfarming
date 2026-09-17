package com.mobile.sap.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mobile.sap.data.api.ApiService
import com.mobile.sap.data.auth.SessionManager
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Integration tests for [AuthRepository] over the real Retrofit [ApiService]
 * against a local [MockWebServer]. Session persistence is backed by an
 * in-memory [SharedPreferences] fake so no Android runtime is required.
 */
class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var session: SessionManager
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        session = SessionManager.getForTest(FakeContext())
        session.clear()
        repository = AuthRepository(session, api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `login success stores token and role and returns the role`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"token":"jwt-123","role":"Farmer"}""")
        )

        val result = repository.login("alice", "secret123")

        assertTrue(result.isSuccess)
        assertEquals("Farmer", result.getOrThrow())
        assertEquals("jwt-123", session.token)
        assertEquals("Farmer", session.role)
        assertTrue(session.isLoggedIn)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/auth/login", req.path)
    }

    @Test
    fun `login trims the username before sending`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"token":"t","role":"Farmer"}"""))

        repository.login("  bob  ", "secret123")

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("\"username\":\"bob\""))
    }

    @Test
    fun `login wrong credentials returns a friendly 401 message and no session`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("unauthorized"))

        val result = repository.login("alice", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Invalid username or password", result.exceptionOrNull()?.message)
        assertNull(session.token)
        assertFalse(session.isLoggedIn)
    }

    @Test
    fun `signup 409 maps to a taken-username message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("conflict"))

        val result = repository.signup("taken", "secret123")

        assertTrue(result.isFailure)
        assertEquals("Username already taken", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signup 400 maps to a password-length message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("bad request"))

        val result = repository.signup("newuser", "123")

        assertTrue(result.isFailure)
        assertEquals("Password must be at least 6 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signup success posts to the signup path and stores the session`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"token":"jwt-9","role":"Farmer"}"""))

        val result = repository.signup("newuser", "secret123")

        assertTrue(result.isSuccess)
        assertEquals("/auth/signup", server.takeRequest().path)
        assertEquals("jwt-9", session.token)
    }

    /** Minimal in-memory [SharedPreferences] so [SessionManager] works off-device. */
    private class FakeContext : android.content.ContextWrapper(null) {
        private val prefs = FakePrefs()
        override fun getApplicationContext(): Context = this
        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = prefs
    }

    private class FakePrefs : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()

        override fun getString(key: String?, defValue: String?): String? =
            (map[key] as? String) ?: defValue

        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun getAll(): MutableMap<String, *> = map
        override fun getInt(key: String?, defValue: Int) = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long) = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float) = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean) = map[key] as? Boolean ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String>) ?: defValues

        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        override fun edit(): SharedPreferences.Editor = FakeEditor(map)

        private class FakeEditor(private val map: MutableMap<String, Any?>) : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) map[key] = value; return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) map[key] = values; return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
            override fun remove(key: String?): SharedPreferences.Editor { map.remove(key); return this }
            override fun clear(): SharedPreferences.Editor { map.clear(); return this }
            override fun commit(): Boolean = true
            override fun apply() {}
        }
    }
}
