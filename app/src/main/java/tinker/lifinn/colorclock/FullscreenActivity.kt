package tinker.lifinn.colorclock

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import tinker.lifinn.colorclock.databinding.ActivityFullscreenBinding


import java.text.SimpleDateFormat
import java.util.*


import android.os.PowerManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

import tinker.lifinn.colorclock.Settings

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler(Looper.myLooper()!!)

    private val currentColor:Int=0xFFFFFFFF.toInt()
    private var colorNext:Int=0xFFFFFFFF.toInt()

    private lateinit var wakeLock: PowerManager.WakeLock

/*    val colorTimes= listOf(
        mapOf("hour" to 6, "minute" to 30, "bgcolor" to 0xFFDECC07.toInt(), "fgcolor" to 0xFF000000.toInt()),
        mapOf("hour" to 18, "minute" to 30, "bgcolor" to 0xFF100699.toInt(), "fgcolor" to 0xFF000000.toInt()),
    )

    val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    editor.putBoolean("key_name", true)
    editor.apply()*/

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }


    private lateinit var currentTimeTextView: TextView
    private val timeHandler = Handler(Looper.getMainLooper())

    private lateinit var transitView: View
    private lateinit var bgView: LinearLayout




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        currentTimeTextView = findViewById(R.id.fullscreen_content)

        transitView = findViewById(R.id.transitRect)
        bgView = findViewById(R.id.backgroundView)



        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = binding.fullscreenContent
        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.mainSettingsFab.setOnTouchListener(delayHideTouchListener)

        val myButton = findViewById<FloatingActionButton>(R.id.main_settings_fab)
        myButton.setOnClickListener(null)
        myButton.setOnClickListener {
            val intent = Intent(applicationContext, Settings::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()

        //load font and size settings
        val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
        val fontSize = sharedPref.getInt("fontSize", 7)
        val fontName = sharedPref.getString("font", "Roboto")
        val edgeLighting=sharedPref.getBoolean("edgeLighting", false)
        //set visibility of blackLayer to edgeLighting
        val blackLayer=findViewById<View>(R.id.blackLayer)
        if(edgeLighting){
            blackLayer.visibility=View.VISIBLE}
        else{
            blackLayer.visibility=View.INVISIBLE
        }
        currentTimeTextView.width = fontSize.toInt()*50
        currentTimeTextView.typeface = Typeface.createFromAsset(assets, "fonts/${fontName!!.lowercase()}.ttf")

        // Acquire a wake lock to prevent the display from turning off
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MyApp::MyWakeLockTag"
        )
        wakeLock.acquire()

        // Schedule a task to update the TextView every full minute
        timeHandler.post(object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance()
                val currentTime = SimpleDateFormat("HH:mm").format(calendar.time)
                val currentHour=SimpleDateFormat("HH").format(calendar.time).toInt()
                val currentMinute=SimpleDateFormat("mm").format(calendar.time).toInt()
                val currentSecond=SimpleDateFormat("ss").format(calendar.time).toLong()


                val colorTimes = ColorTimeSettings.loadColorTimes(applicationContext)
                val currentTimeInt=currentHour*60+currentMinute
                //turn colorTimes into a list of ints
                val colorTimesInt=colorTimes.map{it["hour"]!!.toInt()*60+it["minute"]!!.toInt()}
                //sort by size
                var colorTimesSorted=colorTimesInt.sorted()
                //define fgColor and bgColor as the one of the last time in the list
                var fgColor=(colorTimes[colorTimesInt.indexOf(colorTimesSorted.last())]["fgcolor"]!!)
                var bgColor=(colorTimes[colorTimesInt.indexOf(colorTimesSorted.last())]["bgcolor"]!!)
                var nextBgColor=(colorTimes[0]["bgcolor"]!!)
                var nextTime=colorTimes[0]["hour"]!!.toInt()*60+colorTimes[0]["minute"]!!.toInt()
                //loop through list and check if current time is equal or larger than the time in the list
                for (i in colorTimesSorted){
                    if (currentTimeInt>=i){

                        //set fgColor and bgColor to the color of the time in the list
                        fgColor=(colorTimes[colorTimesInt.indexOf(i)]["fgcolor"]!!)
                        bgColor=(colorTimes[colorTimesInt.indexOf(i)]["bgcolor"]!!)
                        //check if i is the last element in the list
                        //if not, set nextBgColor to the color of the next time in the list
                        if (i!=colorTimesSorted.last()){
                            nextBgColor=(colorTimes[colorTimesInt.indexOf(colorTimesSorted[colorTimesSorted.indexOf(i)+1])]["bgcolor"]!!)
                            nextTime=colorTimes[colorTimesInt.indexOf(colorTimesSorted[colorTimesSorted.indexOf(i)+1])]["hour"]!!.toInt()*60+colorTimes[colorTimesInt.indexOf(colorTimesSorted[colorTimesSorted.indexOf(i)+1])]["minute"]!!.toInt()
                        }
                        else{
                            nextBgColor=(colorTimes[colorTimesInt.indexOf(colorTimesSorted[0])]["bgcolor"]!!)
                            nextTime=colorTimes[colorTimesInt.indexOf(colorTimesSorted[0])]["hour"]!!.toInt()*60+colorTimes[colorTimesInt.indexOf(colorTimesSorted[0])]["minute"]!!.toInt()
                        }
                    }
                }
                //load transition time
                val transitionTime=sharedPref.getInt("transitionTime", 0)
                //calculate transition progress
                var transitionProgress=0f
                if (nextTime<currentTimeInt){
                    nextTime+=24*60
                }
                if( transitionTime!=0)
                {
                    transitionProgress =
                        (((currentTimeInt - nextTime + transitionTime) / transitionTime.toFloat()) * 100).coerceAtLeast(0f).coerceAtMost(100f)
                }
                println("currentTimeInt: $currentTimeInt")
                println("nextTime: $nextTime")
                println("transitionTime: $transitionTime")
                println("transitionProgress: $transitionProgress")
                //set transition progress as weight of transitView
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, transitionProgress.toFloat()).also { transitView.layoutParams = it }

                //set the color of the text and background
                currentTimeTextView.setTextColor(fgColor)
                bgView.setBackgroundColor(bgColor)
                transitView.setBackgroundColor(nextBgColor)
                currentTimeTextView.text = "$currentTime"

                checkClockSize()

                // Schedule the task again at the next full minute
                timeHandler.postDelayed(this, (60-currentSecond)*1000) // schedule the task again at the next full minute
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkClockSize()
    }

    fun checkClockSize(){
        val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
        val fontSize = sharedPref.getInt("fontSize", 7).toInt()
        //get leftFill,rightFill and currentTimeTextView
        val leftFill=findViewById<TextView>(R.id.leftFill)
        val rightFill=findViewById<TextView>(R.id.rightFill)
        var margin=10-fontSize
        val layoutParams=leftFill.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight=margin.toFloat()
        leftFill.layoutParams=layoutParams
        rightFill.layoutParams=layoutParams
        val clockParams=currentTimeTextView.layoutParams as LinearLayout.LayoutParams
        clockParams.weight=fontSize.toFloat()
        currentTimeTextView.layoutParams=clockParams


        /*
        // Get the screen width
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels*0.7

        // Get the text width
        val paint = Paint()
        paint.textSize = fontSize
        paint.typeface = Typeface.createFromAsset(assets, "fonts/${fontName!!.lowercase()}.ttf")
        val textWidth = paint.measureText(currentTime)*2
        println("textWidth: $textWidth, screenWidth: $screenWidth, fontSize: $fontSize")

        // Adjust the font size if necessary
        if (textWidth > screenWidth) {
            val scaledTextSize = (fontSize * (screenWidth) / textWidth).toInt()
            println("scaledTextSize: $scaledTextSize")
            currentTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTextSize.toFloat())
        }*/
    }

    override fun onPause() {
        super.onPause()

        // Remove any scheduled tasks when the activity is paused
        timeHandler.removeCallbacksAndMessages(null)
        // Release the wake lock when the activity is destroyed
        wakeLock.release()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}