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

        // منطق پادگانی: فقط بایومیز، بقیه مسدود!
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val urlString = request?.url.toString().lowercase()

                // شرط طلایی: آیا آدرس حاوی biomaze.ir یا maze است؟
                if (urlString.contains("biomaze.ir") || urlString.contains("maze")) {
                    return false // اجازه باز شدن بده
                }

                // اگر شرط بالا برقرار نبود (هر سایت دیگری مثل تلگرام، گوگل و...) مسدود می‌شود
                Toast.makeText(this@MainActivity, "Access Denied: External links are blocked.", Toast.LENGTH_SHORT).show()
                return true // جلوی باز شدن لینک را بگیر
            }
        }

        // سیستم دانلود با اعلان‌های انگلیسی
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            
            val cookies = CookieManager.getInstance().getCookie(url)
            request.addRequestHeader("cookie", cookies)
            request.addRequestHeader("User-Agent", userAgent)
            
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType)
            )

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(applicationContext, "Download started...", Toast.LENGTH_LONG).show()
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
