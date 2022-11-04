package com.mingz.billdetails

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.mingz.billdetails.databinding.LayoutControlBillBinding
import com.mingz.data.bill.Bill
import com.mingz.share.DialogPack
import com.mingz.share.MyLog
import com.mingz.share.databinding.DialogInputAmountBinding
import com.mingz.share.showToast

/**
 * 是否保存了数据.
 */
private const val KEY_SAVED_BOOL = "saved"

/**
 * 保存账单数据的键.
 */
private const val KEY_BILL = "bill"

/**
 * 保存[BillFragment]工作模式的键.
 */
private const val KEY_MODE_BOOL = "mode"

/**
 * 账单详情页，可在此添加、删除、查看、修改账单.
 */
abstract class BillFragment : Fragment() {
    /**
     * 清除数据.
     *
     *在“添加账单”模式下，使用“add + hide + show”方式切换[Fragment]时，用于在“hide”操作后：
     *
     * 清理持有的数据.
     */
    abstract fun clearDataAfterHide()

    /**
     * 初始化视图中的数据.
     *
     * 调用前需先调用[initMenuBinding].
     *
     * 在“添加账单”模式下，使用“add + hide + show”方式切换[Fragment]时，用于在“show”操作前：
     *
     * 初始化[BillFragment]视图上的数据.
     */
    abstract fun initViewBeforeShow()

    /**
     * 设置[BillFragmentImpl.menuBinding].
     */
    abstract fun initMenuBinding(binding: LayoutControlBillBinding)

    /**
     * 当根视图分配ACTION_DOWN事件时调用.
     */
    open fun onDispatchActionDown(x: Float, y: Float) {}
}

/**
 * [BillFragment]实现类.
 *
 * 在屏幕旋转、Home后内存紧张等原因销毁[BillFragment]时，能够保存和恢复数据.
 *
 * 当该[BillFragment]隐藏时，将不会保存和恢复数据.
 */
abstract class BillFragmentImpl<BILL> : BillFragment() where BILL : Bill, BILL : Parcelable {
    /**
     * 日志.
     */
    protected val myLog by lazy(LazyThreadSafetyMode.NONE) { createMyLog() }

    /**
     * 账单数据.
     */
    protected var bill: BILL? = null

    /**
     * 该[BillFragment]的工作模式.
     *
     * - null: 添加账单
     * - true: 查看账单
     * - false: 修改账单
     */
    protected var mode: Boolean? = null
        private set

    /**
     * 操作菜单.
     */
    private var menuBinding: LayoutControlBillBinding? = null

    /**
     * 数额输入弹窗.
     */
    private var mInputAmountDialog: DialogPack<DialogInputAmountBinding>? = null

    protected companion object {
        @JvmField
        /**
         * 添加账单模式.
         */
        val MODE_ADD: Boolean? = null

        /**
         * 查看账单模式.
         */
        const val MODE_CHECK = true

        /**
         * 修改账单模式.
         */
        const val MODE_MODIFY = false
    }

    final override fun clearDataAfterHide() {
        bill = null
        mode = null
        menuBinding = null
        mInputAmountDialog = null
    }

    final override fun initViewBeforeShow() {
        bill = null
        mode = null
        initFromBill()
        initView()
        initMenuItem()
    }

    final override fun initMenuBinding(binding: LayoutControlBillBinding) {
        menuBinding = binding
    }

    /**
     * 构造[myLog].
     */
    protected abstract fun createMyLog(): MyLog

    /**
     * 在生成新的[BillFragment]实例时调用.
     * @param bill 账单数据实体
     * @param mode 工作模式，参见[BillFragmentImpl.mode]
     */
    protected fun initArguments(bill: BILL?, mode: Boolean?) {
        if (mode == null) { // 添加账单，忽略账单数据
            this.bill = null
        } else { // 查看账单或修改账单，账单数据不应为空
            if (bill == null) {
                throw IllegalArgumentException("要求查看或修改账单，但账单数据为空")
            }
            this.bill = bill
        }
        this.mode = mode
        initFromBill()
    }

