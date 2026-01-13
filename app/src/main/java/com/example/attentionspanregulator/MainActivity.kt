package com.example.attentionspanregulator

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attentionspanregulator.service.AppCheckerService
import com.example.attentionspanregulator.ui.theme.AttentionSpanRegulatorTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AttentionSpanRegulatorTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(navController = navController)
                    }
                    composable("app_selection") {
                        AppSelectionScreen(navController = navController)
                    }
                    composable("dashboard") {
                        DashboardScreen(navController = navController)
                    }
                    composable("history") {
                        HistoryScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val buttonShape = RoundedCornerShape(12.dp)
    val transparentButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.White
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Black.copy(alpha = 0.85f),
            ) {
                Text("MindGuard Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("dashboard")
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(8.dp)),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = Color.White,
                        unselectedContainerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Usage History") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("history")
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(8.dp)),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = Color.White,
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.4f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = { 
                                scope.launch { 
                                    drawerState.open() 
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Black.copy(alpha = 0.6f)
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(id = R.drawable.mindguard_logo),
                        contentDescription = "MindGuard Logo",
                        modifier = Modifier.width(280.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = painterResource(id = R.drawable.homescreen_subtitle),
                        contentDescription = "MindGuard Subtitle",
                        modifier = Modifier.width(250.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    val buttonModifier = Modifier.fillMaxWidth().height(50.dp)
                    val buttonBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))

                    Button(
                        onClick = { navController.navigate("app_selection") },
                        shape = buttonShape,
                        colors = transparentButtonColors,
                        border = buttonBorder,
                        modifier = buttonModifier
                    ) {
                        Text("Set App Time Limits")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                        shape = buttonShape,
                        colors = transparentButtonColors,
                        border = buttonBorder,
                        modifier = buttonModifier
                    ) {
                        Text("Grant Usage Access Permission")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            context.startActivity(intent)
                        },
                        shape = buttonShape,
                        colors = transparentButtonColors,
                        border = buttonBorder,
                        modifier = buttonModifier
                    ) {
                        Text("Grant Overlay Permission")
                    }
                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            val serviceIntent = Intent(context, AppCheckerService::class.java)
                            context.startForegroundService(serviceIntent)

                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(homeIntent)
                        },
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = buttonModifier
                    ) {
                        Text("Start Focus Session")
                    }
                    
                    Spacer(modifier = Modifier.weight(1.5f))
                }
            }
        }
    }
}

// Helper to convert from a linear slider (0-1) to a non-linear minute value
private fun toMinutes(sliderValue: Float): Int {
    return (720 * sliderValue.pow(2)).roundToInt()
}

// Helper to convert from a minute value back to a non-linear slider position
private fun toSliderValue(minutes: Int): Float {
    return if (minutes == 0) 0f else (minutes / 720f).pow(0.5f)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }
    val launchableApps = installedApps.filter { packageManager.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName }

    val sharedPreferences = context.getSharedPreferences("app_limits", Context.MODE_PRIVATE)

    val appLimits = remember { mutableStateMapOf<String, Int>().apply {
        val jsonString = sharedPreferences.getString("time_limits_map", "{}") ?: "{}"
        val jsonObject = JSONObject(jsonString)
        for (key in jsonObject.keys()) {
            this[key] = jsonObject.getInt(key)
        }
    } }

    fun saveLimits() {
        sharedPreferences.edit(commit = true) { 
            val jsonObject = JSONObject()
            for ((key, value) in appLimits) {
                jsonObject.put(key, value)
            }
            putString("time_limits_map", jsonObject.toString())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Set App Time Limits") },
                    actions = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.Done, "Done", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Black.copy(alpha = 0.6f)
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(launchableApps) { app ->
                    var isEnabled by remember { mutableStateOf(appLimits.containsKey(app.packageName)) }
                    var currentTime by remember { mutableStateOf(appLimits[app.packageName] ?: 0) }

                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberDrawablePainter(drawable = app.loadIcon(packageManager)),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = app.loadLabel(packageManager).toString(),
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = {
                                    isEnabled = it
                                    if (isEnabled) {
                                        appLimits[app.packageName] = currentTime
                                    } else {
                                        appLimits.remove(app.packageName)
                                    }
                                    saveLimits()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (isEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            // Time Display and Manual Input
                            Row(verticalAlignment = Alignment.CenterVertically, modifier=Modifier.fillMaxWidth()) {
                                var isHours by remember { mutableStateOf(false) }
                                var dropdownExpanded by remember { mutableStateOf(false) }
                                var textValue by remember(currentTime, isHours) { mutableStateOf(if(isHours) (currentTime / 60).toString() else currentTime.toString()) }

                                OutlinedTextField(
                                    value = textValue,
                                    onValueChange = {
                                        textValue = it
                                        val rawValue = it.toIntOrNull() ?: 0
                                        currentTime = if(isHours) rawValue * 60 else rawValue
                                        appLimits[app.packageName] = currentTime
                                        saveLimits()
                                    },
                                    modifier = Modifier.width(100.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White,
                                        focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                        focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                // Dropdown for Min/Hr
                                ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = !dropdownExpanded }) {
                                    OutlinedTextField(
                                        value = if(isHours) "Hours" else "Minutes",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                        modifier = Modifier.menuAnchor().width(120.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                                    )
                                    ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                                        DropdownMenuItem(text = {Text("Minutes")}, onClick = { isHours = false; dropdownExpanded = false })
                                        DropdownMenuItem(text = {Text("Hours")}, onClick = { isHours = true; dropdownExpanded = false })
                                    }
                                }

                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            // Slider and Steppers
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { 
                                    if (currentTime > 0) {
                                        currentTime--
                                        appLimits[app.packageName] = currentTime
                                        saveLimits()
                                    }
                                }) {
                                    Icon(Icons.Filled.Remove, "Decrement", tint = Color.White)
                                }
                                Slider(
                                    value = toSliderValue(currentTime),
                                    onValueChange = { 
                                        currentTime = toMinutes(it)
                                        appLimits[app.packageName] = currentTime
                                        saveLimits()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { 
                                    currentTime++
                                    appLimits[app.packageName] = currentTime
                                    saveLimits()
                                }) {
                                    Icon(Icons.Filled.Add, "Increment", tint = Color.White)
                                }
                            }
                             Text(
                                text = if(currentTime == 0) "Block Immediately" else "${currentTime / 60}h ${currentTime % 60}m",
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}
