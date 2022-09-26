package com.mingz.billing.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentHomeAdminSubjectCopyBinding
import com.mingz.billing.databinding.TempItemDragBinding
import com.mingz.billing.utils.MyLog

class HomeAdminSubjectFragmentCopy : HomeFragment() {
    private lateinit var binding: FragmentHomeAdminSubjectCopyBinding

    companion object {
        @JvmStatic
        fun newInstance() = HomeAdminSubjectFragmentCopy()
    }

    override fun getTitle() = "科目"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeAdminSubjectCopyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        val itemTouchHelper = ItemTouchHelper(MyCallback(
            ColorStateList.valueOf(context.getColor(R.color.themeGrey))
        ))
        itemTouchHelper.attachToRecyclerView(binding.recycler)
        binding.recycler.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL, false)
        binding.recycler.adapter = RecyclerAdapter(context, arrayOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        ), itemTouchHelper, ColorStateList.valueOf(context.getColor(R.color.themeColor)))
    }

    private class RecyclerAdapter(
        private val context: Context, private val data: Array<String>,
        private val itemTouchHelper: ItemTouchHelper, private val dragTint: ColorStateList
    ) : RecyclerView.Adapter<ViewHolder>() {
        fun move(current: Int, target: Int) {
            val srcData = data[current]
            if (target >= current) {
                for (i in current until target) {
                    data[i] = data[i + 1]
                }
            } else {
                for (i in (current - 1) downTo target) {
                    data[i + 1] = data[i]
                }
            }
            data[target] = srcData
            notifyItemMoved(current, target)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = TempItemDragBinding.inflate(LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.text.text = data[position]
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
        }

        override fun getItemCount() = data.size
    }

    private class ViewHolder(val binding: TempItemDragBinding)
        : RecyclerView.ViewHolder(binding.root)

    private class MyCallback(
        private val notDragTint: ColorStateList
    ) : ItemTouchHelper.Callback() {
        private val myLog = MyLog("Callback")
        private var allow = true

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }

        override fun isLongPressDragEnabled() = false

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder is ViewHolder) {
                myLog.d("clearView")
                viewHolder.binding.drag.imageTintList = notDragTint
                allow = false
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
        ): Boolean {
            /*if (viewHolder is ViewHolder) {
                myLog.d("onMove:  current: ${viewHolder.adapterPosition}  target: ${target.adapterPosition}")
            } else {
                myLog.d("onMove: ${viewHolder::class}")
            }*/
            val adapter = recyclerView.adapter
            if (adapter is RecyclerAdapter) {
                adapter.move(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (viewHolder is ViewHolder) {
                myLog.d("onSwiped: ${viewHolder.adapterPosition}")
            } else {
                myLog.d("onSwiped: ${viewHolder::class}")
            }
        }
    }
}