package com.ruqqienav

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.HitTestResult
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.izikode.izilib.veinview.VeinView
import com.izikode.izilib.veinview.VeinViewClient
import com.izikode.izilib.veinview.VeinViewInjector
import com.izikode.izilib.veinview.defaultClient
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val webview: VeinView by lazy { findViewById<VeinView>(R.id.webview) }
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout.setScrimColor(Color.TRANSPARENT)
        //the variable below - 'swipeRefreshLayout' is reported by debugger as redundant but seems to be required for SRL to initialise correctly
        var swipeRefreshLayout: SwipeRefreshLayout? = null
        swipeRefreshLayout = findViewById(R.id.swipeContainer)
        swipeRefreshLayout!!.setOnRefreshListener {
            webview.reload()
            swipeRefreshLayout.isRefreshing = false
        }
        webview.settings.apply {
            webview.settings
            webview.settings.userAgentString = "RuqqieNav"
            webview.settings.javaScriptEnabled = true
            webview.settings.displayZoomControls = false
            webview.settings.builtInZoomControls = true
            webview.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            webview.settings.allowFileAccess = true
            webview.settings.domStorageEnabled = true
            setWebContentsDebuggingEnabled(false)
            webview.settings.useWideViewPort = false
            webview.settings.setSupportZoom(true)
            //webview.settings.setSupportMultipleWindows(true)
            registerForContextMenu(webview)
            webview.setVeinViewClient(object : VeinViewClient() {
                override fun onReadyToInject(injector: VeinViewInjector, page: String) {

                    injector.injectCSS(R.raw.cssinject)

                }
            })

            webview.loadUrl("https://www.ruqqus.com")
        }

        webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                progressBar.progress = progress
                if (progress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
                webview.loadUrl(
                    "javascript:(function() { " +
                            "document.getElementById('mobile-bottom-navigation-bar').style.display='none'}" + ")()"
                )
            }
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack()
            return true
        }
            return super.onKeyDown(keyCode, event)
    }
    override fun onPause()
    {
        super.onPause()
        webview.onPause()
        webview.pauseTimers()
    }
        override fun onResume() {
        super.onResume()
        webview.onResume()
        webview.resumeTimers()
    }

//solution taken from https://stackoverflow.com/questions/48362239/how-to-download-an-image-from-webview-by-long-press
    override fun onCreateContextMenu(
        contextMenu: ContextMenu, view: View?,
        contextMenuInfo: ContextMenuInfo?)
        {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo)
        val webViewHitTestResult: HitTestResult = webview.hitTestResult
        if (webViewHitTestResult.type == HitTestResult.IMAGE_TYPE ||
            webViewHitTestResult.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)

        {
            contextMenu.add(0, 1, 0, "save image")
                .setOnMenuItemClickListener {
                    val DownloadImageURL = webViewHitTestResult.extra
                    if (URLUtil.isValidUrl(DownloadImageURL)) {
                        val request =
                            DownloadManager.Request(Uri.parse(DownloadImageURL))
                        request.allowScanningByMediaScanner()
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        val downloadManager =
                            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                        downloadManager.enqueue(request)
                        Toast.makeText(
                            this@MainActivity,
                            "downloading",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed (unknown)", Toast.LENGTH_LONG)
                            .show()
                    }
                    false
                }
            }
        }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_homepage -> {
                webview.loadUrl("https://www.ruqqus.com")
            }
            R.id.nav_post -> {
                webview.loadUrl("https://ruqqus.com/submit")
            }
            R.id.nav_trending -> {
                webview.loadUrl("https://ruqqus.com/all?sort=hot")
            }
            R.id.nav_newest -> {
                webview.loadUrl("https://ruqqus.com/all?sort=new")
            }
            R.id.nav_commented -> {
                webview.loadUrl("https://ruqqus.com/all?sort=activity&t=week")
            }
            R.id.nav_discover -> {
                webview.loadUrl("https://ruqqus.com/browse")
            }
            R.id.nav_account -> {
                webview.loadUrl("https://ruqqus.com/settings/profile")
            }
            R.id.nav_devstream -> {
                webview.loadUrl("https://www.twitch.tv/captainmeta4")
            }
            R.id.nav_github -> {
                webview.loadUrl("https://github.com/ruqqus/ruqqus")
            }
            R.id.nav_discord -> {
                webview.loadUrl("https://ruqqus.com/discord")
            }
            R.id.nav_help -> {
                webview.loadUrl("https://ruqqus.com/help")
            }
            R.id.nav_github2 -> {
                webview.loadUrl("https://github.com/ruqqusshill/RuqqieNav")
            }
            R.id.nav_yourguilds -> {
                webview.loadUrl("https://ruqqus.com/mine")
            }
            R.id.nav_mod -> {
                webview.loadUrl("https://ruqqus.com/mod/queue")
            }
            R.id.nav_disputed -> {
                webview.loadUrl("https://ruqqus.com/?sort=disputed&t=day")
            }
            R.id.nav_rand -> {
                webview.loadUrl("https://ruqqus.com/random/guild")
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}


