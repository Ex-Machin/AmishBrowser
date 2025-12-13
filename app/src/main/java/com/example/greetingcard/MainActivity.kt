package com.example.greetingcard

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.theme.GreetingCardTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val allowedUrls = listOf(
            "https://imageprawojazdy.pl/",
            "https://www.e-prawojazdy.eu/",
            "https://randomnerdtutorials.com/",
            "https://cerkiew-gdansk.pl/",
            "https://www.accuweather.com/",
            "https://it.pracuj.pl/",
            "https://www.play.pl/",
            "https://login.play.pl/",
            "https://24.play.pl/",
            "https://doladowania.play.pl/",
            "https://www.ifixit.com/",
            "https://aniagotuje.pl/",
            "https://www.russianfood.com/",
            "https://www.luxmed.pl/",
            "https://portalpacjenta.luxmed.pl/",
            "https://aquastacja.pl/",
            "https://regiojet.pl/",
            "https://azbyka.ru/",
        )

        // Список разрешённых доменов (host!)
        val allowedHosts = listOf(
            "imageprawojazdy.pl",
            "e-prawojazdy.eu",
            "randomnerdtutorials.com",
            "cerkiew-gdansk.pl",
            "accuweather.com",
            "pracuj.pl",
            "play.pl",
            "ifixit.com",
            "aniagotuje.pl",
            "russianfood.com",
            "luxmed.pl",
            "aquastacja.pl",
            "regiojet.pl",
            "azbyka.ru"
        )

        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    BrowserScreen(
                        allowedUrls = allowedUrls,
                        allowedHosts = allowedHosts,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    allowedUrls: List<String>,
    allowedHosts: List<String>,
    modifier: Modifier = Modifier
) {
    var webView: WebView? by remember { mutableStateOf(null) }

    // 🔥 состояние WebView
    val webViewState = rememberSaveable { Bundle() }

    var selectedUrl by rememberSaveable {
        mutableStateOf(allowedUrls.first())
    }

    var expanded by remember { mutableStateOf(false) }

    // 🔥 Домены рекламы (AdBlock)
    val adHosts = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "adsystem.com",
        "taboola.com",
        "outbrain.com",
        "facebookads",
        "ads.twitter.com"
    )

    Column(modifier = modifier.fillMaxSize()) {

        // ---------- Навигация ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Box {
                Button(onClick = { expanded = true }) {
                    Text("🔗 Сайты")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    allowedUrls.forEach { url ->
                        DropdownMenuItem(
                            text = { Text(url) },
                            onClick = {
                                selectedUrl = url
                                webView?.loadUrl(url)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(onClick = { webView?.goBack() }) { Text("◀") }
            Button(onClick = { webView?.goForward() }) { Text("▶") }
            Button(onClick = { webView?.reload() }) { Text("⟳") }
            Button(onClick = { webView?.loadUrl(selectedUrl) }) { Text("🏠") }
        }

        // ---------- WebView ----------
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                WebView(context).apply {

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.mixedContentMode =
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    settings.userAgentString =
                        settings.userAgentString.replace("wv", "")

                    webViewClient = object : WebViewClient() {

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            if (!request.isForMainFrame) return false

                            val host = request.url.host ?: return true
                            return if (allowedHosts.any { host.endsWith(it) }) {
                                false
                            } else {
                                Toast.makeText(
                                    context,
                                    "🚫 Доступ запрещён:\n$host",
                                    Toast.LENGTH_SHORT
                                ).show()
                                true
                            }
                        }

                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {

                            val host = request.url.host ?: return null

                            if (adHosts.any { host.contains(it) }) {
                                // 🚫 Блокируем рекламный запрос
                                return WebResourceResponse(
                                    "text/plain",
                                    "utf-8",
                                    null
                                )
                            }

                            return super.shouldInterceptRequest(view, request)
                        }

                    }



                    // 🔥 ВОССТАНОВЛЕНИЕ
                    if (webViewState.isEmpty) {
                        loadUrl(selectedUrl)
                    } else {
                        restoreState(webViewState)
                    }

                    webView = this
                }
            },
            update = { view ->
                webView = view
            },
            onRelease = { view ->
                // 🔥 СОХРАНЕНИЕ
                view.saveState(webViewState)
            }
        )
    }
}