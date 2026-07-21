package ir.biomaze.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val urlString = request?.url.toString().lowercase()

                // ۱. مسدودسازی کامل تلگرام (حتی لینک‌های آیدی تلگرام)
                if (urlString.contains("telegram") || urlString.contains("t.me") || urlString.startsWith("tg://")) {
                    Toast.makeText(this@MainActivity, "دسترسی به تلگرام مسدود شده است!", Toast.LENGTH_SHORT).show()
                    return true
                }

                // ۲. باز کردن اختصاصی سایت biomaze.ir یا لینک دانلود دارای کلمه maze
                if (urlString.contains("biomaze.ir") || urlString.contains("maze")) {
                    return false
                }

                // ۳. مسدود کردن هر لینک یا سایت خارجی دیگر
                Toast.makeText(this@MainActivity, "خروج از محیط بایومیز مجاز نیست!", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        // قابلیت دانلود مستقیم در پوشه Downloads گوشی
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            
            val cookies = CookieManager.getInstance().getCookie(url)
            request.addRequestHeader("cookie", cookies)
            request.addRequestHeader("User-Agent", userAgent)
            
            request.setDescription("در حال دانلود فایل...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType)
            )

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(applicationContext, "دانلود آغاز شد...", Toast.LENGTH_LONG).show()
        }

        webView.loadUrl("https://biomaze.ir")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
