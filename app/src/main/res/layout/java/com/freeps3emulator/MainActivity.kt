package com.freeps3emulator

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private var selected: Uri? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        findViewById<Button>(R.id.selectButton).setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(i, 10)
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (selected == null) {
                Toast.makeText(this, "पहले PS3 file चुनें।", Toast.LENGTH_SHORT).show()
            } else {
                status.text = "Emulator foundation started. CPU/SPU/RSX core is next."
            }
        }
    }

    @Deprecated("Android callback API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10 && resultCode == RESULT_OK) {
            selected = data?.data
            status.text = "File selected: ${selected?.lastPathSegment ?: "selected"}"
        }
    }
}
