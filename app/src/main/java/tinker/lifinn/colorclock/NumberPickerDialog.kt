package tinker.lifinn.colorclock

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class NumberPickerDialog(
    private val title: String,
    private val minValue: Int,
    private val maxValue: Int,
    private val initValue: Int,
    private val onValueSelected: (Int) -> Unit
) : DialogFragment() {

    private lateinit var numberPicker: NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        // Inflate the layout for the dialog
        val inflater = requireActivity().layoutInflater
        val layout = inflater.inflate(R.layout.number_picker_dialog, null)

        // Set the dialog title
        layout.findViewById<TextView>(R.id.title).text = title

        // Set up the number picker
        numberPicker = layout.findViewById(R.id.numberPicker)
        numberPicker.minValue = minValue
        numberPicker.maxValue = maxValue
        numberPicker.value= initValue

        // Set up the Done button
        layout.findViewById<Button>(R.id.doneButton).setOnClickListener {
            // Call the callback with the selected value
            onValueSelected(numberPicker.value)
            // Dismiss the dialog
            dismiss()
        }

        builder.setView(layout)

        return builder.create()
    }
}
