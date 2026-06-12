package com.mobile.sap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.sap.data.model.PestInfo
import com.mobile.sap.ui.theme.*
import com.mobile.sap.ui.viewmodel.PestUiState
import com.mobile.sap.ui.viewmodel.PestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestManagementScreen(
    viewModel: PestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pests",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FioriBlue,
                    titleContentColor = FioriWhite
                )
            )
        },
        containerColor = FioriLightGray
    ) { paddingValues ->
        when (val state = uiState) {
            is PestUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FioriBlue)
                }
            }
            is PestUiState.Success -> {
                PestListContent(
                    pests = state.pests,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is PestUiState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = { viewModel.loadPestInfo() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun PestListContent(
    pests: List<PestInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            InfoCard()
        }

        items(pests) { pest ->
            PestInfoCard(pest)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriInfo.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ℹ️",
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 14.dp)
            )
            Column {
                Text(
                    text = "Pest Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = FioriBlack,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Monitor by season and severity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FioriDarkGray.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PestInfoCard(pest: PestInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FioriWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pest.pestName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = FioriBlack,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.weight(1f)
                )
                SeverityBadge(severity = pest.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pest.pestDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = FioriDarkGray.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            SeasonTag(season = pest.season)
        }
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val (backgroundColor, textColor) = when (severity.lowercase()) {
        "high" -> SeverityHigh to FioriWhite
        "medium" -> SeverityMedium to FioriWhite
        "low" -> SeverityLow to FioriBlack
        else -> FioriGray to FioriBlack
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = severity,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun SeasonTag(season: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FioriBlue.copy(alpha = 0.08f)
    ) {
        Text(
            text = season,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = FioriBlue,
            fontSize = 12.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ErrorStateWithRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🐛",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Unable to Load Pest Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FioriBlack
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = FioriError
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = FioriBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Retry",
                    color = FioriWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
