package com.mobile.sap.data.repository

import com.mobile.sap.data.model.PestInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PestRepository {

    suspend fun getPestInfo(): Result<List<PestInfo>> = withContext(Dispatchers.IO) {
        try {
            delay(500) // Simulate network call

            val mockPests = listOf(
                PestInfo(
                    pestName = "Downy Mildew (Peronospora parasitica)",
                    pestDescription = "Yellow spots on top, grey mold under leaves. Affects cabbage and kale.",
                    season = "Rainy Season",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Black Rot (Xanthomonas campestris)",
                    pestDescription = "V-shaped yellow lesions on cabbage leaf edges.",
                    season = "All Year",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Leaf Spot (Cercospora, Alternaria)",
                    pestDescription = "Brown spots on amaranth, huckleberry. Worse in rainy season.",
                    season = "Rainy Season",
                    severity = "Medium"
                ),
                PestInfo(
                    pestName = "Damping-Off (Pythium, Rhizoctonia)",
                    pestDescription = "Seedlings collapse in nursery beds.",
                    season = "All Year",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Regional Notes",
                    pestDescription = "Tuta absoluta and bacterial wilt are worst in West and Littoral where tomatoes are intensive. Fall armyworm is everywhere but most severe in Centre, Adamawa, North maize. Fungal diseases dominate in the humid South. Viruses and mites dominate in the drier North.",
                    season = "All Year",
                    severity = "Medium"
                )
            )

            Result.success(mockPests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
