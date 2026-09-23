package com.freeps3emulator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_GAME_FILE = 101;
    private TextView statusText;
    private Button startButton;
    private String selectedGamePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainMenu();
    }

    private void showMainMenu() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFF121212);
        layout.setPadding(40, 40, 40, 40);

        TextView titleText = new TextView(this);
        titleText.setText("FREE PS3 EMULATOR");
        titleText.setTextSize(26);
        titleText.setTextColor(0xFF00E5FF);
        titleText.setGravity(Gravity.CENTER);
        layout.addView(titleText);

        statusText = new TextView(this);
        statusText.setText(selectedGamePath == null ? "\nNo game loaded yet.\n" : "\nSelected Game:\n" + selectedGamePath + "\n");
        statusText.setTextSize(16);
        statusText.setTextColor(selectedGamePath == null ? 0xFFB0B0B0 : 0xFF76FF03);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        Button loadButton = new Button(this);
        loadButton.setText("SELECT GAME (ISO / PKG)");
        loadButton.setTextSize(16);
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

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, 30));
        layout.addView(spacer);

        startButton = new Button(this);
        startButton.setText("START GAME");
        startButton.setTextSize(16);
        startButton.setTextColor(0xFFFFFFFF);
        startButton.setBackgroundColor(0xFF2E7D32);
        startButton.setPadding(30, 20, 30, 20);
        startButton.setVisibility(selectedGamePath == null ? View.GONE : View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGameScreen();
            }
        });
        layout.addView(startButton);

        setContentView(layout);
    }

    private void showGameScreen() {
        RelativeLayout gameLayout = new RelativeLayout(this);
        gameLayout.setBackgroundColor(Color.BLACK);

        TextView screenView = new TextView(this);
        screenView.setText("PS3 Running...\n" + (selectedGamePath != null ? selectedGamePath : ""));
        screenView.setTextColor(Color.WHITE);
        screenView.setTextSize(18);
        screenView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams screenParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        gameLayout.addView(screenView, screenParams);

        LinearLayout dpadLayout = new LinearLayout(this);
        dpadLayout.setOrientation(LinearLayout.VERTICAL);
        dpadLayout.setGravity(Gravity.CENTER);

        Button btnUp = makeControlButton("▲");
        Button btnDown = makeControlButton("▼");
        Button btnLeft = makeControlButton("◀");
        Button btnRight = makeControlButton("▶");

        LinearLayout midDpad = new LinearLayout(this);
        midDpad.setOrientation(LinearLayout.HORIZONTAL);
        midDpad.addView(btnLeft);
        midDpad.addView(btnRight);

        dpadLayout.addView(btnUp);
        dpadLayout.addView(midDpad);
        dpadLayout.addView(btnDown);

        RelativeLayout.LayoutParams dpadParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dpadParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        dpadParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dpadParams.leftMargin = 40;
        dpadParams.bottomMargin = 40;
        gameLayout.addView(dpadLayout, dpadParams);

        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setOrientation(LinearLayout.VERTICAL);
        actionLayout.setGravity(Gravity.CENTER);

        Button btnTriangle = makeControlButton("△");
        Button btnCross = makeControlButton("✕");
        Button btnSquare = makeControlButton("◻");
        Button btnCircle = makeControlButton("○");

        LinearLayout midAction = new LinearLayout(this);
        midAction.setOrientation(LinearLayout.HORIZONTAL);
        midAction.addView(btnSquare);
        midAction.addView(btnCircle);

        actionLayout.addView(btnTriangle);
        actionLayout.addView(midAction);
        actionLayout.addView(btnCross);

        RelativeLayout.LayoutParams actionParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        actionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        actionParams.rightMargin = 40;
        actionParams.bottomMargin = 40;
        gameLayout.addView(actionLayout, actionParams);

        Button btnBack = new Button(this);
        btnBack.setText("EXIT");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setBackgroundColor(0x88333333);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
        RelativeLayout.LayoutParams backParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        backParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        backParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        backParams.leftMargin = 20;
        backParams.topMargin = 20;
        gameLayout.addView(btnBack, backParams);

        setContentView(gameLayout);
    }

    private Button makeControlButton(final String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(18);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(0x66FFFFFF);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, label + " Pressed", Toast.LENGTH_SHORT).show();
            }
        });
        return btn;
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
                selectedGamePath = uri.getLastPathSegment();
                statusText.setText("\nSelected Game:\n" + selectedGamePath + "\n");
                statusText.setTextColor(0xFF76FF03);
                if (startButton != null) {
                    startButton.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Game loaded! Tap START GAME", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
