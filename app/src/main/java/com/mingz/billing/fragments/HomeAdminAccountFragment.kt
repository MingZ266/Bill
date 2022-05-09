package com.mingz.billing.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import com.mingz.billing.R
import com.mingz.billing.databinding.*
import com.mingz.billing.utils.Account
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.Tools
import java.util.*

class HomeAdminAccountFragment : HomeFragment() {
    private lateinit var binding: FragmentHomeAdminAccountBinding
    private lateinit var adapter: AccountListAdapter
    private var position = -1

    companion object {
        @JvmStatic
        fun newInstance() = HomeAdminAccountFragment()
    }

    override fun getTitle() = "账户"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeAdminAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        adapter = AccountListAdapter(context)
        binding.accountList.adapter = adapter
        binding.accountList.setOnItemClickListener { _, _, position, _ ->
            this.position = position
            addOrEditAccountInfo(context, adapter.getData(position).copy())
        }
        binding.addAccount.setOnClickListener {
            position = adapter.count
            addOrEditAccountInfo(context, DataSource.accountList.generateEmptyAccount())
        }
        binding.save.setOnClickListener {
            DataSource.accountList.replace(adapter.sortAccountList)
            // TODO: 写入文件
            Tools.showToast(context, "已保存")
        }
    }

    private fun addOrEditAccountInfo(context: Context, account: Account) {
        val dialog = AlertDialog.Builder(context, R.style.FullScreenDialog).create()
        val binding = DialogAccountInfoBinding.inflate(LayoutInflater.from(context))
        Tools.setAsBottomPopupAndShow(dialog, binding.root, true)
        dialog.setCanceledOnTouchOutside(false)
        // 设置以避免窗口获取焦点使得不能正常弹出输入法
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        binding.accountName.setText(account.name)
        val assetsAdapter = AssetsListAdapter(context, account)
        binding.assetsList.adapter = assetsAdapter
        accountInfoListener(context, binding, assetsAdapter, dialog, account)
    }

    private fun accountInfoListener(context: Context, binding: DialogAccountInfoBinding,
                                    assetsAdapter: AssetsListAdapter, dialog: AlertDialog, account: Account) {
        binding.back.setOnClickListener { dialog.cancel() }

        // 回车时收起键盘，清除焦点
        Tools.clearFocusOnEnter(context, binding.accountName)

        binding.addAssets.setOnClickListener {
            Tools.showSelectType(context, "选择资产类型", -1, false, {
                if (account.existsAssets(it)) {
                    Tools.showToast(context, "该类型资产已存在")
                } else {
                    account.findOrAddAssets(it)
                    assetsAdapter.notifyDataSetChanged()
                }
            }, {
                // TODO: onEdit
                Tools.showToast(context, "编辑资产类型")
                Tools.setAsBottomPopupAndShow(AlertDialog.Builder(context, R.style.FullScreenDialog).create(),
                    DialogAdminTypeBinding.inflate(LayoutInflater.from(context)).root, true)
            })
        }

        binding.okBtn.setOnClickListener {
            val name = binding.accountName.text.toString()
            if (name.isEmpty()) {
                Tools.showToast(context, "请输入账户名称")
                return@setOnClickListener
            }
            val result = account.setName(name)
            if (position < adapter.count) {
                adapter.setData(position, result)
            } else {
                adapter.addData(result)
            }
            dialog.cancel()
        }
    }

    private class AccountListAdapter(private val context: Context) : BaseAdapter() {
        val sortAccountList = DataSource.accountList.copy()

        init {
            Collections.sort(sortAccountList) { o1, o2 -> o1.name.compareTo(o2.name) }
        }

        fun getData(position: Int) = sortAccountList[position]

        fun setData(position: Int, account: Account) {
            val oldAccount = sortAccountList[position]
            sortAccountList[position] = account
            if (account.name == oldAccount.name) {
                return
            }
            Collections.sort(sortAccountList) { o1, o2 -> o1.name.compareTo(o2.name) }
            notifyDataSetChanged()
        }

        fun addData(account: Account) {
            sortAccountList.addAccount(account)
            Collections.sort(sortAccountList) { o1, o2 -> o1.name.compareTo(o2.name) }
            notifyDataSetChanged()
        }

        override fun getCount() = sortAccountList.size

        override fun getItem(position: Int) = null

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val binding: ItemListAdminAccountBinding
            if (convertView == null) {
                binding = ItemListAdminAccountBinding.inflate(LayoutInflater.from(context))
                view = binding.root
                view.tag = binding
            } else {
                binding = convertView.tag as ItemListAdminAccountBinding
                view = convertView
            }
            with (sortAccountList[position]) {
                binding.name.text = name
                binding.disable.imageTintList = ColorStateList.valueOf(
                    context.getColor(if (availability) R.color.whiteGrey else R.color.themeRed)
                )
                binding.disable.setOnClickListener {
                    sortAccountList[position] = this.setAvailability(!availability)
                    notifyDataSetChanged()
                }
            }
            binding.delete.setOnClickListener {
                Tools.showTipPopup(context, "删除会丢失该账户下资产信息，确定要删除吗？") {
                    sortAccountList.removeAt(position)
                    notifyDataSetChanged()
                }
            }
            return view
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