package com.farazai.notesapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.farazai.notesapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    companion object {
        private const val TAG = "FarazAINotes"
        private const val APP_URL = "file:///android_asset/index.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make app truly full screen - extend behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set status bar and nav bar transparent
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Dark icons on status bar = false (we want light icons on dark app)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView
        setupWebView()

        // Restore or load fresh
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(APP_URL)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings

        // ── Core settings ──────────────────────────────────────────────────
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true          // localStorage support
        settings.databaseEnabled = true            // Web SQL (legacy support)
        settings.allowFileAccessFromFileURLs = true // asset:// → asset://
        settings.allowUniversalAccessFromFileURLs = true

        // ── Performance optimizations ──────────────────────────────────────
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

        // Hardware acceleration is set at app level (AndroidManifest),
        // but we also enable layer type here for smooth rendering
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // ── Display settings ───────────────────────────────────────────────
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.textZoom = 100                    // Prevent system font scaling
        settings.setSupportZoom(false)             // Disable pinch zoom (it's a native app)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // ── Media ──────────────────────────────────────────────────────────
        settings.mediaPlaybackRequiresUserGesture = false

        // ── Safe browsing & dark mode ──────────────────────────────────────
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false)
        }

        // ── Scrollbar ──────────────────────────────────────────────────────
        webView.isScrollbarFadingEnabled = true
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.overScrollMode = View.OVER_SCROLL_NEVER  // Remove bounce effect

        // ── Background color matches app ───────────────────────────────────
        webView.setBackgroundColor(Color.parseColor("#0A0A14"))

        // ── WebViewClient: handle navigation & errors ──────────────────────
        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                // Allow all requests (needed for Google Fonts CDN + Anthropic API)
                return super.shouldInterceptRequest(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.e(TAG, "WebView error [$errorCode]: $description at $failingUrl")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page loaded: $url")
                // Inject CSS to remove the phone-frame wrapper and make app truly fill screen
                injectNativeOverrides(view)
            }
        }

        // ── WebChromeClient: console logs & progress ───────────────────────
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    Log.d(TAG, "[JS Console] ${it.message()} (line ${it.lineNumber()})")
                }
                return true
            }
        }

        // ── JavaScript Bridge ──────────────────────────────────────────────
        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")
    }

    /**
     * Injects CSS overrides after page load to remove the phone-frame wrapper
     * (the .phone div) and make the app fill the entire Android screen natively.
     * Also adjusts font rendering for Android.
     */
    private fun injectNativeOverrides(view: WebView?) {
        val css = """
            /* Remove the simulated phone frame */
            body {
                padding: 0 !important;
                margin: 0 !important;
                background: #0a0a14 !important;
                display: block !important;
                align-items: unset !important;
                justify-content: unset !important;
            }
            
            /* Make the phone div fill the whole screen */
            .phone {
                width: 100vw !important;
                height: 100vh !important;
                height: 100dvh !important;
                border-radius: 0 !important;
                border: none !important;
                box-shadow: none !important;
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
            }
            
            /* Smooth scrolling everywhere */
            * {
                -webkit-overflow-scrolling: touch;
                scroll-behavior: smooth;
            }
            
            /* Better tap highlight */
            * {
                -webkit-tap-highlight-color: rgba(139, 92, 246, 0.15);
            }
            
            /* Prevent text selection on UI elements */
            .nav-btn, .toolbar-btn, .cat-card, .prompt-card, 
            .recent-card, .modal-btn, .modal-btn-ghost, .fab {
                -webkit-user-select: none;
                user-select: none;
            }
            
            /* Allow text selection in editor */
            .line-input, .prose-editor, .editor-title {
                -webkit-user-select: text;
                user-select: text;
            }
        """.trimIndent()

        // Encode CSS and inject via JS
        val encodedCSS = css.replace("\n", " ").replace("\"", "\\\"")
        val js = """
            (function() {
                var style = document.getElementById('android-native-overrides');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'android-native-overrides';
                    document.head.appendChild(style);
                }
                style.textContent = "$encodedCSS";
            })();
        """.trimIndent()

        view?.post {
            view.evaluateJavascript(js, null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onBackPressed() {
        // Let the WebApp handle back navigation (go back in history)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        // Proper WebView cleanup to prevent memory leaks
        webView.apply {
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }
}

/**
 * JavaScript ↔ Android bridge.
 * Methods annotated with @JavascriptInterface are callable from HTML/JS via:
 *   window.AndroidBridge.methodName(args)
 */
class AndroidBridge(private val context: Context) {

    @android.webkit.JavascriptInterface
    fun getDeviceInfo(): String {
        return """{"model":"${Build.MODEL}","sdk":${Build.VERSION.SDK_INT},"brand":"${Build.BRAND}"}"""
    }

    @android.webkit.JavascriptInterface
    fun showToast(message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    @android.webkit.JavascriptInterface
    fun isAndroid(): Boolean = true
}
