package com.mingz.bill

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mingz.bill.databinding.ActivityTempBinding
import com.mingz.share.MyLog

class TempActivity : AppCompatActivity() {
    private val myLog by lazy(LazyThreadSafetyMode.NONE) { MyLog(TempActivity::class) }
    private lateinit var binding: ActivityTempBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTempBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testBtn.setOnClickListener {
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
            val content = "#1  qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM\n#2\n#3"
            binding.test1.text = content
            binding.test2.text = content
            binding.test3.text = content
            binding.test4.text = content
        }
        binding.test1.setOnClickListener {
            Toast.makeText(this, "T-T: ${binding.test1.text}", Toast.LENGTH_SHORT).show()
        }
        binding.test2.setOnClickListener {
            Toast.makeText(this, "T-F: ${binding.test2.text}", Toast.LENGTH_SHORT).show()
        }
        binding.test3.setOnClickListener {
            Toast.makeText(this, "F-T: ${binding.test3.text}", Toast.LENGTH_SHORT).show()
        }
        binding.test4.setOnClickListener {
            Toast.makeText(this, "F-F: ${binding.test4.text}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            val x = ev.x
            val y = ev.y
            binding.test1.clearFocusWhenParentDispatchDown(x, y)
            binding.test2.clearFocusWhenParentDispatchDown(x, y)
            binding.test3.clearFocusWhenParentDispatchDown(x, y)
            binding.test4.clearFocusWhenParentDispatchDown(x, y)
        }
        return super.dispatchTouchEvent(ev)
    }
}