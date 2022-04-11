package com.mingz.billing.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.mingz.billing.R
import com.mingz.billing.ui.MyExpandableListView
import com.mingz.billing.utils.MyLog

class BillingMainFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = BillingMainFragment()
    }

    private lateinit var yearBilling: MyExpandableListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main_billing, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { context ->
            initView(view)
            val data = arrayListOf(
                YearData("1", arrayListOf(
                    MonthData("1 - 1", arrayListOf(
                        DayData("1 - 1 - 1"),
                        DayData("1 - 1 - 2")
                    )),
                    MonthData("1 - 2", arrayListOf(
                        DayData("1 - 2 - 1"),
                        DayData("1 - 2 - 2"),
                        DayData("1 - 2 - 3")
                    )),
                )),
                YearData("2", arrayListOf(
                    MonthData("2 - 1", arrayListOf(
                        DayData("2 - 1 - 1"),
                        DayData("2 - 1 - 2")
                    )),
                    MonthData("2 - 2", arrayListOf(
                        DayData("2 - 2 - 1"),
                        DayData("2 - 2 - 2")
                    )),
                    MonthData("2 - 3", arrayListOf(
                        DayData("2 - 3 - 1")
                    )),
                )),
                YearData("3", arrayListOf(
                    MonthData("3 - 1", arrayListOf(
                        DayData("3 - 1 - 1")
                    ))
                ))
            )
            yearBilling.setAdapter(YearBillingAdapter(context, data))
        }
    }

    private fun initView(view: View) {
        yearBilling = view.findViewById(R.id.yearBilling)
    }

    private data class DayData(val data: String)

    private data class MonthData(val data: String, val dayData: List<DayData>)

    private data class YearData(val data: String, val monthData: List<MonthData>)

    private class YearBillingAdapter(
        val context: Context,
        val data: List<YearData>,
    ) : BaseExpandableListAdapter() {
        private val myLog = MyLog(this)

        override fun getGroupCount(): Int = data.size

        override fun getChildrenCount(groupPosition: Int): Int =
            if (data[groupPosition].monthData.isEmpty()) 0 else 1

        override fun getGroup(groupPosition: Int): Any =
            data[groupPosition].data

        override fun getChild(groupPosition: Int, childPosition: Int): Any =
            data[groupPosition].monthData

        override fun getGroupId(groupPosition: Int): Long {
            val id = data[groupPosition].data.hashCode().toLong()
            myLog.v("1st - $groupPosition getGroupId: $id")
            return id
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            val id = 0L
            myLog.v("1st - ($groupPosition, $childPosition) getChildId: $id")
            return id
        }

        override fun hasStableIds(): Boolean = true

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean,
            convertView: View?, parent: ViewGroup?
        ): View {
            myLog.v("1st - $groupPosition getGroupView  isNull: ${convertView == null}")
            val view: View
            val viewHolder: GroupViewHolder
            if (convertView == null) {
                view = View.inflate(context, R.layout.item_list_billing_year, null)
                viewHolder = GroupViewHolder()
                viewHolder.text = view.findViewById(R.id.text)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = convertView.tag as GroupViewHolder
            }
            viewHolder.text.text = data[groupPosition].data
            return view
        }

        override fun getChildView(groupPosition: Int, childPosition: Int,
            isLastChild: Boolean, convertView: View?, parent: ViewGroup?
        ): View {
            myLog.v("1st - ($groupPosition, $childPosition) getChildView  isNull: ${convertView == null}")
            val view: View
            val viewHolder: ChildViewHolder
            if (convertView == null) {
                view = View.inflate(context, R.layout.drop_item_list_billing_year_child, null)
                viewHolder = ChildViewHolder()
                viewHolder.monthBilling = view.findViewById(R.id.monthBilling)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = convertView.tag as ChildViewHolder
            }
            viewHolder.monthBilling.setAdapter(MonthBillingAdapter(context, data[groupPosition].monthData))
//            viewHolder.monthBilling.setOnGroupClickListener { parent, v, groupPosition, id ->
//                myLog.v("Month onGroupClick")
//                parent.requestLayout()
//                false
//            }
            //viewHolder.monthBilling.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            //    myLog.v("1st - 点击Child: onChild")
            //    false
            //}
            return view
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

        private class GroupViewHolder {
            lateinit var text: TextView
        }

        private class ChildViewHolder {
            lateinit var monthBilling: MyExpandableListView
        }

        private class MonthBillingAdapter(
            val context: Context,
            val data: List<MonthData>,
        ) : BaseExpandableListAdapter() {
            private val myLog = MyLog(this)

            override fun getGroupCount(): Int =
                data.size

            override fun getChildrenCount(groupPosition: Int): Int =
                data[groupPosition].dayData.size

            override fun getGroup(groupPosition: Int): Any =
                data[groupPosition].data

            override fun getChild(groupPosition: Int, childPosition: Int): Any =
                data[groupPosition].dayData[childPosition].data

            override fun getGroupId(groupPosition: Int): Long {
                val id = data[groupPosition].data.hashCode().toLong()
                myLog.v("2rd - $groupPosition getGroupId: $id")
                return id
            }

            override fun getChildId(groupPosition: Int, childPosition: Int): Long {
                val id = data[groupPosition].dayData[childPosition].data.hashCode().toLong()
                myLog.v("2rd - ($groupPosition, $childPosition) getChildId: $id")
                return id
            }

            override fun hasStableIds(): Boolean = true

            override fun getGroupView(groupPosition: Int, isExpanded: Boolean,
                convertView: View?, parent: ViewGroup?
            ): View {
                myLog.v("2rd - $groupPosition getGroupView  isNull: ${convertView == null}")
                val view: View
                val viewHolder: GroupViewHolder
                if (convertView == null) {
                    view = View.inflate(context, R.layout.item_list_billing_month, null)
                    viewHolder = GroupViewHolder()
                    viewHolder.text = view.findViewById(R.id.text)
                    view.tag = viewHolder
                } else {
                    view = convertView
                    viewHolder = convertView.tag as GroupViewHolder
                }
                viewHolder.text.text = data[groupPosition].data
                return view
            }

            override fun getChildView(groupPosition: Int, childPosition: Int,
                isLastChild: Boolean, convertView: View?, parent: ViewGroup?
            ): View {
                myLog.v("2rd - ($groupPosition, $childPosition) ChildView  isNull: ${convertView == null}")
                val view: View
                val viewHolder: ChildViewHolder
                if (convertView == null) {
                    view = View.inflate(context, R.layout.item_list_billing_day, null)
                    viewHolder = ChildViewHolder()
                    viewHolder.text = view.findViewById(R.id.text)
                    view.tag = viewHolder
                } else {
                    view = convertView
                    viewHolder = convertView.tag as ChildViewHolder
                }
                viewHolder.text.text = data[groupPosition].dayData[childPosition].data
                return view
            }

            override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

            private class GroupViewHolder {
                lateinit var text: TextView
            }

            private class ChildViewHolder {
                lateinit var text: TextView
            }
        }
    }
}
