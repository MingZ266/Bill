package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordExpenditureBinding
import com.mingz.billing.entities.Billing
import com.mingz.billing.entities.Expenditure
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.StringWithId
import com.mingz.billing.utils.Tools
import java.math.BigDecimal

class RecordExpenditureFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordExpenditureBinding

    private var subject: StringWithId? = null
    private var account: StringWithId? = null
    private lateinit var price: BigDecimal
    private lateinit var originalPrice: BigDecimal
    private lateinit var discount: BigDecimal

    companion object {
        @JvmStatic
        fun newInstance() = RecordExpenditureFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordExpenditureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        binding.subject.setOnClickListener {
            Tools.showSelectSubject(context, binding.subject.getTitle(),
                DataSource.expenditureSubject) {
                subject = it
                binding.subject.setContent(it.content)
            }
        }

        binding.account.setOnClickListener {
            Tools.showSelectPopup(context, binding.account.getTitle(),
                DataSource.accountList,
                if (account != null) account!!.id else -1, {
                    account = it
                    binding.account.setContent(it.content)
                })
        }

        binding.price.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.price.getTitle(),
                binding.price.getAmount()) {
                price = BigDecimal(it)
                originalPrice = price.add(discount)
                binding.price.setAmount(it)
                binding.originalPrice.setAmount(originalPrice)
            }
        }

        binding.originalPrice.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.originalPrice.getTitle(),
                binding.originalPrice.getAmount()) {
                val theOriginalPrice = BigDecimal(it)
                if (theOriginalPrice > discount) {
                    originalPrice = theOriginalPrice
                    price = originalPrice.minus(discount)
                    binding.price.setAmount(price)
                    binding.originalPrice.setAmount(it)
                } else {
                    Tools.showToast(context, "${binding.originalPrice.getTitle()}必须" +
                            "高于${binding.discount.getTitle()}")
                }
            }
        }

        binding.discount.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.discount.getTitle(),
                binding.discount.getAmount()) {
                val theDiscount = BigDecimal(it)
                if (theDiscount < originalPrice) {
                    discount = theDiscount
                    price = originalPrice.minus(discount)
                    binding.price.setAmount(price)
                    binding.discount.setAmount(it)
                } else {
                    Tools.showToast(context, "${binding.discount.getTitle()}必须" +
                            "低于${binding.originalPrice.getTitle()}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        subject = null
        account = null
        price = BigDecimal.ZERO
        originalPrice = BigDecimal.ZERO
        discount = BigDecimal.ZERO
        DataSource.checkedPosition = -1
        binding.time.updateToNowTime()
    }

    override fun getTitle(): String = "支出"

    override fun save() {
        val context = context ?: return
        if (subject == null) {
            Tools.showToast(context, "请选择${binding.subject.getTitle()}")
        } else if (account == null) {
            Tools.showToast(context, "请选择${binding.account.getTitle()}")
        } else if (price <= BigDecimal.ZERO) {
            Tools.showToast(context, "${binding.price.getTitle()}必须大于零")
        } else {
            val format = "%.2f"
            val expenditure = Expenditure(
                binding.time.getTimestamp(),
                subject!!,
                account!!,
                binding.price.getType(),
                String.format(format, price),
                String.format(format, originalPrice),
                String.format(format, discount),
                binding.remarks.getContent()
            )
            if (Billing.saveBilling(binding.time.year, binding.time.month, expenditure)) {
                Tools.showToast(context, "已保存")
                activity?.onBackPressed()
            } else {
                Tools.showToast(context, "保存失败")
            }
        }
    }
}