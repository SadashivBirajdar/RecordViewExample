package com.example.birajdar.recordviewexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web.*
import org.xwalk.core.XWalkPreferences

class WebActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true)

        val room = 5810826724490838L // generateRandomLong()
        val url = "https://i-webscout-dev.ityx.de/chat-test/video_m/video.html#$room"
        webView.loadUrl(url)

    }

    private fun generateRandomLong(): Long {
        val leftLimit = 1000000000000000
        val rightLimit = 9999999999999999
        return leftLimit + (Math.random() * (rightLimit - leftLimit)).toLong()
    }
}
