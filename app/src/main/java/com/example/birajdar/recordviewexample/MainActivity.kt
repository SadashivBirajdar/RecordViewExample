package com.example.birajdar.recordviewexample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), AudioPlayerHelper.AudioPlayerListener,
    AudioRecorderHelper.AudioRecordListener {

    companion object {
        const val AUDIO_CODE = 123
        const val RequestPermissionCode = 101
    }

    private lateinit var mAudioPlayerHelper: AudioPlayerHelper
    private lateinit var mAudioRecorderHelper: AudioRecorderHelper
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioRecorderHelper = AudioRecorderHelper(this)
        mAudioPlayerHelper = AudioPlayerHelper(this)

        if (!checkPermission()) {
            requestPermission()
        }

        // record audio UI
        //IMPORTANT
        record_button.setRecordView(record_view)
        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        record_view.cancelBounds = 8f
        record_view.setSmallMicColor(Color.parseColor("#c2185b"))
        //prevent recording under one Second
        record_view.setLessThanSecondAllowed(false)
        record_view.setSlideToCancelText("Slide To Cancel")
        record_view.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0)
        record_view.setOnRecordListener(mAudioRecorderHelper)

        // playing audio code
        playButton.setOnClickListener { mAudioPlayerHelper.playSong(uri) }
        seekbar.setOnSeekBarChangeListener(mAudioPlayerHelper)

        button.setOnClickListener {
            val intent = mAudioRecorderHelper.createAudioPickerIntent()
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(Intent.createChooser(intent, "Select audio file"), AUDIO_CODE)
            }
        }

       /* openWeb.setOnClickListener {
            startActivity(Intent(this, WebActivity::class.java))
        }*/
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET, Manifest.permission.CAMERA,
                Manifest.permission.MODIFY_AUDIO_SETTINGS),
            RequestPermissionCode
        )
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUDIO_CODE && resultCode == Activity.RESULT_OK) {
            val mediaUriList = ArrayList<Uri>()
            if (data != null && data.data != null) {
                mediaUriList.add(data.data)
            } else if (data!!.clipData != null) {
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    mediaUriList.add(clipData.getItemAt(i).uri)
                }
            }
            setUri(mediaUriList[0])
        }
    }

    override fun updateSeekBar(progress: Int) {
        seekbar.progress = progress
    }

    override fun setSeekBarMax(duration: Int) {
        seekbar.max = duration
    }

    override fun updatePlayButton(drawable: Drawable?) {
        playButton.setImageDrawable(drawable)
    }

    override fun updateAudioProgress(progress: Long) {
        tvFileLength.text = getAudioLength(progress)
    }

    override fun setUri(uri: Uri) {
        Toast.makeText(this, "Click Play Button", Toast.LENGTH_SHORT).show()
        this.uri = uri
        val mp = MediaPlayer.create(this, uri)
        val duration = mp.duration
        tvFileLength.text = getAudioLength(duration.toLong())
        mp.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioPlayerHelper.clearMediaPlayer()
    }

    private fun getAudioLength(milliseconds: Long): String {
        val seconds = (milliseconds % 60000) / 1000
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }
}
