package com.mingz.billing.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.databinding.ActivityAccountInfoBinding
import com.mingz.billing.databinding.ItemListAssetsInfoBinding
import com.mingz.billing.utils.Account
import com.mingz.billing.utils.Tools

class AccountInfoActivity : AppCompatActivity() {
    private val activity = this
    private lateinit var binding: ActivityAccountInfoBinding
    private lateinit var adapter: AssetsListAdapter

    companion object {
        @JvmField
        var account: Account? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        if (account == null) {
            finish()
            return
        }
        binding = ActivityAccountInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.accountName.setText(account!!.name)
        adapter = AssetsListAdapter(activity, account!!)
        binding.assetsList.adapter = adapter
        myListener()
    }

    private fun myListener() {
        binding.back.setOnClickListener { onBackPressed() }

        binding.addAssets.setOnClickListener {
            Tools.showSelectType(activity, "选择资产类型", -1, false, {
                if (account!!.existsAssets(it)) {
                    Tools.showToast(activity, "该类型资产已存在")
                } else {
                    account!!.findOrAddAssets(it)
                    adapter.notifyDataSetChanged()
                }
            }, {
                // TODO: onEdit
                Tools.showToast(activity, "编辑资产类型")
            })
        }

        binding.okBtn.setOnClickListener {
            val name = binding.accountName.text.toString()
            if (name.isEmpty()) {
                Tools.showToast(activity, "请输入账户名称")
                return@setOnClickListener
            }
            account = account!!.setName(name)
            setResult(RESULT_OK)
            onBackPressed()
        }
    }

    private class AssetsListAdapter(
        private val context: Context, private val account: Account
    ) : BaseAdapter() {
        override fun getCount() = account.assetsList.size

        override fun getItem(position: Int) = null

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val binding: ItemListAssetsInfoBinding
            if (convertView == null) {
                binding = ItemListAssetsInfoBinding.inflate(LayoutInflater.from(context))
                view = binding.root
                view.tag = binding
            } else {
                view = convertView
                binding = view.tag as ItemListAssetsInfoBinding
            }
            val assets = account.assetsList[position]
            binding.type.text = assets.type.content
            binding.amount.text = assets.initValue
            binding.amount.setOnClickListener {
                Tools.inputAmountOfMoney(context, "初始金额", assets.initValue, 2, true) {
                    binding.amount.text = it
                    account.assetsList[position] = assets.setInitValue(it)
                }
            }
            binding.delete.setOnClickListener {
                Tools.showTipPopup(context, "确定要移除该资产吗？") {
                    account.deleteAssets(position)
                    notifyDataSetChanged()
                }
            }
            return view
        }
    }
}