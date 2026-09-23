package com.freeps3emulator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
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
        layout.setBackgroundColor(0xFF0F0F14);
        layout.setPadding(40, 40, 40, 40);

        TextView titleText = new TextView(this);
        titleText.setText("FREE PS3 EMULATOR");
        titleText.setTextSize(26);
        titleText.setTextColor(0xFF00E5FF);
        titleText.setGravity(Gravity.CENTER);
        layout.addView(titleText);

        statusText = new TextView(this);
        statusText.setText(selectedGamePath == null ? "\nNo game loaded yet.\n" : "\nSelected Game:\n" + selectedGamePath + "\n");
        statusText.setTextSize(15);
        statusText.setTextColor(selectedGamePath == null ? 0xFF888888 : 0xFF00E676);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        Button loadButton = new Button(this);
        loadButton.setText("SELECT GAME (ISO / PKG)");
        loadButton.setTextSize(16);
        loadButton.setTextColor(Color.WHITE);
        loadButton.setBackground(createRoundBackground(0xFF1E88E5, 20, 0, 0));
        loadButton.setPadding(40, 20, 40, 20);
        loadButton.setOnClickListener(v -> openFilePicker());
        layout.addView(loadButton);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, 30));
        layout.addView(spacer);

        startButton = new Button(this);
        startButton.setText("START GAME");
        startButton.setTextSize(16);
        startButton.setTextColor(Color.WHITE);
        startButton.setBackground(createRoundBackground(0xFF00C853, 20, 0, 0));
        startButton.setPadding(40, 20, 40, 20);
        startButton.setVisibility(selectedGamePath == null ? View.GONE : View.VISIBLE);
        startButton.setOnClickListener(v -> showGameHubScreen());
        layout.addView(startButton);

        setContentView(layout);
    }

    private void showGameHubScreen() {
        RelativeLayout gameLayout = new RelativeLayout(this);
        gameLayout.setBackgroundColor(0xFF050508);

        // बीच में गेम स्क्रीन
        TextView screenView = new TextView(this);
        screenView.setText("GAME RUNNING\n" + (selectedGamePath != null ? selectedGamePath : ""));
        screenView.setTextColor(0xFF333333);
        screenView.setTextSize(16);
        screenView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams screenParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        gameLayout.addView(screenView, screenParams);

        // --- TOP SHOULDER BUTTONS (L1, L2, R1, R2, EXIT) ---
        // L1 & L2 (Left Top)
        LinearLayout leftShoulder = new LinearLayout(this);
        leftShoulder.setOrientation(LinearLayout.HORIZONTAL);
        leftShoulder.addView(createShoulderButton("L2"));
        View spL = new View(this);
        spL.setLayoutParams(new LinearLayout.LayoutParams(15, 1));
        leftShoulder.addView(spL);
        leftShoulder.addView(createShoulderButton("L1"));

        RelativeLayout.LayoutParams lsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lsParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lsParams.leftMargin = 40;
        lsParams.topMargin = 30;
        gameLayout.addView(leftShoulder, lsParams);

        // R1 & R2 (Right Top)
        LinearLayout rightShoulder = new LinearLayout(this);
        rightShoulder.setOrientation(LinearLayout.HORIZONTAL);
        rightShoulder.addView(createShoulderButton("R1"));
        View spR = new View(this);
        spR.setLayoutParams(new LinearLayout.LayoutParams(15, 1));
        rightShoulder.addView(spR);
        rightShoulder.addView(createShoulderButton("R2"));

        RelativeLayout.LayoutParams rsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rsParams.rightMargin = 40;
        rsParams.topMargin = 30;
        gameLayout.addView(rightShoulder, rsParams);

        // Exit / Menu (Center Top)
        Button btnExit = createCapsuleButton("MENU", 0x33FFFFFF);
        btnExit.setOnClickListener(v -> showMainMenu());
        RelativeLayout.LayoutParams exitParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        exitParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        exitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        exitParams.topMargin = 30;
        gameLayout.addView(btnExit, exitParams);

        // --- LEFT D-PAD (GameHub Cross Style) ---
        RelativeLayout dpadLayout = new RelativeLayout(this);
        int btnSize = dpToPx(55);

        Button up = createCircularButton("▲", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);
        Button down = createCircularButton("▼", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);
        Button left = createCircularButton("◀", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);
        Button right = createCircularButton("▶", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);

        up.setId(View.generateViewId());
        down.setId(View.generateViewId());
        left.setId(View.generateViewId());
        right.setId(View.generateViewId());

        RelativeLayout.LayoutParams pUp = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pUp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pUp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        RelativeLayout.LayoutParams pDown = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pDown.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pDown.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        RelativeLayout.LayoutParams pLeft = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pLeft.addRule(RelativeLayout.CENTER_VERTICAL);
        pLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams pRight = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pRight.addRule(RelativeLayout.CENTER_VERTICAL);
        pRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        int dpadBoxSize = dpToPx(160);
        RelativeLayout.LayoutParams dpadBoxParams = new RelativeLayout.LayoutParams(dpadBoxSize, dpadBoxSize);
        dpadBoxParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        dpadBoxParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dpadBoxParams.leftMargin = dpToPx(30);
        dpadBoxParams.bottomMargin = dpToPx(30);

        dpadLayout.addView(up, pUp);
        dpadLayout.addView(down, pDown);
        dpadLayout.addView(left, pLeft);
        dpadLayout.addView(right, pRight);
        gameLayout.addView(dpadLayout, dpadBoxParams);

        // --- RIGHT ACTION BUTTONS (PS Symbols: △, ○, ✕, ◻) ---
        RelativeLayout actionLayout = new RelativeLayout(this);

        Button triangle = createCircularButton("△", 0x2A00E676, 0x8800E676, 0xFF00E676, btnSize); // Green
        Button circle = createCircularButton("○", 0x2AFF1744, 0x88FF1744, 0xFFFF1744, btnSize);   // Red
        Button cross = createCircularButton("✕", 0x2A2979FF, 0x882979FF, 0xFF2979FF, btnSize);    // Blue
        Button square = createCircularButton("◻", 0x2AF50057, 0x88F50057, 0xFFF50057, btnSize);   // Pink

        triangle.setId(View.generateViewId());
        circle.setId(View.generateViewId());
        cross.setId(View.generateViewId());
        square.setId(View.generateViewId());

        RelativeLayout.LayoutParams pTri = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pTri.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pTri.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        RelativeLayout.LayoutParams pCross = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pCross.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pCross.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        RelativeLayout.LayoutParams pSq = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pSq.addRule(RelativeLayout.CENTER_VERTICAL);
        pSq.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams pCir = new RelativeLayout.LayoutParams(btnSize, btnSize);
        pCir.addRule(RelativeLayout.CENTER_VERTICAL);
        pCir.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams actionBoxParams = new RelativeLayout.LayoutParams(dpadBoxSize, dpadBoxSize);
        actionBoxParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actionBoxParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        actionBoxParams.rightMargin = dpToPx(30);
        actionBoxParams.bottomMargin = dpToPx(30);

        actionLayout.addView(triangle, pTri);
        actionLayout.addView(cross, pCross);
        actionLayout.addView(square, pSq);
        actionLayout.addView(circle, pCir);
        gameLayout.addView(actionLayout, actionBoxParams);

        // --- CENTER SELECT & START BUTTONS ---
        LinearLayout centerPills = new LinearLayout(this);
        centerPills.setOrientation(LinearLayout.HORIZONTAL);
        centerPills.addView(createCapsuleButton("SELECT", 0x2AFFFFFF));
        View spC = new View(this);
        spC.setLayoutParams(new LinearLayout.LayoutParams(25, 1));
        centerPills.addView(spC);
        centerPills.addView(createCapsuleButton("START", 0x2AFFFFFF));

        RelativeLayout.LayoutParams cpParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cpParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cpParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cpParams.bottomMargin = dpToPx(35);
        gameLayout.addView(centerPills, cpParams);

        setContentView(gameLayout);
    }

    private Button createCircularButton(String label, int bgColor, int strokeColor, int textColor, int size) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(20);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(createRoundBackground(bgColor, size / 2, strokeColor, 2));
        btn.setOnClickListener(v -> Toast.makeText(this, label + " Pressed", Toast.LENGTH_SHORT).show());
        return btn;
    }

    private Button createShoulderButton(String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(14);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(createRoundBackground(0x2AFFFFFF, 12, 0x44FFFFFF, 2));
        btn.setPadding(35, 15, 35, 15);
        btn.setOnClickListener(v -> Toast.makeText(this, label + " Pressed", Toast.LENGTH_SHORT).show());
        return btn;
    }

    private Button createCapsuleButton(String label, int bgColor) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(12);
        btn.setTextColor(0xFFCCCCCC);
        btn.setBackground(createRoundBackground(bgColor, 30, 0x33FFFFFF, 1));
        btn.setPadding(30, 10, 30, 10);
        btn.setOnClickListener(v -> Toast.makeText(this, label + " Pressed", Toast.LENGTH_SHORT).show());
        return btn;
    }

    private GradientDrawable createRoundBackground(int color, int radiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(dpToPx(radiusDp));
        if (strokeWidthDp > 0) {
            gd.setStroke(dpToPx(strokeWidthDp), strokeColor);
        }
        return gd;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
                statusText.setTextColor(0xFF00E676);
                if (startButton != null) {
                    startButton.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Game loaded! Tap START GAME", Toast.LENGTH_SHORT).show();
            }
        }
    }
            }
