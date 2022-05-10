package com.mingz.billing.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityEditTypeBinding
import com.mingz.billing.databinding.DialogAddTypeBinding
import com.mingz.billing.databinding.ItemListTypeInfoBinding
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.Tools
import java.util.*

class EditTypeActivity : AppCompatActivity() {
    private val activity = this
    private lateinit var binding: ActivityEditTypeBinding
    private lateinit var adapter: EditTypeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTypeBinding.inflate(layoutInflater)
        window.statusBarColor = getColor(R.color.themeWhite)
        setContentView(binding.root)

        setResult(RESULT_CANCELED)
        adapter = EditTypeAdapter(activity)
        binding.typeList.adapter = adapter
        myListener()
    }

    private fun myListener() {
        binding.back.setOnClickListener { onBackPressed() }
        binding.addType.setOnClickListener {
            addOrEditType(activity, -1, adapter)
        }
        binding.typeList.setOnItemClickListener { _, _, position, _ ->
            addOrEditType(activity, position, adapter)
        }
        binding.save.setOnClickListener {
            DataSource.typeList.replace(adapter.sortTypeList)
            // TODO: 写入文件
            Tools.showToast(activity, "已保存")
            setResult(RESULT_OK)
            onBackPressed()
        }
    }

    private fun addOrEditType(context: Context, index: Int, adapter: EditTypeAdapter) {
        val binding = DialogAddTypeBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context, R.style.RoundCornerDialog)
            .setView(binding.root)
            .create()
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        if (index >= 0) {
            binding.name.setText(adapter.sortTypeList[index].content)
        }
        binding.cancelBtn.setOnClickListener { dialog.cancel() }
        binding.okBtn.setOnClickListener {
            val content = binding.name.text.toString()
            if (content.isEmpty()) {
                Tools.showToast(context, "名称不能为空")
            } else {
                if (index >= 0) {
                    adapter.setData(index, content)
                } else {
                    adapter.addData(content)
                }
                dialog.cancel()
            }
        }
    }

    private class EditTypeAdapter(private val context: Context) : BaseAdapter() {
        val sortTypeList = DataSource.typeList.copy()

        init {
            Collections.sort(sortTypeList) { o1, o2 -> o1.content.compareTo(o2.content) }
        }

        fun setData(index: Int, content: String) {
            val goalType = sortTypeList[index]
            if (goalType.content == content) {
                return
            }
            sortTypeList[index] = goalType.setContent(content)
            Collections.sort(sortTypeList) { o1, o2 -> o1.content.compareTo(o2.content) }
            notifyDataSetChanged()
        }

        fun addData(content: String) {
            sortTypeList.add(content)
            Collections.sort(sortTypeList) { o1, o2 -> o1.content.compareTo(o2.content) }
            notifyDataSetChanged()
        }

        override fun getCount() = sortTypeList.size

        override fun getItem(position: Int) = null

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val binding: ItemListTypeInfoBinding
            if (convertView == null) {
                binding = ItemListTypeInfoBinding.inflate(LayoutInflater.from(context))
                view = binding.root
                view.tag = binding
            } else {
                view = convertView
                binding = view.tag as ItemListTypeInfoBinding
            }
            val type = sortTypeList[position]
            binding.type.text = type.content
            binding.delete.setOnClickListener {
                Tools.showTipPopup(context, "确定要移除该资产类型吗？") {
                    sortTypeList.removeAt(position)
                    notifyDataSetChanged()
                }
            }
            return view
        }
    }
}