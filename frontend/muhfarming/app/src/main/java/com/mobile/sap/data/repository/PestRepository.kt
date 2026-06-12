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
                    pestName = "Downy Mildew",
                    pestDescription = "Yellow spots on top, grey mold under leaves. Affects cabbage and kale crops.",
                    season = "Rainy Season",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Black Rot",
                    pestDescription = "V-shaped yellow lesions on cabbage leaf edges. Bacterial disease caused by Xanthomonas campestris.",
                    season = "All Year",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Leaf Spot",
                    pestDescription = "Brown spots on amaranth and huckleberry. Caused by Cercospora and Alternaria. Worse in rainy season.",
                    season = "Rainy Season",
                    severity = "Medium"
                ),
                PestInfo(
                    pestName = "Damping-Off",
                    pestDescription = "Seedlings collapse in nursery beds. Caused by Pythium and Rhizoctonia fungi.",
                    season = "All Year",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Tuta Absoluta",
                    pestDescription = "Tomato leaf miner. Most severe in West and Littoral regions where tomatoes are intensively cultivated.",
                    season = "All Year",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Bacterial Wilt",
                    pestDescription = "Severe in West and Littoral regions. Causes wilting and death of tomato plants.",
                    season = "Warm Season",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Fall Armyworm",
                    pestDescription = "Found everywhere but most severe in Centre, Adamawa, and North regions affecting maize crops.",
                    season = "All Year",
                    severity = "High"
                ),
                PestInfo(
                    pestName = "Fungal Diseases (General)",
                    pestDescription = "Various fungal diseases dominate in the humid South regions due to high moisture.",
                    season = "Rainy Season",
                    severity = "Medium"
                ),
                PestInfo(
                    pestName = "Viral Diseases",
                    pestDescription = "Viruses dominate in drier regions. Spread by insects and affect various crops.",
                    season = "Dry Season",
                    severity = "Medium"
                ),
                PestInfo(
                    pestName = "Spider Mites",
                    pestDescription = "Tiny arachnids more prevalent in drier regions. Cause leaf discoloration and webbing.",
                    season = "Dry Season",
                    severity = "Medium"
                ),
                PestInfo(
                    pestName = "Aphids",
                    pestDescription = "Small sap-sucking insects that damage leaves and stems. Common across all regions.",
                    season = "All Year",
                    severity = "Low"
                ),
                PestInfo(
                    pestName = "Whiteflies",
                    pestDescription = "Transmit viral diseases and cause direct damage. More common in warmer regions.",
                    season = "Warm Season",
                    severity = "Medium"
                )
            )

            Result.success(mockPests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
