package com.example.attentionspanregulator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.attentionspanregulator.BreathingActivity
import com.example.attentionspanregulator.R
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AppCheckerService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var windowManager: WindowManager
    private var overlayView: android.view.View? = null
    private lateinit var appLimits: Map<String, Int>
    private var countDownTimer: CountDownTimer? = null
    private lateinit var homePackageName: String
    private lateinit var sharedPreferences: SharedPreferences

    private var currentForegroundApp: String? = null
    private var lastEventCheckTime: Long = 0
    private var indulgedAppPackage: String? = null

    private val quotes = listOf(
        "Don't stop when you're tired. Stop when you're done.",
        "The pain you feel today is the strength you feel tomorrow.",
        "Embrace the suck.",
        "Your mind is your most powerful weapon. It can be your best friend or your worst enemy.",
        "The cost of regret is always greater than the price of discipline.",
        "Suffer now and live the rest of your life as a champion.",
        "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
        "The secret of your future is hidden in your daily routine.",
        "Don't get lost in the short-term comfort.",
        "To get to the other side, you have to go through it.",
        "Are you choosing what's easy, or what's necessary?",
        "Every minute you waste is a minute you can't get back. Make it count.",
        "Discipline is the bridge between goals and accomplishment.",
        "Your future is defined by what you do today, not tomorrow.",
        "Motivation gets you going, but discipline keeps you growing.",
        "The successful warrior is the average person, with laser-like focus.",
        "Small disciplines repeated with consistency every day lead to great achievements.",
        "We don't rise to the level of our expectations; we fall to the level of our training.",
        "Do what you have to do, so you can do what you want to do.",
        "The best time to plant a tree was several years ago. The second best time is now.",
        "A little progress each day adds up to big results.",
        "Don't be afraid to be uncommon amongst the uncommon.",
        "You are in danger of living a life so comfortable and soft, you will die without ever realizing your true potential.",
        "The cost of regret is always greater than the price of discipline.",
        "Are you choosing what is easy, or what is necessary?",
        "Don't get lost in short-term comfort.",
        "Your mind is your most powerful weapon; train it well.",
        "Stop negotiating with your feelings. Just do the work.",
        "To get to the other side, you have to go through it.",
        "Suffer now and live the rest of your life as a champion.",
        "Embrace the suck."
    )

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        handler = Handler(Looper.getMainLooper())
        
        sharedPreferences = getSharedPreferences("app_limits", Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        loadAppLimits()

        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        homePackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))?.activityInfo?.packageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
        } ?: ""
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "time_limits_map") {
            loadAppLimits()
        }
    }

    private fun loadAppLimits() {
        val jsonString = sharedPreferences.getString("time_limits_map", "{}") ?: "{}"
        val jsonObject = JSONObject(jsonString)
        val limits = mutableMapOf<String, Int>()
        for (key in jsonObject.keys()) {
            limits[key] = jsonObject.getInt(key)
        }
        appLimits = limits
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        val now = System.currentTimeMillis()
        findForegroundApp(now - 1000 * 60, now) // Check last minute for initial state
        lastEventCheckTime = now

        runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                findForegroundApp(lastEventCheckTime, currentTime)
                lastEventCheckTime = currentTime
                
                if (shouldShowOverlay(currentForegroundApp)) {
                    showOverlay()
                } else {
                    hideOverlay()
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)

        return START_STICKY
    }

    private fun shouldShowOverlay(foregroundApp: String?): Boolean {
        if (foregroundApp == null || foregroundApp == packageName || foregroundApp == homePackageName) {
            return false
        }

        if (foregroundApp != indulgedAppPackage) {
            indulgedAppPackage = null
        }

        if (foregroundApp == indulgedAppPackage) {
            return false
        }

        val timeLimitMinutes = appLimits[foregroundApp]
        if (timeLimitMinutes != null) {
            val usage = getAppUsage(foregroundApp)
            val timeLimitMillis = timeLimitMinutes * 60 * 1000L
            return usage > timeLimitMillis
        }

        return false
    }

    private fun getAppUsage(packageName: String): Long {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        return usageStats.filter { it.packageName == packageName }.sumOf { it.totalTimeInForeground }
    }

    private fun startForegroundService() {
        val channelId = "app_checker_channel"
        val channelName = "App Checker Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Mode Active")
            .setContentText("Monitoring distracting apps.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun findForegroundApp(startTime: Long, endTime: Long) {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val eventType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                UsageEvents.Event.ACTIVITY_RESUMED
            } else {
                @Suppress("DEPRECATION")
                UsageEvents.Event.MOVE_TO_FOREGROUND
            }
            if (event.eventType == eventType) {
                currentForegroundApp = event.packageName
            }
        }
    }

    private fun showOverlay() {
        if (overlayView == null) {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            overlayView = layoutInflater.inflate(R.layout.overlay_layout, null)

            val quoteTextView = overlayView?.findViewById<TextView>(R.id.quote_subtitle)
            quoteTextView?.text = quotes[Random.nextInt(quotes.size)]

            val goBackButton = overlayView?.findViewById<Button>(R.id.go_back_button)
            val breatheButton = overlayView?.findViewById<Button>(R.id.breathe_button)
            val indulgeButton = overlayView?.findViewById<Button>(R.id.indulge_button)

            goBackButton?.setOnClickListener {
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(homeIntent)
                hideOverlay()
            }

            breatheButton?.setOnClickListener {
                val intent = Intent(this, BreathingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                hideOverlay()
            }

            indulgeButton?.isEnabled = false

            countDownTimer = object : CountDownTimer(TimeUnit.SECONDS.toMillis(30), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                    indulgeButton?.text = "Indulge anyway (${secondsRemaining}s)"
                }

                override fun onFinish() {
                    indulgeButton?.isEnabled = true
                    indulgeButton?.text = "Indulge anyway"
                }
            }.start()

            indulgeButton?.setOnClickListener {
                indulgedAppPackage = currentForegroundApp
                hideOverlay()
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                dimAmount = 0.8f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    blurBehindRadius = 40
                }
            }

            windowManager.addView(overlayView, params)
        }
    }

    private fun hideOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
            countDownTimer?.cancel()
            countDownTimer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        hideOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
