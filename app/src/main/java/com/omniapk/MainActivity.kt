package com.omniapk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We will likely set content view here later or use a layout
        // setContentView(R.layout.activity_main) 
        // For now, just to compile:
        val textView = android.widget.TextView(this)
        textView.text = "OmniAPK Initialized!"
        setContentView(textView)
    }
}
