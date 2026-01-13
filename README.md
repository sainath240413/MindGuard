MindGuard: A Psychologically-Attuned Digital Wellness Framework
MindGuard is an advanced Android-based wellness application designed to curb smartphone addiction through Pattern Interrupts. Unlike traditional app blockers that use rigid restrictions, MindGuard utilizes system-level monitoring and "Nudges" to shift user cognition from impulsive (System 1) to rational (System 2) thinking.

This project is based on research published in the International Journal of Computer Application (IJCA).

🚀 Key Features
Real-Time Pattern Interruption: Utilizes a high-frequency monitoring engine to detect problematic usage triggers.
Non-Coercive "Nudges": Implements persistent UI overlays that force a 30-second cognitive pause before accessing high-distraction apps.
System-Level Integration: Uses Android Foreground Services and UsageStats Manager for seamless background monitoring.
High Performance: Optimized for a low resource footprint (~45MB RAM) and minimal battery impact (<2% per 24h).
Privacy-First: Operates 100% offline; no user data is ever transmitted to external servers.

🛠️ Technical Specifications:
Language: Kotlin / Java
Framework: Android SDK
Core APIs:
UsageStatsManager: For tracking application foreground events.
WindowManager (TYPE_APPLICATION_OVERLAY): For drawing intervention UI over other apps.
Foreground Service: To ensure persistent monitoring even when the app is minimized.

💻 Local Setup Instructions:

Prerequisites
To run this project locally, you need:
Android Studio (Ladybug or newer recommended).
JDK 17 or higher.
Android SDK (API Level 30+ recommended).

Required Extensions/Plugins:
Install these in Android Studio (File > Settings > Plugins):
Kotlin: To support the primary language of the project.
Android Drawable Importer: For managing UI assets.
ADB Idea: For quickly clearing app data and restarting services during testing.

Installation Steps

Clone the Repository:
Bash
git clone https://github.com/sainath240413/MindGuard.git
Open Project: Launch Android Studio and select Open, then navigate to the cloned folder.
Sync Gradle: Allow the IDE to download dependencies and sync the project files.
Device Setup: Connect a physical Android device or launch an Emulator.
Enable Permissions: Once installed, you must manually grant:
Usage Access: To allow the app to monitor which app is in the foreground.
Display Over Other Apps: To allow the "Nudge" overlays to function.

📊 Performance Benchmarks
Metric                  Value
Response Latency        530msRAM 
Usage                   ~45MB
Battery Drain           < 2% / 24 hours

📜 Publication:
This project is officially published in: International Journal of Computer Application (IJCA), Volume 15, No. 6, 2025. DOI: 10.26808/RS.2025.7ba47a