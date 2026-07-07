# FarazAI Notes — Android App

A beautiful poetry & notes writing app built with WebView + Kotlin, wrapped around your custom HTML/CSS/JS app.

---

## 📁 Project Structure

```
FarazAI_Notes/
├── build.gradle                          ← Project-level Gradle config
├── settings.gradle                       ← Project name & module setup
├── gradle.properties                     ← Gradle JVM & AndroidX flags
├── gradle/wrapper/
│   └── gradle-wrapper.properties         ← Gradle version (8.4)
│
└── app/
    ├── build.gradle                      ← App-level dependencies & SDK config
    ├── proguard-rules.pro                ← Release build optimization rules
    └── src/main/
        ├── AndroidManifest.xml           ← App config, permissions, activity
        ├── assets/
        │   └── index.html                ← ★ YOUR HTML APP (loaded locally)
        ├── java/com/farazai/notesapp/
        │   └── MainActivity.kt           ← WebView setup + Android bridge
        └── res/
            ├── layout/
            │   └── activity_main.xml     ← Full-screen WebView layout
            ├── drawable/
            │   ├── ic_launcher_background.xml
            │   ├── ic_launcher_foreground.xml
            │   └── splash_screen.xml
            ├── mipmap-*/
            │   └── ic_launcher*.png      ← App icons (all densities)
            ├── mipmap-anydpi-v26/
            │   ├── ic_launcher.xml       ← Adaptive icon
            │   └── ic_launcher_round.xml
            └── values/
                ├── strings.xml
                ├── colors.xml
                └── themes.xml
```

---

## 🛠️ Prerequisites — Install These First

### 1. Java Development Kit (JDK 17)
- Download from: https://adoptium.net/
- Install and make sure `java -version` works in terminal

### 2. Android Studio
- Download from: https://developer.android.com/studio
- Install with **all default options** (includes Android SDK, Emulator, etc.)
- First launch will download required SDK components — let it finish

---

## 🚀 How to Open in Android Studio

1. **Launch Android Studio**

2. Click **"Open"** (or File → Open if already inside a project)

3. Navigate to the **`FarazAI_Notes`** folder and click **OK**

4. Android Studio will **sync Gradle** automatically (bottom progress bar)
   - This downloads dependencies — takes 2–5 minutes on first open
   - Wait until you see **"Gradle sync finished"** in the status bar

5. If you see **"SDK not found"** warnings:
   - Go to **File → Project Structure → SDK Location**
   - Point it to your Android SDK folder (usually `~/Android/Sdk` on Mac/Linux or `C:\Users\YOU\AppData\Local\Android\Sdk` on Windows)

---

## ▶️ How to Run on an Emulator

1. Click **Device Manager** (icon in top-right toolbar, looks like a phone)

2. Click **"Create Device"**

3. Choose **Pixel 7** → click Next

4. Download **API 34 (Android 14)** system image → click Next → Finish

5. Click the **▶ Run** button (green triangle) in the toolbar

6. Select your emulator → the app will build and launch

---

## 📱 How to Build APK & Install on Real Phone

### Step 1 — Enable Developer Mode on your phone
1. Go to **Settings → About Phone**
2. Tap **Build Number** 7 times rapidly
3. You'll see "You are now a developer!"
4. Go back to Settings → **Developer Options**
5. Enable **USB Debugging**

### Step 2 — Connect phone to computer
- Use a USB cable
- Your phone will ask **"Allow USB Debugging?"** → tap **Allow**

### Step 3 — Run directly on phone
- In Android Studio, your phone will appear in the device dropdown (top toolbar)
- Select it and click **▶ Run**
- The app installs and launches on your phone automatically!

### Step 4 — Build a shareable APK file
1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to finish
3. Click **"locate"** in the notification that pops up
4. Your APK is at: `app/build/outputs/apk/debug/app-debug.apk`

### Step 5 — Install APK manually on phone
1. Copy the `.apk` file to your phone (via USB, WhatsApp, email, etc.)
2. On your phone: **Settings → Install Unknown Apps** → allow your file manager
3. Open the APK file → tap **Install**

---

## 🏗️ How to Build a Release APK (for sharing/Play Store)

### Create a Keystore (do this once)
1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Choose **APK** → Next
3. Click **"Create new..."** to create a keystore
4. Fill in the details — **save the keystore file and password somewhere safe!**
5. Choose **release** build variant → Finish

The signed APK will be at:
`app/build/outputs/apk/release/app-release.apk`

---

## ⚙️ App Features & Technical Details

| Feature | Implementation |
|---------|---------------|
| HTML app loading | Loaded from `assets/index.html` — **no internet needed for the app itself** |
| JavaScript | Fully enabled in WebView |
| Google Fonts | Loaded from CDN (requires internet on first load, then cached) |
| AI features | Calls Anthropic API via `fetch()` in JS (requires internet) |
| localStorage | Enabled via `domStorageEnabled = true` |
| Fullscreen | Extends behind status bar & nav bar via `WindowCompat` |
| Back button | Navigates WebView history before exiting app |
| Performance | Hardware acceleration + `LAYER_TYPE_HARDWARE` + `LOAD_CACHE_ELSE_NETWORK` |
| Android Bridge | `window.AndroidBridge.isAndroid()` callable from your JS |

---

## 🔧 Customizing the App

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Change Package Name
1. In `app/build.gradle`, change:
   ```
   applicationId "com.farazai.notesapp"
   ```
2. Right-click the `com.farazai.notesapp` folder in Android Studio
3. Choose Refactor → Rename

### Update the HTML App
Just replace `app/src/main/assets/index.html` with your new file and rebuild.

### Call Android from JavaScript
```javascript
// Check if running inside Android app
if (window.AndroidBridge && window.AndroidBridge.isAndroid()) {
    window.AndroidBridge.showToast("Hello from JS!");
}
```

---

## ❓ Troubleshooting

**"Gradle sync failed"**
→ Check your internet connection. Go to File → Invalidate Caches → Restart.

**"SDK not found"**
→ File → Project Structure → set Android SDK location.

**App shows blank white screen**
→ Make sure `index.html` is in `app/src/main/assets/` folder.

**Fonts not loading**
→ The app needs internet to load Google Fonts from CDN. On first run, connect to Wi-Fi.

**AI features not working**
→ Your HTML file needs a valid Anthropic API key set inside it. Check the `API_KEY` variable in `index.html`.

**Build fails with "minSdk" error**
→ The app requires Android 7.0+ (API 24). Very old phones won't be supported.

---

## 📋 Requirements

- Android Studio **Hedgehog (2023.1.1)** or newer
- JDK 17+
- Android SDK API **24** minimum (Android 7.0+)
- Target API **34** (Android 14)
- Gradle **8.4**
- Kotlin **1.9.22**

---

*Built with ❤️ using Kotlin + WebView*
