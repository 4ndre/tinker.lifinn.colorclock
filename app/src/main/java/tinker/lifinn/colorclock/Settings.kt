package tinker.lifinn.colorclock

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.widget.NumberPicker
import android.widget.Switch

class Settings : AppCompatActivity() {

    private lateinit var colorTimesValue: TextView
    private lateinit var transitionTimeValue: TextView
    private lateinit var fontValue: TextView
    private lateinit var fontSizeValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val edgeLightingToggle = findViewById<Switch>(R.id.edgeLightingToggle)
        edgeLightingToggle.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("edgeLighting", isChecked)
            editor.apply()
        }


    }

    override fun onResume() {
        super.onResume()

        //load the settings from the shared preferences
        val colorTimes = ColorTimeSettings.loadColorTimes(applicationContext)
        //combine up to the first 3 color times into a string
        var colorTimesString = ""
        for (i in 0..2){
            if (i<colorTimes.size){
                colorTimesString += "${colorTimes[i]["hour"]}:${String.format("%02d", colorTimes[i]["minute"]!!.toInt())}, "
            }
        }
        //remove the last comma and space
        colorTimesString = colorTimesString.dropLast(2)
        //if there are more than 3 color times, add "..."
        if (colorTimes.size>3){
            colorTimesString += "..."
        }


        val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
        var font = sharedPref.getString("font", "Roboto")
        var fontSize = sharedPref.getInt("fontSize", 7)
        var transitionTime = sharedPref.getInt("transitionTime", 0)
        var edgeLighting= sharedPref.getBoolean("edgeLighting", false)

        //set the values of the text views to the settings values
        colorTimesValue = findViewById(R.id.colorTimesValue)
        colorTimesValue.text = colorTimesString
        fontValue = findViewById(R.id.fontValue)
        fontValue.text = font
        fontValue.typeface = Typeface.createFromAsset(assets, "fonts/${font!!.lowercase()}.ttf")
        transitionTimeValue = findViewById(R.id.transitionTimeValue)
        transitionTimeValue.text = transitionTime.toString()
        fontSizeValue = findViewById(R.id.fontSizeValue)
        fontSizeValue.text = fontSize.toString()
        val edgeLightingToggle = findViewById<Switch>(R.id.edgeLightingToggle)
        edgeLightingToggle.isChecked = edgeLighting
    }

    fun showColorTimeSettings(view: View) {
        val intent = Intent(this, ColorTimeSettings::class.java)
        startActivity(intent)
    }

    fun showTransitionTimePicker(view:View){
        val transitionTimeValue = findViewById<TextView>(R.id.transitionTimeValue)
        val numberPickerDialog = NumberPickerDialog("Select transition time in minutes", 0, 60,transitionTimeValue.text.toString().toInt()) { selectedValue ->
            transitionTimeValue.text = selectedValue.toString()
            val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putInt("transitionTime", selectedValue)
                apply()
            }
        }
        numberPickerDialog.show(supportFragmentManager, "NumberPickerDialog")




    }

    fun showFontDialog(view:View){
        val fontValue= findViewById<TextView>(R.id.fontValue)
        val fonts = listOf("Roboto", "Alkatra", "CabinSketch", "Monoton","Plaster","Poiretone","DeliciousHandrawn","ShadowsIntoLight","ShareTechMono","DigitalDream")
        val fontPickerDialog = FontPickerDialog(this, fonts) { font ->
            fontValue.text = font
            fontValue.typeface = Typeface.createFromAsset(assets, "fonts/${font.lowercase()}.ttf")
            val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("font", font)
                apply()
            }
            }
        fontPickerDialog.show()
    }
    fun showFontSizeDialog(view: View){
        val fontSizeValue = findViewById<TextView>(R.id.fontSizeValue)
        val numberPickerDialog = NumberPickerDialog("Select clock font size", 1, 10,fontSizeValue.text.toString().toInt()) { selectedValue ->
            fontSizeValue.text = selectedValue.toString()
            val sharedPref = getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putInt("fontSize", selectedValue)
                apply()
            }
        }
        numberPickerDialog.show(supportFragmentManager, "NumberPickerDialog")

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
