package tinker.lifinn.colorclock

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView

class ColorTimeSettings : AppCompatActivity() {

    companion object {
        fun saveColorTimes(context: Context, colorTimes: MutableList<MutableMap<String, Int>>) {
            //save colorTimes to SharedPreferences
            val sharedPref = context.getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("color_times", colorTimes.map { "${it["hour"]},${it["minute"]},${Integer.toHexString(
                    it["bgcolor"]!!
                )},${Integer.toHexString(it["fgcolor"]!!)}" }.joinToString(";"))
                commit()
            }
        }

        //load and return colorTimes from SharedPreferences
        fun loadColorTimes(context: Context): MutableList<MutableMap<String, Int>> {
            val sharedPref = context.getSharedPreferences("kcc_settings", Context.MODE_PRIVATE)
            var value = sharedPref.getString("color_times", "6,30,FFDECC07,FF000000;19,30,FF100699,FF000000")
            //check if value is ""
            if (value==""){
                //set value to default
                value="6,30,FFDECC07,FF000000"
            }
            //turn value into a list of maps
            //split value at ;
            //split each item at ,
            //first item is hour, second is minute, third is bgcolor, fourth is fgcolor
            var colorTimes = value!!.split(";")
                .map { it.split(",").map { it.trim() } }
                .map { mapOf("hour" to it[0].toInt(), "minute" to it[1].toInt(), "bgcolor" to (java.lang.Long.parseLong(it[2],16).toInt()), "fgcolor" to (java.lang.Long.parseLong(it[3],16).toInt())) }
                .toMutableList()
                .map { it.toMutableMap() }
                .toMutableList()
            return colorTimes
        }
    }

    private lateinit var listView: ListView
    private lateinit var addButton: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var colorTimes: MutableList<MutableMap<String, Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colortimesettings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        // get reference to ListView
        listView = findViewById(R.id.colorTimesList)

        var colorTimes = loadColorTimes(this)

        // create and set custom adapter
        val adapter = ColorTimeAdapter(this, colorTimes)
        listView.adapter = adapter

        addButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            colorTimes.add(mutableMapOf("hour" to 0, "minute" to 0, "bgcolor" to 0xFFDECC07.toInt(), "fgcolor" to 0xFF000000.toInt()))
            adapter.notifyDataSetChanged()
            saveColorTimes(this, colorTimes)
        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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