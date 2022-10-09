package com.mingz.bill

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mingz.bill.databinding.ActivityTempBinding
import com.mingz.data.*

class TempActivity : AppCompatActivity() {
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
        }
    }
}