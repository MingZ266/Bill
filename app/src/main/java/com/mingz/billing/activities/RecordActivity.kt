package com.mingz.billing.activities

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityRecordBinding
import com.mingz.billing.fragments.*
import com.mingz.billing.ui.DrawableTextView
import com.mingz.billing.ui.MultilevelListView

class RecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordBinding
    private val expenditure by lazy { RecordExpenditureFragment.newInstance() }
    private val income by lazy { RecordIncomeFragment.newInstance() }
    private val transfer by lazy { RecordTransferFragment.newInstance() }
    private val fundPurchase by lazy { RecordFundPurchaseFragment.newInstance() }
    private val fundSales by lazy { RecordFundSalesFragment.newInstance() }
    private val fundTransfer by lazy { RecordFundTransferFragment.newInstance() }
    private val fundDividend by lazy { RecordFundDividendFragment.newInstance() }
    private lateinit var current: RecordFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        myListener()
    }

    private fun initView() {
        current = expenditure
        binding.title.text = current.getTitle()
        supportFragmentManager.beginTransaction().add(R.id.space, current).commit()
        // 切换记录类型
        binding.optionList.setData(arrayOf(
            OptionLevelOne(expenditure.getTitle()),
            OptionLevelOne(income.getTitle()),
            OptionLevelOne(transfer.getTitle()),
            OptionLevelOne("基金", arrayOf(
                OptionLevelTwo(fundPurchase.getTitle()),
                OptionLevelTwo(fundSales.getTitle()),
                OptionLevelTwo(fundTransfer.getTitle()),
                OptionLevelTwo(fundDividend.getTitle())
            ))
        ))
    }

    private fun myListener() {
        binding.backIcon.setOnClickListener { onBackPressed() }
        binding.toggle.setOnClickListener { binding.drawer.openDrawer(Gravity.END) }
        binding.back.setOnClickListener { onBackPressed() }
        binding.save.setOnClickListener { current.save() }
        binding.optionList.setOnItemClickListener { data, _ ->
            if (data is OptionLevelOne) {
                if (data.subordinateData == null) {
                    when (data.data) {
                        expenditure.getTitle() -> replaceFragment(expenditure)
                        income.getTitle() -> replaceFragment(income)
                        transfer.getTitle() -> replaceFragment(transfer)
                    }
                    binding.drawer.closeDrawer(Gravity.END)
                }
            } else if (data is OptionLevelTwo) {
                when (data.data) {
                    fundPurchase.getTitle() -> replaceFragment(fundPurchase)
                    fundSales.getTitle() -> replaceFragment(fundSales)
                    fundTransfer.getTitle() -> replaceFragment(fundTransfer)
                    fundDividend.getTitle() -> replaceFragment(fundDividend)
                }
                binding.drawer.closeDrawer(Gravity.END)
            }
        }
    }

    private fun replaceFragment(fragment: RecordFragment) {
        binding.title.text = fragment.getTitle()
        supportFragmentManager.beginTransaction().replace(R.id.space, fragment).commit()
        current = fragment
    }

    private class OptionLevelOne(data: String, children: Array<OptionLevelTwo>? = null) :
        MultilevelListView.Data<String, OptionLevelOne.ViewHolder>(data, children) {

        override fun getResId() = R.layout.item_side_bar_record_type_level_one

        override fun getLevel() = 0

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.margin = view.findViewById(R.id.margin)
            viewHolder.option = view.findViewById(R.id.option)
            return viewHolder
        }

        override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
            if (viewHolder is ViewHolder) {
                viewHolder.option.text = data
                if (subordinateData == null) {
                    viewHolder.margin.visibility = View.VISIBLE
                    viewHolder.option.setDrawables(null, null, null, null)
                } else {
                    viewHolder.margin.visibility = View.GONE
                    if (isExpand) {
                        viewHolder.option.setDrawables(
                            ContextCompat.getDrawable(
                                context, R.drawable.ic_arrow_put_away
                            ), null, null, null
                        )
                    } else {
                        viewHolder.option.setDrawables(
                            ContextCompat.getDrawable(
                                context, R.drawable.ic_arrow_open
                            ), null, null, null
                        )
                    }
                }
            }
        }

        class ViewHolder {
            lateinit var margin: View
            lateinit var option: DrawableTextView
        }
    }

    private class OptionLevelTwo(data: String) :
        MultilevelListView.Data<String, OptionLevelTwo.ViewHolder>(data) {

        override fun getResId() = R.layout.item_side_bar_record_type_level_two

        override fun getLevel() = 1

        override fun newViewHolder(view: View): ViewHolder {
            val viewHolder = ViewHolder()
            viewHolder.option = view.findViewById(R.id.option)
            return viewHolder
        }

        override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
            if (viewHolder is ViewHolder) {
                viewHolder.option.text = data
            }
        }

        private class ViewHolder {
            lateinit var option: TextView
        }
    }
}