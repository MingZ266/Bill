package com.mingz.billing

import org.junit.Test
import java.math.BigDecimal

class UnitTest {
    @Test
    fun test() {
        val num = BigDecimal("-2.3")
        val format = String.format("%.2f", num)
        println(num)
        println(format)
    }
}