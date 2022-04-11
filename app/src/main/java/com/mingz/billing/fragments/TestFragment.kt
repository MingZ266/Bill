package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mingz.billing.R
import com.mingz.billing.ui.MultilevelListView
import com.mingz.billing.ui.ShowDateTime
import com.mingz.billing.utils.MyLog
import com.mingz.billing.utils.Tools

class TestFragment : Fragment() {
    private lateinit var showDateTime: ShowDateTime
    private lateinit var dataListView: MultilevelListView

    companion object {
        private val myLog = MyLog(this)

        @JvmStatic
        fun newInstance() = TestFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { context ->
            initView(view)
            showDateTime.updateToNowTime()
            Tools.chooseDateTime(context, showDateTime)
            val data = arrayOf(
                Level1("A", arrayOf(
                    Level2(0, arrayOf(
                        Level3(0.1),
                        Level3(1.2)
                    )),
                    Level2(1, arrayOf(
                        Level3(2.3)
                    ))
                )),
                Level1("B", arrayOf(
                    Level2(2, arrayOf(
                        Level3(3.4),
                        Level3(4.5),
                        Level3(5.6)
                    ))
                )),
                Level1("C", arrayOf(
                    Level2(3, arrayOf(
                        Level3(6.7)
                    )),
                    Level2(4, arrayOf(
                        Level3(7.8)
                    )),
                    Level2(5, arrayOf(
                        Level3(8.9)
                    ))
                ))
            )
            dataListView.setData(data)
        }
    }

    private fun initView(view: View) {
        showDateTime = view.findViewById(R.id.showDateTime)
        dataListView = view.findViewById(R.id.dataListView)
    }

    private class Level1(data: String, subordinateData: Array<Level2>? = null) :
        MultilevelListView.Data<String, Level1.ViewHolder>(
        R.layout.test_item_list_level_1, data, subordinateData
    ) {

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.level1text = view.findViewById(R.id.level1text)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                myLog.v("Level 1: ${viewHolder.javaClass.canonicalName}")
                return
            }
            viewHolder.level1text.text = data
        }

        override fun toString(): String {
            return data!!
        }

        private class ViewHolder {
            lateinit var level1text: TextView
        }
    }

    private class Level2(data: Int, subordinateData: Array<Level3>? = null) :
        MultilevelListView.Data<Int, Level2.ViewHolder>(
        R.layout.test_item_list_level_2, data, subordinateData
    ) {

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.level2text = view.findViewById(R.id.level2text)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                myLog.v("Level 2: ${viewHolder.javaClass.canonicalName}")
                return
            }
            viewHolder.level2text.text = data.toString()
        }

        override fun toString(): String {
            return data.toString()
        }

        private class ViewHolder {
            lateinit var level2text: TextView
        }
    }

    private class Level3(data: Double) : MultilevelListView.Data<Double, Level3.ViewHolder>(
        R.layout.test_item_list_level_3, data
    ) {
        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.level3text = view.findViewById(R.id.level3text)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                myLog.v("Level 3: ${viewHolder.javaClass.canonicalName}")
                return
            }
            viewHolder.level3text.text = data.toString()
        }

        override fun toString(): String {
            return data.toString()
        }

        private class ViewHolder {
            lateinit var level3text: TextView
        }
    }

    /*private class Level1(data: String, subordinateData: Array<Level2>? = null) :
        MultilevelListView.Data<String, ViewHolder>(
            R.layout.test_item_list_simple_text, data, subordinateData
        ) {

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.text = view.findViewById(R.id.text)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                myLog.v("Level 1: ${viewHolder.javaClass.canonicalName}")
                return
            }
            viewHolder.text.text = "1st: $data"
        }

        override fun toString(): String {
            return data!!
        }
    }

    private class Level2(data: Int, subordinateData: Array<Level3>? = null) :
        MultilevelListView.Data<Int, ViewHolder>(
            R.layout.test_item_list_simple_text, data, subordinateData
        ) {

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.text = view.findViewById(R.id.text)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                myLog.v("Level 2: ${viewHolder.javaClass.canonicalName}")
                return
            }
            viewHolder.text.text = "   2nd: ${data.toString()}"
        }

        override fun toString(): String {
            return data.toString()
        }
    }

    private class Level3(data: Double) : MultilevelListView.Data<Double, ViewHolder>(
        R.layout.test_item_list_simple_text, data
    ) {
        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.text = view.findViewById(R.id.text)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                myLog.v("Level 3: ${viewHolder.javaClass.canonicalName}")
                return
            }
            viewHolder.text.text = "      3rd: ${data.toString()}"
        }

        override fun toString(): String {
            return data.toString()
        }
    }

    private class ViewHolder {
        lateinit var text: TextView
    }*/
}