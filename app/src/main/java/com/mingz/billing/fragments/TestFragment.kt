package com.mingz.billing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mingz.billing.R
import com.mingz.billing.ui.MultilevelListView

class TestFragment : Fragment() {
    private lateinit var dataListView: MultilevelListView

    companion object {
        @JvmStatic
        fun newInstance() = TestFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { //context ->
            initView(view)
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
        dataListView = view.findViewById(R.id.dataListView)
    }

    private class Level1(data: String, subordinateData: Array<Level2>?) :
        MultilevelListView.Data<String, Level1.ViewHolder>(
        android.R.layout.simple_list_item_1, data, subordinateData
    ) {

        constructor(data: String) : this(data, null);

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.text1 = view.findViewById(android.R.id.text1)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                return
            }
            viewHolder.text1.text = data
        }

        override fun toString(): String {
            return data!!
        }

        private class ViewHolder {
            lateinit var text1: TextView
        }
    }

    private class Level2(data: Int, subordinateData: Array<Level3>?) :
        MultilevelListView.Data<Int, Level2.ViewHolder>(
        android.R.layout.simple_list_item_1, data, subordinateData
    ) {
        constructor(data: Int) : this(data, null);

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.text1 = view.findViewById(android.R.id.text1)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                return
            }
            viewHolder.text1.text = data.toString()
        }

        override fun toString(): String {
            return data.toString()
        }

        private class ViewHolder {
            lateinit var text1: TextView
        }
    }

    private class Level3(data: Double) : MultilevelListView.Data<Double, Level3.ViewHolder>(
        android.R.layout.simple_list_item_1, data
    ) {
        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.text1 = view.findViewById(android.R.id.text1)
            return viewHolder
        }

        override fun loadingDataOnView(viewHolder: Any) {
            if (viewHolder !is ViewHolder) {
                return
            }
            viewHolder.text1.text = data.toString()
        }

        override fun toString(): String {
            return data.toString()
        }

        private class ViewHolder {
            lateinit var text1: TextView
        }
    }
}