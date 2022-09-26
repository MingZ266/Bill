package com.mingz.billing.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityEditSubjectBinding
import com.mingz.billing.databinding.ItemRecyclerSubjectSortBinding
import com.mingz.billing.ui.MultilevelListView
import com.mingz.billing.utils.SubjectArray
import com.mingz.billing.utils.SubjectLvOne
import com.mingz.billing.utils.SubjectLvTwo
import com.mingz.billing.utils.Tools
import com.mingz.billing.utils.Tools.Companion.remove

class EditSubjectActivity : AppCompatActivity() {
    private val activity = this
    private val subjectCopy by lazy { subject!!.copy() }
    private lateinit var binding: ActivityEditSubjectBinding
    private lateinit var currentSubject: SubjectArray

    companion object {
        @JvmField
        var subject: SubjectArray? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (subject == null) {
            finish()
            return
        }
        initView()
        myListener()
    }

    private fun initView() {
        val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(
            ColorStateList.valueOf(getColor(R.color.themeGrey))
        ))
        binding.subjectList.adapter = SubjectOneAdapter(activity, subjectCopy,
            itemTouchHelper, ColorStateList.valueOf(getColor(R.color.themeColor)))
    }

    private fun myListener() {
        // TODO: add

        // TODO: backList

        binding.save.setOnClickListener {
            subject!!.replace(subjectCopy)
            // TODO: 写入文件
            Tools.showToast(activity, "已保存")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subject = null
    }

    private class SubjectAdapter(
        private val context: Context, var subject: SubjectArray,
        private val itemTouchHelper: ItemTouchHelper, private val dragTint: ColorStateList
    ) : RecyclerView.Adapter<ViewHolder>() {
        private var data: Array<MultilevelListView.Data<*, *>> =
            Array(subject.size()) { subject[it] }
        private var childPosition = -1 // 记录展开的二级科目所属一级科目的位置

        fun move(current: Int, target: Int) {
            if (current < 0 || target < 0) {
                return
            }
            if (childPosition >= 0) {
                // 缓存数据与数据源为同一引用
                val currentData = data[current]
                if (target >= current) {
                    for (i in current until target) {
                        data[i] = data[i + 1]
                    }
                } else {
                    for (i in (current - 1) downTo target) {
                        data[i + 1] = data[i]
                    }
                }
                data[target] = currentData
            } else {
                // 移动缓存数据及数据源
                val currentData = data[current]
                val currentSubject = subject[current]
                if (target >= current) {
                    for (i in current until target) {
                        data[i] = data[i + 1]
                        subject[i] = subject[i + 1]
                    }
                } else {
                    for (i in (current - 1) downTo target) {
                        data[i + 1] = data[i]
                        subject[i + 1] = subject[i]
                    }
                }
                data[target] = currentData
                subject[target] = currentSubject
            }
            notifyItemMoved(current, target)
        }

        // 展开一级科目，返回是否已展开
        fun expand(position: Int): Boolean {
            if (childPosition >= 0) {
                return false
            }
            val theData = data[position]
            if (theData is SubjectLvOne) {
                val children = theData.subordinateData
                if (children != null) {
                    data = children
                    childPosition = position
                    notifyDataSetChanged()
                    return true
                }
            }
            return false
        }

        // 收起二级科目，恢复为一级科目
        fun stow() {
            if (childPosition < 0) {
                return
            }
            data = Array(subject.size()) { subject[it] }
            childPosition = -1
            notifyDataSetChanged()
        }

        fun addSubject(name: String) {
            // TODO: 应用于data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRecyclerSubjectSortBinding.inflate(
                LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.binding.drag) {
                setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        imageTintList = dragTint
                        itemTouchHelper.startDrag(holder)
                        return@setOnTouchListener true
                    }
                    false
                }
            }
            val theData = data[position]
            holder.binding.subject.text = when (theData) {
                is SubjectLvOne -> theData.data!!.content
                is SubjectLvTwo -> theData.data!!.content
                else -> ""
            }
            holder.binding.delete.setOnClickListener {
                Tools.showTipPopup(context, if (childPosition >= 0) "确定要移除该科目吗？" else
                    "确定要移除该科目及所属二级科目吗？") {
                    data = data.remove(position)
                    if (childPosition >= 0) { // 当前数据为二级科目
                        // 修改所属的一级科目
                        val oldSubjectOne = subject[childPosition]
                        subject[childPosition] = SubjectLvOne(oldSubjectOne.data!!, data)
                    } else { // 当前数据为一级科目
                        subject.remove(position)
                    }
                    notifyDataSetChanged()
                }
            }
            holder.binding.expand.visibility = if (theData.subordinateData == null) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }

        override fun getItemCount() = data.size
    }

    private class SubjectOneAdapter(
        private val context: Context, private val subject: SubjectArray,
        private val itemTouchHelper: ItemTouchHelper, private val dragTint: ColorStateList
    ) : RecyclerView.Adapter<ViewHolder>() {

        fun move(current: Int, target: Int) {
            if (current < 0 || target < 0) {
                return
            }
            val currentSubject = subject[current]
            if (target >= current) {
                for (i in current until target) {
                    subject[i] = subject[i + 1]
                }
            } else {
                for (i in (current - 1) downTo target) {
                    subject[i + 1] = subject[i]
                }
            }
            subject[target] = currentSubject
            notifyItemMoved(current, target)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRecyclerSubjectSortBinding.inflate(
                LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.delete.setOnClickListener {
                Tools.showTipPopup(context, "确定要移除该科目吗？（其下的二级科目也将被移除）") {
                    subject.remove(position)
                    notifyDataSetChanged()
                }
            }
            with(holder.binding.drag) {
                setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        imageTintList = dragTint
                        itemTouchHelper.startDrag(holder)
                        return@setOnTouchListener true
                    }
                    false
                }
            }
            val data = subject[position]
            holder.binding.subject.text = data.data!!.content
            with(holder.binding.expand) {
                visibility = if (data.subordinateData == null) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            }
        }

        override fun getItemCount() = subject.size()
    }

    /*private class SubjectTwoAdapter(
        private val context: Context, private var nextId: Int, private var subject: SubjectLvOne,
        private val itemTouchHelper: ItemTouchHelper, private val dragTint: ColorStateList
    ) : RecyclerView.Adapter<ViewHolder>() {

        fun move(current: Int, target: Int) {
            if (current < 0 || target < 0) {
                return
            }
            val currentSubject = subject[current]
            if (target >= current) {
                for (i in current until target) {
                    subject[i] = subject[i + 1]
                }
            } else {
                for (i in (current - 1) downTo target) {
                    subject[i + 1] = subject[i]
                }
            }
            subject[target] = currentSubject
            notifyItemMoved(current, target)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRecyclerSubjectSortBinding.inflate(
                LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.delete.setOnClickListener {
                Tools.showTipPopup(context, "确定要移除该科目吗？") {
                    subject = subject.remove(position)
                    notifyDataSetChanged()
                }
            }
            with(holder.binding.drag) {
                setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        imageTintList = dragTint
                        itemTouchHelper.startDrag(holder)
                        return@setOnTouchListener true
                    }
                    false
                }
            }
            holder.binding.subject.text = subject[position].data!!.content
            holder.binding.expand.visibility = View.GONE
        }

        override fun getItemCount() = subject.size()
    }*/

    private class ViewHolder(
        val binding: ItemRecyclerSubjectSortBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private class ItemTouchCallback(
        private val notDragTint: ColorStateList
    ) : ItemTouchHelper.Callback() {

        override fun isLongPressDragEnabled() = false

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder is ViewHolder) {
                viewHolder.binding.drag.imageTintList = notDragTint
            }
        }

        // 只允许上下拖动
        override fun getMovementFlags(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
        ) = makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
            ItemTouchHelper.UP or ItemTouchHelper.DOWN)

        override fun onMove(recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = recyclerView.adapter
            if (adapter is SubjectOneAdapter) {
                adapter.move(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }/* else if (adapter is SubjectTwoAdapter) {
                adapter.move(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }*/
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
}