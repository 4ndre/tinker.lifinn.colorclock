package tinker.lifinn.colorclock

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import tinker.lifinn.colorclock.ColorTimeSettings
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class ColorTimeAdapter(val context: Context, val colorTimes: MutableList<MutableMap<String, Int>>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.colortime_setting_layout, parent, false)
        }

        // Get the current color time item from the list
        val colorTime = colorTimes[position]

        // Set the time text view
        val timeTextView = view?.findViewById<TextView>(R.id.timeTextView)
        timeTextView?.text = String.format("%02d:%02d", colorTime["hour"], colorTime["minute"])
        timeTextView?.setOnClickListener {
            val hour = colorTime["hour"] ?: 0
            val minute = colorTime["minute"] ?: 0
            val timePickerDialog = TimePickerDialog(view!!.context, { _, h, m ->
                colorTime["hour"] = h
                colorTime["minute"] = m
                timeTextView!!.text = String.format("%02d:%02d", h, m)
                ColorTimeSettings.saveColorTimes(context, colorTimes)
            }, hour, minute, true)
            timePickerDialog.show()
        }


        // Set the background color button
        val bgColorButton = view?.findViewById<Button>(R.id.bgColorButton)
        bgColorButton?.setBackgroundColor(colorTime["bgcolor"] as Int)
        bgColorButton?.setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(colorTime["bgcolor"] as Int)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("ok") { _, selectedColor, _ ->
                    bgColorButton.setBackgroundColor(selectedColor)
                    colorTime["bgcolor"] = selectedColor
                    ColorTimeSettings.saveColorTimes(context, colorTimes)
                }
                .setNegativeButton("cancel") { _, _ -> }
                .build()
                .show()
            //showColorPickerDialog(this.context, bgColorButton, true,colorTime["bgcolor"] as Int)
        }

        // Set the foreground color button
        val fgColorButton = view?.findViewById<Button>(R.id.fgColorButton)
        fgColorButton?.setBackgroundColor(colorTime["fgcolor"] as Int)
        fgColorButton?.setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(colorTime["fgcolor"] as Int)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("ok") { _, selectedColor, _ ->
                    fgColorButton.setBackgroundColor(selectedColor)
                    colorTime["fgcolor"] = selectedColor
                    ColorTimeSettings.saveColorTimes(context, colorTimes)
                }
                .setNegativeButton("cancel") { _, _ -> }
                .build()
                .show()
            //showColorPickerDialog(this.context, fgColorButton, false,colorTime["fgcolor"] as Int)
        }

        // Set the trash can icon
        val deleteButton = view?.findViewById<ImageView>(R.id.deleteColorTime)
        deleteButton?.setOnClickListener {
            colorTimes.removeAt(position)
            ColorTimeSettings.saveColorTimes(context, colorTimes)
            notifyDataSetChanged()
        }

        return view!!
    }

    override fun getItem(position: Int): Any {
        return colorTimes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return colorTimes.size
    }


private fun showColorPickerDialog(context: Context,colorView: Button,bgcolorField:Boolean,currentColor:Int) {
    val view = LayoutInflater.from(context).inflate(R.layout.color_picker_layout, null)
    val colorPicker = view.findViewById<ColorPickerView>(R.id.color_picker)
    val btnOk = view.findViewById<Button>(R.id.btn_ok)
    val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

    val dialog = AlertDialog.Builder(context)
        .setView(view)
        .create()

    // Set the initial color of the color picker
    //val currentColor = (colorView.background as ColorDrawable).color
    colorPicker.setColor(currentColor, true)

    // Handle the OK button click
    btnOk.setOnClickListener {
        val newColor = colorPicker.selectedColor
        colorView.setBackgroundColor(newColor)
        dialog.dismiss()
    }

    // Handle the Cancel button click
    btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}
}



