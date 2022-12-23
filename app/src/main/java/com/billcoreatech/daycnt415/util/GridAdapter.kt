package com.billcoreatech.daycnt415.util

import android.annotation.SuppressLint
import android.content.*
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.billcoreatech.daycnt415.R
import com.billcoreatech.daycnt415.database.DBHandler
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class GridAdapter(context: Context, private var list: List<String>) : BaseAdapter() {

    var TAG = "GridAdapter"
    private lateinit var mCal: Calendar
    private val inflater: LayoutInflater
    lateinit var tvItemGridView: TextView
    lateinit var tv1: TextView
    private var nListCnt = 0
    var sdf: SimpleDateFormat
    lateinit var dbHandler: DBHandler

    /**
     * 생성자
     *
     * @param context
     * @param list
     */
    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        sdf = SimpleDateFormat("yyyyMMdd")
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): String {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun updateReceiptsList(_oData: ArrayList<String>) {
        list = _oData
        nListCnt = list.size // 배열 사이즈 다시 확인
        notifyDataSetChanged() // 그냥 여기서 하자
    }

    @SuppressLint("SetTextI18n", "Range")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView1 = convertView
        val context = parent.context
        if (convertView1 == null) {
            convertView1 = inflater.inflate(R.layout.itemcalendar, parent, false)
            tvItemGridView = convertView1.findViewById(R.id.tv_item_gridview)
            tv1 = convertView1.findViewById(R.id.tv1)
        }
        if (getItem(position).length > 3) {
            tvItemGridView.text = "" + getItem(position).substring(6, 8)
        } else {
            tvItemGridView.text = "" + getItem(position)
        }

        //해당 날짜 텍스트 컬러,배경 변경
        mCal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd")
        try {
            val now = System.currentTimeMillis()
            val toDay = Date(now)
            val sToday = sdf.format(toDay)
            sdf.parse(getItem(position))?.let { mCal.setTime(it) }
            val weekOfDay = mCal.get(Calendar.DAY_OF_WEEK)
            dbHandler = DBHandler.Companion.open(context)
            val rs = dbHandler.getTodayMsg(getItem(position))
            var msg = ""
            if (rs.moveToNext()) {
                msg = rs.getString(rs.getColumnIndex("msg"))
                tv1.text = msg
                if ("Y" == rs.getString(rs.getColumnIndex("isholiday"))) {
                    tvItemGridView.setTextColor(context.getColor(R.color.softred))
                }
            }
            dbHandler.close()
            if (weekOfDay == Calendar.SUNDAY) {
                tvItemGridView.setTextColor(context.getColor(R.color.softred))
            }
            if (weekOfDay == Calendar.SATURDAY && "" == msg) {
                tvItemGridView.setTextColor(context.getColor(R.color.softblue))
            }
            if (sToday == getItem(position)) { //오늘 day 텍스트 컬러 변경
                tvItemGridView.background =
                    ContextCompat.getDrawable(context, R.drawable.background_text_gray)
                tvItemGridView.setTextColor(context.getColor(R.color.white))
            } else {
                tvItemGridView.background =
                    ContextCompat.getDrawable(context, R.drawable.backgroud_border_100)
            }
        } catch (e: Exception) {
            if ("일" == getItem(position)) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvItemGridView.setBackgroundColor(context.getColor(R.color.softred))
                tvItemGridView.background =
                    ContextCompat.getDrawable(context, R.drawable.backgroud_border_200)
            } else if ("토" == getItem(position)) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvItemGridView.setBackgroundColor(context.getColor(R.color.softred))
                tvItemGridView.background =
                    ContextCompat.getDrawable(context, R.drawable.backgroud_border_200)
            } else if ("" != getItem(position)) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvItemGridView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                tvItemGridView.background =
                    ContextCompat.getDrawable(context, R.drawable.backgroud_border_200)
            }
        }
        tv1.isFocusable = true
        return convertView1
    }
}