    /**
     * 当账单数据更新时调用.
     *
     * 用于从[bill]中构建页面数据.
     */
    protected abstract fun initFromBill()

    /**
     * 将页面数据填充到视图中.
     */
    protected abstract fun initView()

    /**
     * 在屏幕旋转等原因保存数据时调用以更新[bill].
     */
    protected abstract fun updateBill()

    /**
     * 当因屏幕旋转等原因保存数据时调用.
     * @param outState 用来保存额外的数据以在[onRestoreData]中恢复
     */
    @CallSuper
    protected open fun onSaveData(outState: Bundle) {
        updateBill()
    }

    /**
     * 当恢复数据时调用.
     * @param savedInstanceState 用来读取额外保存的数据
     */
    protected open fun onRestoreData(savedInstanceState: Bundle) {}

    /**
     * 根据[mode]更新菜单项图标.
     *
     * 菜单项item1、item2操作含义如下：
     * - 查看账单: 修改账单、删除账单
     * - 修改账单: 选择币种、保存
     * - 添加账单: 选择币种、保存
     */
    private fun initMenuItem() {
        val menuBinding = menuBinding ?: return
        if (mode == MODE_ADD || mode == MODE_MODIFY) { // 添加账单或修改账单
            menuBinding.item1.setImageResource(R.drawable.ic_type)
            menuBinding.item1.setOnClickListener { // 选择币种
                context?.showToast("选择币种")
            }
            menuBinding.item2.setImageResource(R.drawable.ic_save)
            menuBinding.item2.setOnClickListener { // 保存
                context?.showToast("保存")
            }
        } else { // 查看账单
            menuBinding.item1.setImageResource(R.drawable.ic_edit)
            menuBinding.item1.setOnClickListener { // 修改账单
                context?.showToast("修改账单")
            }
            menuBinding.item2.setImageResource(R.drawable.ic_delete)
            menuBinding.item2.setOnClickListener { // 删除账单
                context?.showToast("删除账单")
            }
        }
    }

    /**
     * 为[views]中的每一个元素设置[enabled].
     */
    protected fun setEnabled(enabled: Boolean, vararg views: View) {
        for (v in views) {
            v.isEnabled = enabled
        }
    }

    /**
     * 获取数额输入弹窗.
     */
    protected val inputAmountDialog: DialogPack<DialogInputAmountBinding>
        get() {
            if (mInputAmountDialog == null) {
                val context = context!!
                mInputAmountDialog = DialogPack(context, object : DialogPack.Creator<DialogInputAmountBinding> {
                    override fun createBinding() = DialogInputAmountBinding.inflate(LayoutInflater.from(context))
                }, true)
            }
            return mInputAmountDialog!!
        }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        myLog.v("onSaveInstanceState: isHidden = $isHidden")
        if (!isHidden) { // 保存数据
            onSaveData(outState) // 在数据保存前调用，避免覆盖
            outState.putBoolean(KEY_SAVED_BOOL, true) // 标记已保存数据
            myLog.v("保存数据: $mode - $bill")
            outState.putParcelable(KEY_BILL, bill)
            if (mode == null) {
                outState.putString(KEY_MODE_BOOL, null)
            } else {
                outState.putBoolean(KEY_MODE_BOOL, mode!!)
            }
        }
    }

    // 在系统恢复视图数据后恢复或填充视图数据，以避免系统恢复的数据造成覆盖
    @CallSuper
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        myLog.v("onViewStateRestored: isHidden = $isHidden")
        if (!isHidden) { // 恢复数据
            if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_SAVED_BOOL, false)) {
                savedInstanceState.putBoolean(KEY_SAVED_BOOL, false) // 移除已保存标记
                bill = savedInstanceState.getParcelable(KEY_BILL)
                mode = savedInstanceState.get(KEY_MODE_BOOL) as Boolean?
                myLog.v("读取账单数据: $mode - $bill")
                onRestoreData(savedInstanceState) // 在数据恢复后调用，避免修改
                initFromBill()
            }
            initView()
            initMenuItem()
        }
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        menuBinding = null
        mInputAmountDialog = null
    }
}