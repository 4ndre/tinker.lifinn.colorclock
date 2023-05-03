package tinker.lifinn.colorclock

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

class FontPickerDialog(
    context: Context,
    private val fontList: List<String>,
    private val listener: (String) -> Unit
) : Dialog(context) {

    private lateinit var fontListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.font_picker_dialog)

        // Set up the title and font list
        val titleView = findViewById<TextView>(R.id.font_picker_title)
        fontListView = findViewById(R.id.font_picker_list)
        val adapter = FontListAdapter(context, fontList)
        fontListView.adapter = adapter

        fontListView.setOnItemClickListener { _, _, position, _ ->
            listener(fontList[position])
            dismiss()
        }

    }
}
