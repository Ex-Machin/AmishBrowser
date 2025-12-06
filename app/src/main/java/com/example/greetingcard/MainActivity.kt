package com.example.greetingcard

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.theme.GreetingCardTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val whitelist = listOf(
            "https://imageprawojazdy.pl/",
            "https://www.e-prawojazdy.eu/",
            "https://randomnerdtutorials.com/",
        )

        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    BrowserScreen(
                        allowedDomains = whitelist,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(allowedDomains: List<String>, modifier: Modifier = Modifier) {

    var webView: WebView? by remember { mutableStateOf(null) }

    // выбранный сайт из списка
    var selectedUrl by remember { mutableStateOf(allowedDomains.first()) }

    // состояние меню
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {

        // ---------- Навигационное меню ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // Выбор URL из whitelist
            Box {
                Button(onClick = { expanded = true }) {
                    Text("🔗 Сайты")
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    allowedDomains.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedUrl = option
                                webView?.loadUrl(option)
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

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {

                            val url = request?.url.toString()

                            return if (allowedDomains.any { url.contains(it) }) {
                                false
                            } else {
                                Toast.makeText(context, "🚫 Доступ запрещён", Toast.LENGTH_SHORT).show()
                                true
                            }
                        }
                    }

                    loadUrl(selectedUrl)
                    webView = this
                }
            }
        )
    }
}
