package com.freeps3emulator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_GAME_FILE = 101;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // मुख्य लेआउट
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFF121212);
        layout.setPadding(40, 40, 40, 40);

        // टाइटल
        TextView titleText = new TextView(this);
        titleText.setText("FREE PS3 EMULATOR");
        titleText.setTextSize(26);
        titleText.setTextColor(0xFF00E5FF);
        titleText.setGravity(Gravity.CENTER);
        layout.addView(titleText);

        // स्टेटस टेक्स्ट
        statusText = new TextView(this);
        statusText.setText("\nNo game loaded yet.\n");
        statusText.setTextSize(16);
        statusText.setTextColor(0xFFB0B0B0);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        // लोड गेम बटन
        Button loadButton = new Button(this);
        loadButton.setText("SELECT GAME (ISO / PKG)");
        loadButton.setTextSize(18);
        loadButton.setTextColor(0xFFFFFFFF);
        loadButton.setBackgroundColor(0xFF1E88E5);
        loadButton.setPadding(30, 20, 30, 20);

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        layout.addView(loadButton);
        setContentView(layout);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_GAME_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_GAME_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = uri.getLastPathSegment();
                statusText.setText("\nSelected Game:\n" + path + "\n");
                statusText.setTextColor(0xFF76FF03);
                Toast.makeText(this, "Game loaded successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
