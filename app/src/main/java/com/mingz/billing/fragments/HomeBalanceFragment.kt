package com.mingz.billing.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentHomeBalanceBinding
import com.mingz.billing.ui.MultilevelListView
import com.mingz.billing.utils.Tools

class HomeBalanceFragment : HomeFragment() {
    private lateinit var binding: FragmentHomeBalanceBinding

    companion object {
        @JvmStatic
        fun newInstance() = HomeBalanceFragment()
    }

    override fun getTitle(): String = "账户余额"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        /*binding.testMulList.setOnItemClickListener { data, position ->
            val content = when (data) {
                is OptionLevelOne -> data.data
                is OptionLevelTwo -> data.data
                is OptionLevelThree -> data.data
                else -> null
            }
            Tools.showToast(context, "$position: $content")
        }*/
    }

    override fun onResume() {
        super.onResume()
        binding.testMulList.setData(arrayOf(
            OptionLevelOne("1", arrayOf(
                OptionLevelTwo("1-1"),
                OptionLevelTwo("1-2", arrayOf(
                    OptionLevelThree("1-2-1"),
                    OptionLevelThree("1-2-2"),
                    OptionLevelThree("1-2-3")
                )),
                OptionLevelTwo("1-3", arrayOf(
                    OptionLevelThree("1-3-1"),
                    OptionLevelThree("1-3-2")
                ))
            )),
            OptionLevelOne("2", arrayOf(
                OptionLevelTwo("2-1", arrayOf(
                    OptionLevelThree("2-1-1")
                )),
                OptionLevelTwo("2-2")
            )),
            OptionLevelOne("3", arrayOf(
                OptionLevelTwo("3-1"),
                OptionLevelTwo("3-2", arrayOf(
                    OptionLevelThree("3-2-1"),
                    OptionLevelThree("3-2-2"),
                    OptionLevelThree("3-2-3")
                ))
            ))
        ))
    }

    private class OptionLevelOne(data: String, children: Array<OptionLevelTwo>? = null) :
        MultilevelListView.Data<String, OptionLevelOne.ViewHolder>(data, children) {

        override fun getResId() = R.layout.temp_item_level_one

        override fun getLevel() = 0

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.icon = view.findViewById(R.id.icon)
            viewHolder.content = view.findViewById(R.id.content)
            return viewHolder
        }

        override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
            if (viewHolder is ViewHolder) {
                viewHolder.content.text = data
                if (subordinateData == null) {
                    viewHolder.icon.visibility = View.INVISIBLE
                } else {
                    viewHolder.icon.visibility = View.VISIBLE
                    viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(context,
                        if (isExpand) R.drawable.ic_arrow_put_away else R.drawable.ic_arrow_open
                    ))
                }
            }
        }

        override fun toString(): String {
            return "层级: $level; $data; $isExpand; ${if (subordinateData == null) "null" else "not null"}"
        }

        private class ViewHolder {
            lateinit var icon: ImageView
            lateinit var content: TextView
        }
    }

    private class OptionLevelTwo(data: String, children: Array<OptionLevelThree>? = null) :
        MultilevelListView.Data<String, OptionLevelTwo.ViewHolder>(data, children) {

        override fun getResId() = R.layout.temp_item_level_two

        override fun getLevel() = 1

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.icon = view.findViewById(R.id.icon)
            viewHolder.content = view.findViewById(R.id.content)
            return viewHolder
        }

        override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
            if (viewHolder is ViewHolder) {
                viewHolder.content.text = data
                if (subordinateData == null) {
                    viewHolder.icon.visibility = View.INVISIBLE
                } else {
                    viewHolder.icon.visibility = View.VISIBLE
                    viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(context,
                        if (isExpand) R.drawable.ic_arrow_put_away else R.drawable.ic_arrow_open
                    ))
                }
            }
        }

        override fun toString(): String {
            return "层级: $level; $data; $isExpand; ${if (subordinateData == null) "null" else "not null"}"
        }

        private class ViewHolder {
            lateinit var icon: ImageView
            lateinit var content: TextView
        }
    }

    private class OptionLevelThree(data: String) :
        MultilevelListView.Data<String, OptionLevelThree.ViewHolder>(data) {

        override fun getResId() = R.layout.temp_item_level_three

        override fun getLevel() = 2

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.content = view.findViewById(R.id.content)
            return viewHolder
        }

        override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
            if (viewHolder is ViewHolder) {
                viewHolder.content.text = data
            }
        }

        override fun toString(): String {
            return "层级: $level; $data; $isExpand; ${if (subordinateData == null) "null" else "not null"}"
        }

        private class ViewHolder {
            lateinit var content: TextView
        }
    }
}
