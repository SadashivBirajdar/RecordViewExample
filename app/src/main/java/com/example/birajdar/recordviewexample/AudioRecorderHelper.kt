package com.example.birajdar.recordviewexample

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import de.ityx.record_view.OnRecordListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AudioRecorderHelper(private val context: Context) : OnRecordListener {

    private var mRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null
    private lateinit var audioListener: AudioRecordListener

    private val outputFile: File
        get() {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)
            return File(
                Environment.getExternalStorageDirectory().absolutePath + "/Voice Recorder/RECORDING_" + dateFormat.format(
                    Date()
                ) + ".m4a"
            )
        }

    init {
        if (context is AudioRecordListener) {
            audioListener = context
        }
    }

    interface AudioRecordListener {
        fun setUri(uri: Uri)
    }

    override fun onStart() {
        Log.d("RecordView", "onStart")
        Toast.makeText(context, "Recording Started", Toast.LENGTH_SHORT).show()
        startRecording()
    }

    override fun onCancel() {
        Toast.makeText(context, "onCancel", Toast.LENGTH_SHORT).show()
        Log.d("RecordView", "Recording Canceled")
        stopRecording(false)
    }

    override fun onFinish(recordTime: Long) {
        val time = getHumanTimeText(recordTime)
        Log.d("RecordView", "onFinish")
        Log.d("RecordTime", time)

        stopRecording(true)
        val uri = Uri.parse("file://" + mOutputFile!!.absolutePath)
        Log.d("Audio Uri", uri.toString())
        audioListener.setUri(uri)
    }

    override fun onLessThanSecond() {
        Toast.makeText(context, "OnLessThanSecond", Toast.LENGTH_SHORT).show()
        Log.d("RecordView", "onLessThanSecond")
        stopRecording(false)
    }

    private fun getHumanTimeText(milliseconds: Long): String {
        return String.format(
            Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    milliseconds
                )
            )
        )
    }

    private fun startRecording() {
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            mRecorder!!.setAudioEncodingBitRate(48000)
        } else {
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mRecorder!!.setAudioEncodingBitRate(64000)
        }
        mRecorder!!.setAudioSamplingRate(16000)
        mOutputFile = outputFile
        mOutputFile!!.parentFile.mkdirs()
        mRecorder!!.setOutputFile(mOutputFile!!.absolutePath)

        try {
            mRecorder!!.prepare()
            mRecorder!!.start()
            Log.d("Voice Recorder", "started recording to " + mOutputFile!!.absolutePath)
        } catch (e: IOException) {
            Log.e("Voice Recorder", "prepare() failed " + e.message)
        }

    }

    private fun stopRecording(saveFile: Boolean) {
        try {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
            if (!saveFile && mOutputFile != null) {
                mOutputFile!!.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun createAudioPickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }
}
