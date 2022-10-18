package com.mingz.bill

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mingz.bill.databinding.ActivityTempBinding
import com.mingz.share.MyLog
import com.mingz.share.ui.ShowText
import com.mingz.share.ui.TextWithUnits

class TempActivity : AppCompatActivity() {
    private val myLog by lazy(LazyThreadSafetyMode.NONE) { MyLog(TempActivity::class) }
    private lateinit var binding: ActivityTempBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTempBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.testBtn.setOnClickListener {
            /*println("###### 全空 ######")
            var subjectOutSet = emptyArray<Subject>()
            var subjectInSet = emptyArray<Subject>()
            var accountSet = emptyArray<Account>()
            var typeSet = emptyArray<Type>()
            parsingDataSet(getDataSetBytes(subjectOutSet, subjectInSet, accountSet, typeSet))
            println()
            println("###### 填充数据 ######")
            subjectOutSet = arrayOf(
                Subject(1, "主类1", arrayOf(
                    Subject(2, "副类1-1"),
                    Subject(3, "副类1-2")
                )),
                Subject(4, "主类2", arrayOf()),
                Subject(5, "主类3", arrayOf(
                    Subject(6, "副类3-1")
                ))
            )
            subjectInSet = arrayOf(
                Subject(1, "主类1", arrayOf(
                    Subject(2, "副类1-1")
                ))
            )
            accountSet = arrayOf(
                Account(1, "账户A", arrayOf(
                    Asset(1, "0.00", "0.02"),
                    Asset(2, "1.00", "3.00")
                )),
                Account(2, "账户B", arrayOf(
                    Asset(1, "5.00", "2.34")
                )),
                Account(3, "账户C", arrayOf(
                    Asset(2, "-8.35", "10.22"),
                    Asset(3, "9.98", "-22.13")
                ))
            )
            typeSet = arrayOf(
                Type(1, "币种1"),
                Type(2, "币种2"),
                Type(3, "币种3")
            )
            parsingDataSet(getDataSetBytes(subjectOutSet, subjectInSet, accountSet, typeSet))*/
        //}
        test()
    }

    private fun test() {
        val a = arrayOf(binding.a1, binding.a2, binding.a3)
        binding.aBtn1.setOnClickListener {
            for (v in a) {
                v.content = "#1: QqWwEeRrTtYyUuIiOoPpAaSsDdFfGgHhJjKkLlZzXxCcVvBbNnMm\n#2\n#3"
            }
        }
        binding.aBtn2.setOnClickListener {
            myLog.v("a1 ~ a3 enable:")
            for (v in a) {
                myLog.v("    ${v.isEnabled}")
                v.toggleStatus()
                v.setOnClickListener { aListener(v) }
            }
            myLog.v("================")
        }
        for (v in a) {
            v.setOnClickListener { aListener(v) }
        }

        val b = arrayOf(binding.b1, binding.b2)
        binding.bBtn.setOnClickListener {
            myLog.v("b1 ~ b2 enable:")
            for (v in b) {
                val enable = v.isEnabled
                myLog.v("    $enable")
                v.isEnabled = !enable
                v.setOnClickListener { bListener(v) }
            }
            myLog.v("================")
        }
        for (v in b) {
            v.setOnClickListener { bListener(v) }
        }

        binding.testBtn.setOnClickListener {
            for (v in a) {
                v.content = ""
            }
            for (v in b) {
                amount = ""
                v.setAmount("")
            }
        }
    }

    val aListener = { v: ShowText ->
        Toast.makeText(this, v.content, Toast.LENGTH_SHORT).show()
    }

    var amount = ""

    val bListener = { v: TextWithUnits ->
        amount += "0"
        v.setAmount(amount)
        //Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            binding.a3.clearFocusWhenParentDispatchDown(ev.x, ev.y)
        }
        return super.dispatchTouchEvent(ev)
    }
}