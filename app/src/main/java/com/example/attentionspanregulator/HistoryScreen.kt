package com.example.attentionspanregulator

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class DailyUsage(val date: String, val totalUsageMillis: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    var dailyUsage by remember { mutableStateOf<List<DailyUsage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val usage = mutableListOf<DailyUsage>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endTime = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            val totalTime = stats.sumOf { it.totalTimeInForeground }
            usage.add(DailyUsage(dateFormat.format(Date(startTime)), totalTime))
        }
        dailyUsage = usage
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.dashboard_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("7-Day Usage History") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Black.copy(alpha = 0.6f)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Screen Time Last 7 Days", 
                            style = MaterialTheme.typography.headlineSmall, 
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        BarGraph(dailyUsage = dailyUsage)
                    }
                }
            }
        }
    }
}

@Composable
fun BarGraph(dailyUsage: List<DailyUsage>) {
    val maxUsage = dailyUsage.maxOfOrNull { it.totalUsageMillis } ?: 1L
    val primaryColor = MaterialTheme.colorScheme.primary // Read color from theme here

    Row(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        dailyUsage.forEach { usage ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDuration(usage.totalUsageMillis, true),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(modifier = Modifier.width(30.dp).heightIn(min = 10.dp).weight(1f)) {
                    val barHeight = (usage.totalUsageMillis.toFloat() / maxUsage.toFloat()) * size.height
                    drawLine(
                        color = primaryColor, // Use the variable here
                        start = Offset(x = center.x, y = size.height),
                        end = Offset(x = center.x, y = size.height - barHeight),
                        strokeWidth = 30f
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = usage.date, 
                    fontSize = 12.sp, 
                    color = Color.White
                )
            }
        }
    }
}

private fun formatDuration(millis: Long, compact: Boolean = false): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return when {
        compact && hours > 0 -> String.format("%dh", hours)
        compact -> String.format("%dm", minutes)
        hours > 0 -> String.format("%d h %02d m", hours, minutes)
        minutes > 0 -> String.format("%d m", minutes)
        else -> "< 1 m"
    }
}
