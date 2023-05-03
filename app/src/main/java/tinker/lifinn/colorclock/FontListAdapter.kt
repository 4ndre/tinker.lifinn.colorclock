package tinker.lifinn.colorclock

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

class FontListAdapter(
    private val context: Context,
    private val fontList: List<String>
) : BaseAdapter() {

    override fun getCount(): Int {
        return fontList.size
    }

    override fun getItem(position: Int): Any {
        return fontList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.font_picker_list_item, parent, false)
        }
        val fontName = view!!.findViewById<TextView>(R.id.font_picker_list_item_name)
        val typeface = Typeface.createFromAsset(context.assets, "fonts/${fontList[position].lowercase()}.ttf")
        //ResourcesCompat.getFont(context,R.font.alkatra)
        fontName.typeface = typeface
        fontName.text = fontList[position]
        return view!!
    }
}
