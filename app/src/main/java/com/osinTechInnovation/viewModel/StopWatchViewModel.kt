package com.osinTechInnovation.viewModel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.osinTechInnovation.PartialResult
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException
import java.lang.Exception
import java.util.Timer
import java.util.TimerTask

class StopWatchViewModel: ViewModel(),RecognitionListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    lateinit  var stoper:Stoper
    lateinit var text:MutableState<String>
    lateinit var textEncirclement:MutableState<String>
    var flag = false


    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 729


    fun initStopWatch(
        applicationContext: Context,
        activity: Activity,
        listener: RecognitionListener,
        textToDisplayMain: MutableState<String>,
        textToDisplayEncirlement: MutableState<String>,
        flag:Boolean
    ){
        // Check if user has given permission to record audio, init the model after permission is granted

        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            stoper = Stoper(textToDisplayMain =textToDisplayMain , textToDisplayEncirlement = textToDisplayEncirlement)
            text = textToDisplayMain
            textEncirclement = textToDisplayEncirlement
            this.flag = flag
            initModel(applicationContext,activity,listener)
        }
    }

    private fun initModel(applicationContext:Context,activity: Activity,listener: RecognitionListener) {
        //model-en-us
        StorageService.unpack(applicationContext, "model-en-us", "model", { model: Model? ->
            this.model = model
            recognizeMicrophone(listener)
            text.value = "000"
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }) { exception: IOException -> setErrorState("Failed to unpack the model" + exception.message) }
    }

    private fun recognizeMicrophone(listener: RecognitionListener) {
        if (speechService != null) {
            //setUiState(org.vosk.demo.VoskActivity.STATE_DONE)
            speechService!!.stop()
            speechService = null
        } else {
            //setUiState(org.vosk.demo.VoskActivity.STATE_MIC)
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
                setErrorState(e.message!!)
            }
        }
    }

    private fun setErrorState(message: String) {/* resultView.setText(message)
         (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
         findViewById<View>(R.id.recognize_file).setEnabled(false)
         findViewById<View>(R.id.recognize_mic).setEnabled(false)*/
    }

    inner class Stoper(var textToDisplayMain: MutableState<String>,var textToDisplayEncirlement: MutableState<String>) {
        var milliseconds = 0L
        var millisecondsEncirlement = 0L

        private var timer: Timer? = null

        fun start() {

            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    milliseconds += 1
                    millisecondsEncirlement+=1

                    val hours = (milliseconds / (1000 * 60 * 60)) % 24
                    val minutes = (milliseconds / (1000 * 60)) % 60
                    val seconds = (milliseconds / 1000) % 60
                    val millis = milliseconds % 1000

                    val hoursEncirlement = (millisecondsEncirlement / (1000 * 60 * 60)) % 24
                    val minutesEncirlement = (millisecondsEncirlement / (1000 * 60)) % 60
                    val secondsEncirlement = (millisecondsEncirlement / 1000) % 60
                    val millisEncirlement = millisecondsEncirlement % 1000



                    if (milliseconds < 999) {
                        textToDisplayMain.value = String.format("%03d", millis)
                    } else if (milliseconds < 60000) {
                        textToDisplayMain.value = String.format("%02d:%03d", seconds, millis)
                    } else if (milliseconds < 3600000) {
                        textToDisplayMain.value = String.format("%02d:%02d:%03d", minutes, seconds, millis)
                    } else if (milliseconds > 3600000) {
                        textToDisplayMain.value =
                            String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis)
                    }

                    if (millisecondsEncirlement < 999) {
                        textToDisplayEncirlement.value = String.format("%03d", millisEncirlement)
                    } else if (millisecondsEncirlement < 60000) {
                        textToDisplayEncirlement.value = String.format("%02d:%03d", secondsEncirlement, millisEncirlement)
                    } else if (millisecondsEncirlement < 3600000) {
                        textToDisplayEncirlement.value = String.format("%02d:%02d:%03d", minutesEncirlement, secondsEncirlement, millisEncirlement)
                    } else if (millisecondsEncirlement > 3600000) {
                        textToDisplayEncirlement.value =
                            String.format("%02d:%02d:%02d:%03d", hoursEncirlement, minutesEncirlement, secondsEncirlement, millisEncirlement)
                    }


                }
            }, 0, 1)
        }

        fun stop() {
            timer?.cancel()
        }
    }

    override fun onPartialResult(hypothesis: String?) {
        val fromJson = Gson().fromJson(hypothesis!!, PartialResult::class.java)
        Log.i("FROM JSON", fromJson.partial!!)

        if (fromJson.partial.equals("stop") && flag) {
            stoper.stop()
            //stoperEncirlement.stop()
            flag = false
        } else if ((fromJson.partial.equals("start") || fromJson.partial.equals("go")) && !flag) {
            stoper.start()
            //stoperEncirlement.stop()
            flag = true
        } else if (fromJson.partial.equals("zero")) {
            stoper.stop()
            //stoperEncirlement.stop()
            //stoperEncirlement.milliseconds = 0L
            stoper.milliseconds = 0L
            stoper.millisecondsEncirlement = 0L
            text.value = "000"
            textEncirclement.value = "000"
            flag = false
        }
    }

    override fun onResult(hypothesis: String?) {
        TODO("Not yet implemented")
    }

    override fun onFinalResult(hypothesis: String?) {
        TODO("Not yet implemented")
    }

    override fun onError(exception: Exception?) {
        TODO("Not yet implemented")
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }

}