package com.osinTechInnovation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.osinTechInnovation.smartstopwatch.R
import com.osinTechInnovation.viewModel.StopWatchViewModel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException
import java.util.Timer
import java.util.TimerTask


class MainActivity : ComponentActivity(), RecognitionListener {

    private val STATE_START = 0
    private val STATE_READY = 1

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private val speechStreamService: SpeechStreamService? = null
    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    var text = mutableStateOf("000")
    var textEncirclement = mutableStateOf("000")
    var timeStopWatch = mutableStateOf(0)
    var timeStopWatchEncirclement = mutableStateOf(0)
    val stoper = Stoper(text)
    val stoperEncirlement = Stoper(textEncirclement)
    var flag = false
    var micState =mutableStateOf(true)
    val itemList = mutableListOf<TimeEncirclement>()
    var count = mutableStateOf(1)

    private val stopWatchViewModel: StopWatchViewModel  by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileAds.initialize(this)
         /*   MainActivityLayout(this)
            BannerAd(
                modifier = Modifier.fillMaxSize(),
                adId = "ca-app-pub-3940256099942544/9214589741"
            )*/
            Layout(this)
        }


        text.value = "Czekaj..."


//        stopWatchViewModel.initStopWatch(this,this,this,text,textEncirclement,flag)



        //TODO 02.04.24- Wyłączone

        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            initModel()
        }


    }


    @Composable
    fun Layout(listener: RecognitionListener){
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            MainActivityLayout(listener = listener)
            BannerAd(
                modifier = Modifier.fillMaxSize(),
                adId = "ca-app-pub-3940256099942544/9214589741"
            )
        }

    }

    @Composable
    fun MainActivityLayout(listener: RecognitionListener) {

    /*    Box(
            modifier = Modifier.fillMaxSize()
        ) {*/
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                ) {
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = "${text.value}",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "${textEncirclement.value}", fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(20.dp))
                    OutlinedButton(onClick = {
                        if (!flag) {
                            //TODO 02.04.24 wyłączyłem
                            stoper.start()
//                            stopWatchViewModel.stoper.start()
                            //stoperEncirlement.start()
                            flag = true
                        }
                        else{
                            //TODO 02.04.24 wyłączyłem
                            stoper.stop()
                            //stopWatchViewModel.stoper.stop()
                            //stoperEncirlement.stop()
                            flag = false
                        }
                    }, border = BorderStroke(2.dp, if(flag) Color.Red else Color.Green ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(100.dp)
                            .weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    )
                    {
                        if (flag)
                        Text(
                            text = "STOP",
                            color = Color.Red,

                        )
                        else{
                            Text(
                                text = "START",
                                color = Color.Green

                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))
                    OutlinedButton(
                        onClick = {
                            if(flag){
                                itemList.add(TimeEncirclement(textEncirclement.value,count.value,text.value))
                                count.value++
                                stoper.millisecondsEncirlement=0L
                                //textEncirclement.value="000"
                            }
                            else if (!flag && stoper.milliseconds>0L ){
                                stoper.milliseconds=0L
                                stoper.millisecondsEncirlement=0L
                                text.value="000"
                                textEncirclement.value="000"
                                count.value=1
                                itemList.clear()
                            }

                    },
                        enabled = if (!flag && stoper.milliseconds<=0L) false else true ,
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                            .weight(1f),
                       border = BorderStroke(1.dp, if (!flag && stoper.milliseconds>0L) Color.Red else if (flag && stoper.milliseconds>0L) Color.Blue else Color.Unspecified),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color.LightGray,
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray
                        ),
                    ) {
                        if(flag)
                        Text(
                            text = "OKR",
                            color = Color.Blue
                        )
                        else if (!flag && stoper.milliseconds>0L )
                            Text(
                                text = "Reset",
                                color = Color.Red
                            )

                   /*     stoper.stop()

                        text.value = "000"
                        flag = false
                        stoper.milliseconds*/
                    }
                    Spacer(modifier = Modifier.width(20.dp))

                }
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier
                        .width(325.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "Pomiar")
                    Text(text = "Czas okrążenia")
                    Text(text = "Czas ogółem")
                }
                Spacer(modifier = Modifier.height(10.dp))

                Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.width(325.dp))


                LazyColumn(modifier = Modifier
                    .height(250.dp)
                    .padding(20.dp)
                    //.weight(1f)
                ) {
                   items(items = itemList){item->
                     Row(   modifier = Modifier
                         .width(325.dp),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.Top
                     ) {

                         Text(
                             text = "${item.id}",
                             fontSize = 15.sp,
                             //fontWeight = FontWeight.ExtraLight,
                             letterSpacing = 3.sp
                         )
                         Spacer(modifier = Modifier.width(20.dp))

                         Text(
                             text = "${item.encirclement}",
                             fontSize = 15.sp,
                             //fontWeight = FontWeight.ExtraLight,
                             letterSpacing = 3.sp
                         )
                         Spacer(modifier = Modifier.width(20.dp))

                         Text(
                             text = "${item.timeOverall}",
                             fontSize = 15.sp,
                             //fontWeight = FontWeight.ExtraLight,
                             letterSpacing = 3.sp
                         )

                     }
                   }
                }



            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Image(painter = painterResource(id = if(micState.value) R.drawable.mic else R.drawable.mic_off)
                    , contentDescription = "Microphone",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            micState.value = !micState.value
                            Log.i("MIC STATE", "$micState")
                            if (micState.value) speechService!!.startListening(listener) else speechService!!.stop()
                        }

                )
                Spacer(modifier = Modifier.size(50.dp))
            }
        //}
    }

    @Composable
    fun BannerAd(modifier: Modifier, adId: String) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(modifier = Modifier.size(24.dp))
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = adId
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                //stopWatchViewModel.initStopWatch(this,this,this,text,textEncirclement,flag)
                initModel()
            } else {
                finish()
            }
        }
    }

    private fun initModel() {
        //model-en-us
        StorageService.unpack(this, "model-en-us", "model", { model: Model? ->
            this.model = model
            recognizeMicrophone()
            text.value = "000"
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }) { exception: IOException -> setErrorState("Failed to unpack the model" + exception.message) }
    }

    private fun setErrorState(message: String) {/* resultView.setText(message)
         (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
         findViewById<View>(R.id.recognize_file).setEnabled(false)
         findViewById<View>(R.id.recognize_mic).setEnabled(false)*/
    }

    private fun recognizeMicrophone() {
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


    inner class Stoper(var textToDisplay: MutableState<String>) {
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
                        text.value = String.format("%03d", millis)
                    } else if (milliseconds < 60000) {
                        text.value = String.format("%02d:%03d", seconds, millis)
                    } else if (milliseconds < 3600000) {
                        text.value = String.format("%02d:%02d:%03d", minutes, seconds, millis)
                    } else if (milliseconds > 3600000) {
                        text.value =
                            String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis)
                    }

                    if (millisecondsEncirlement < 999) {
                        textEncirclement.value = String.format("%03d", millisEncirlement)
                    } else if (millisecondsEncirlement < 60000) {
                        textEncirclement.value = String.format("%02d:%03d", secondsEncirlement, millisEncirlement)
                    } else if (millisecondsEncirlement < 3600000) {
                        textEncirclement.value = String.format("%02d:%02d:%03d", minutesEncirlement, secondsEncirlement, millisEncirlement)
                    } else if (millisecondsEncirlement > 3600000) {
                        textEncirclement.value =
                            String.format("%02d:%02d:%02d:%03d", hoursEncirlement, minutesEncirlement, secondsEncirlement, millisEncirlement)
                    }


                }
            }, 0, 1)
        }

        fun stop() {
            timer?.cancel()
        }
    }

    override fun onResult(hypothesis: String?) {
        /* val fromJson = Gson().fromJson(hypothesis!!, ResultModel::class.java)
         Log.i("FROM JSON",fromJson.text)

         if (fromJson.text.equals("stop") && flag) {
             stoper.stop()
             flag = false
         } else if ((fromJson.text.equals("start") || fromJson.text.equals("go")) && !flag) {
             stoper.start()
             flag = true
         } else if (fromJson.text.equals("zero")) {
             stoper.stop()
             stoper.milliseconds = 0L
             text.value = "000"
             flag = false
         }*/
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
        } else if (fromJson.partial.equals("zero")||fromJson.partial.equals("reset")) {
            stoper.stop()
            //stoperEncirlement.stop()
            //stoperEncirlement.milliseconds = 0L
            stoper.milliseconds = 0L
            stoper.millisecondsEncirlement = 0L
            text.value = "000"
            textEncirclement.value = "000"
            itemList.clear()
            flag = false
        }
    /*    else if (fromJson.partial.equals("lap")){
            itemList.add(TimeEncirclement(textEncirclement.value,count.value,text.value))
            count.value++
            stoper.millisecondsEncirlement=0L
        }
*/
    }

    override fun onFinalResult(hypothesis: String?) {

    }

    override fun onError(exception: Exception?) {
        TODO("Not yet implemented")
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }
}

