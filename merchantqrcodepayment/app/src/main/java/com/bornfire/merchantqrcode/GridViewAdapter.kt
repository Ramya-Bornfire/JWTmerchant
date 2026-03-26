package com.bornfire.merchantqrcode
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bornfire.merchantqrcode.DataModel.gridmodel


internal class GridViewAdapter(
    private val courseList: List<gridmodel>,
    private val context: Context
) :
    BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var text: TextView
    private lateinit var img: ImageView

    // below method is use to return the count of course list
    override fun getCount(): Int {
        return courseList.size
    }

    // below function is use to return the item of grid view.
    override fun getItem(position: Int): Any? {
        return null
    }

    // below function is use to return item id of grid view.
    override fun getItemId(position: Int): Long {
        return 0
    }

    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.gridcarditems, null)
        }
        text= convertView!!.findViewById(R.id.idname)
        img = convertView!!.findViewById(R.id.idimage)
        img.setImageResource(courseList.get(position).Img)
        text.setText(courseList.get(position).Name)
        return convertView
    }
}
