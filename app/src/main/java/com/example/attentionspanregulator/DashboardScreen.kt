package com.example.attentionspanregulator

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AppUsageInfo(
    val appName: String,
    val usageTime: Long, // in milliseconds
    val appIcon: Drawable,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var appUsageList by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var totalScreenTime by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        val aggregatedStats = mutableMapOf<String, Long>()
        for (stat in usageStats) {
            aggregatedStats[stat.packageName] = (aggregatedStats[stat.packageName] ?: 0) + stat.totalTimeInForeground
        }

        totalScreenTime = aggregatedStats.values.sum()

        val colors = listOf(
            Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
            Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
            Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39)
        ).shuffled()
        var colorIndex = 0

        appUsageList = aggregatedStats.mapNotNull { (packageName, usageTime) ->
            try {
                if (usageTime > 0) {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    AppUsageInfo(
                        appName = appInfo.loadLabel(packageManager).toString(),
                        usageTime = usageTime,
                        appIcon = appInfo.loadIcon(packageManager),
                        color = colors[colorIndex++ % colors.size]
                    )
                } else {
                    null
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.sortedByDescending { it.usageTime }

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
                    title = { Text("Usage Dashboard") },
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
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Screen Time Today", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            formatDuration(totalScreenTime),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    PieChartAndLegend(appUsageList, totalScreenTime)

                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier=Modifier.padding(vertical = 8.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(appUsageList) { appInfo ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberDrawablePainter(drawable = appInfo.appIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(appInfo.appName, modifier = Modifier.weight(1f), color = Color.White)
                                Text(formatDuration(appInfo.usageTime), fontSize = 16.sp, color = Color.White)
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartAndLegend(appUsageList: List<AppUsageInfo>, totalScreenTime: Long) {
    val appsForChart = appUsageList.take(5).toMutableList()
    val otherUsage = appUsageList.drop(5).sumOf { it.usageTime }
    if (otherUsage > 0) {
        appsForChart.add(AppUsageInfo("Other", otherUsage, appUsageList.first().appIcon, Color.Gray))
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(150.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) { 
                var startAngle = -90f
                appsForChart.forEach { app ->
                    val sweepAngle = (app.usageTime.toFloat() / totalScreenTime.toFloat()) * 360f
                    drawArc(
                        color = app.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        LazyColumn {
            items(appsForChart) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(it.color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it.appName, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return when {
        hours > 0 -> String.format("%dh %02dm", hours, minutes)
        minutes > 0 -> String.format("%dm", minutes)
        else -> "< 1m"
    }
}
