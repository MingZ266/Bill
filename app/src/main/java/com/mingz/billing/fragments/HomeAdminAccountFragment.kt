package com.mingz.billing.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mingz.billing.R
import com.mingz.billing.activities.AccountInfoActivity
import com.mingz.billing.databinding.FragmentHomeAdminAccountBinding
import com.mingz.billing.databinding.ItemListAdminAccountBinding
import com.mingz.billing.utils.Account
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.MyLog
import com.mingz.billing.utils.Tools
import java.util.*

class HomeAdminAccountFragment : HomeFragment() {
    private lateinit var binding: FragmentHomeAdminAccountBinding
    private lateinit var adapter: AccountListAdapter
    private val requestCode = Tools.getRequestCode()
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
        MyLog.TEMP.v("accountList:")
        MyLog.TEMP.d(DataSource.accountList)
        MyLog.TEMP.v("sort:")
        MyLog.TEMP.d(adapter.sortAccountList)
        binding.accountList.setOnItemClickListener { _, _, position, _ ->
            this.position = position
            addOrEditAccountInfo(adapter.getData(position).copy())
        }
        binding.addAccount.setOnClickListener {
            position = adapter.count
            addOrEditAccountInfo(DataSource.accountList.generateEmptyAccount())
        }
        binding.save.setOnClickListener {
            MyLog.TEMP.v("保存 => sort:")
            MyLog.TEMP.d(adapter.sortAccountList)
            MyLog.TEMP.v("保存前 => accountList:")
            MyLog.TEMP.d(DataSource.accountList)
            DataSource.accountList.replace(adapter.sortAccountList)
            MyLog.TEMP.v("保存后 => accountList:")
            MyLog.TEMP.d(DataSource.accountList)
            // TODO: 写入文件
            Tools.showToast(context, "已保存")
        }
    }

    private fun addOrEditAccountInfo(account: Account) {
        AccountInfoActivity.account = account
        startActivityForResult(Intent(context, AccountInfoActivity::class.java), requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode) {
            MyLog.TEMP.v("返回结果: $resultCode")
            if (resultCode == Activity.RESULT_OK) {
                if (AccountInfoActivity.account != null) {
                    if (position < adapter.count) {
                        adapter.setData(position, AccountInfoActivity.account!!)
                    } else {
                        adapter.addData(AccountInfoActivity.account!!)
                    }
                }
            }
            AccountInfoActivity.account = null
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
}