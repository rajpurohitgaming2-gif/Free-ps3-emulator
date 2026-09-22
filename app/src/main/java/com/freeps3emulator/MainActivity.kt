package com.freeps3emulator

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this)
        textView.text = "Free PS3 Emulator\nApp Started Successfully!"
        textView.textSize = 22f
        textView.gravity = Gravity.CENTER
        
        setContentView(textView)
    }
}

