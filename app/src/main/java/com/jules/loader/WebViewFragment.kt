package com.jules.loader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class WebViewFragment : Fragment() {

    interface OnWebStateListener {
        fun onUrlChanged(url: String, title: String?)
    }

    companion object {
        private const val ARG_URL = "url"
        fun newInstance(url: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    fun canGoBack(): Boolean {
        val webView = view?.findViewById<WebView>(R.id.webview)
        return webView?.canGoBack() == true
    }

    fun goBack() {
        val webView = view?.findViewById<WebView>(R.id.webview)
        webView?.goBack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARG_URL) ?: "https://jules.google.com"
        val webView = view.findViewById<WebView>(R.id.webview)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.domStorageEnabled = true
        // webView.settings.databaseEnabled = true // Deprecated in API level 18+

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                (activity as? OnWebStateListener)?.onUrlChanged(url ?: "", view?.title)
            }
        }
        webView.loadUrl(url)
    }
}
