package com.mingz.billing.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.databinding.ActivityRecordBinding

class RecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}