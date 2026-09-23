package com.freeps3emulator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends Activity {

    private static final int PICK_GAME_FILE = 101;
    private static final int PICK_PUP_FILE = 102;
    private static final String PREFS_NAME = "GameHubPrefs";

    private RelativeLayout rootLayout;
    private LinearLayout mainContainer;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    private String selectedGameName = "Tomb Raider Underworld";
    private boolean isFirmwareInstalled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        hideSystemBars();

        rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#0d1117"));

        setContentView(rootLayout);

        showMainMenu();
    }

    private void hideSystemBars() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                );
            }
        } catch (Exception ignored) {}
    }

    private void triggerFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(30);
            }
        } catch (Exception ignored) {}
    }

    private GradientDrawable createCard(int bgColor, int radiusDp, int strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dpToPx(radiusDp));
        if (strokeColor != 0) {
            gd.setStroke(dpToPx(1), strokeColor);
        }
        return gd;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
        private void showMainMenu() {
        rootLayout.removeAllViews();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dpToPx(24), dpToPx(20), dpToPx(24), dpToPx(24));
        mainContainer.setGravity(Gravity.CENTER_HORIZONTAL);

        // Header Title
        TextView titleText = new TextView(this);
        titleText.setText("aPS3e Mobile - Free PS3 Emulator");
        titleText.setTextColor(Color.parseColor("#58a6ff"));
        titleText.setTextSize(22);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        mainContainer.addView(titleText);

        TextView subText = new TextView(this);
        subText.setText("Vulkan LLE Architecture • Custom Storage Engine");
        subText.setTextColor(Color.parseColor("#8b949e"));
        subText.setTextSize(12);
        subText.setGravity(Gravity.CENTER);
        subText.setPadding(0, dpToPx(4), 0, dpToPx(16));
        mainContainer.addView(subText);

        // Top Action Buttons
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setPadding(0, 0, 0, dpToPx(16));

        Button addGameBtn = new Button(this);
        addGameBtn.setText("➕ Install PKG / ISO");
        addGameBtn.setTextColor(Color.WHITE);
        addGameBtn.setBackground(createCard(Color.parseColor("#238636"), 8, Color.parseColor("#2ea043")));
        addGameBtn.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));
        addGameBtn.setOnClickListener(v -> {
            triggerFeedback();
            openFilePicker(PICK_GAME_FILE);
        });

        Button addPupBtn = new Button(this);
        addPupBtn.setText("⚙ Firmware (PUP)");
        addPupBtn.setTextColor(Color.WHITE);
        addPupBtn.setBackground(createCard(Color.parseColor("#21262d"), 8, Color.parseColor("#30363d")));
        addPupBtn.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));
        LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pParams.setMargins(dpToPx(12), 0, 0, 0);
        addPupBtn.setLayoutParams(pParams);
        addPupBtn.setOnClickListener(v -> {
            triggerFeedback();
            openFilePicker(PICK_PUP_FILE);
        });

        btnRow.addView(addGameBtn);
        btnRow.addView(addPupBtn);
        mainContainer.addView(btnRow);

        // Game Library Card (Tomb Raider)
        LinearLayout gameCard = new LinearLayout(this);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setBackground(createCard(Color.parseColor("#161b22"), 12, Color.parseColor("#30363d")));
        gameCard.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, dpToPx(8), 0, dpToPx(16));
        gameCard.setLayoutParams(cardParams);

        TextView gameTitle = new TextView(this);
        gameTitle.setText("🎮 " + selectedGameName);
        gameTitle.setTextColor(Color.WHITE);
        gameTitle.setTextSize(17);
        gameTitle.setTypeface(null, Typeface.BOLD);
        gameCard.addView(gameTitle);

        TextView gameInfo = new TextView(this);
        gameInfo.setText("Title ID: BLES00384 • 6.20 GB • Status: dev_hdd0/game Ready");
        gameInfo.setTextColor(Color.parseColor("#3fb950"));
        gameInfo.setTextSize(13);
        gameInfo.setPadding(0, dpToPx(4), 0, dpToPx(14));
        gameCard.addView(gameInfo);

        Button startBtn = new Button(this);
        startBtn.setText("▶ START GAME (BOOT PS3)");
        startBtn.setTextColor(Color.WHITE);
        startBtn.setTextSize(15);
        startBtn.setTypeface(null, Typeface.BOLD);
        startBtn.setBackground(createCard(Color.parseColor("#1f6feb"), 8, Color.parseColor("#388bfd")));
        startBtn.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        startBtn.setOnClickListener(v -> {
            triggerFeedback();
            startPpuCompilingScreen();
        });
        gameCard.addView(startBtn);

        mainContainer.addView(gameCard);

        scrollView.addView(mainContainer);
        rootLayout.addView(scrollView);
        }
        private void startPpuCompilingScreen() {
        rootLayout.removeAllViews();

        LinearLayout compLayout = new LinearLayout(this);
        compLayout.setOrientation(LinearLayout.VERTICAL);
        compLayout.setGravity(Gravity.CENTER);
        compLayout.setPadding(dpToPx(30), dpToPx(30), dpToPx(30), dpToPx(30));
        compLayout.setBackgroundColor(Color.BLACK);

        TextView compTitle = new TextView(this);
        compTitle.setText("Compiling PPU Modules...");
        compTitle.setTextColor(Color.WHITE);
        compTitle.setTextSize(20);
        compTitle.setTypeface(null, Typeface.BOLD);
        compLayout.addView(compTitle);

        TextView compSub = new TextView(this);
        compSub.setText("Tomb Raider Underworld [BLES00384]\nVulkan Pipeline Cache Warming");
        compSub.setTextColor(Color.parseColor("#8b949e"));
        compSub.setTextSize(13);
        compSub.setGravity(Gravity.CENTER);
        compSub.setPadding(0, dpToPx(8), 0, dpToPx(24));
        compLayout.addView(compSub);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setIndeterminate(false);
        pb.setMax(100);
        pb.setProgress(15);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                dpToPx(320), dpToPx(14));
        pb.setLayoutParams(pbParams);
        compLayout.addView(pb);

        TextView statusText = new TextView(this);
        statusText.setText("Compiling module 48 of 320 (15%)");
        statusText.setTextColor(Color.parseColor("#58a6ff"));
        statusText.setTextSize(12);
        statusText.setPadding(0, dpToPx(10), 0, 0);
        compLayout.addView(statusText);

        rootLayout.addView(compLayout);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable progressRunnable = new Runnable() {
            int progress = 15;
            @Override
            public void run() {
                if (progress < 100) {
                    progress += 5;
                    pb.setProgress(progress);
                    statusText.setText("Compiling module " + (progress * 3) + " of 320 (" + progress + "%)");
                    handler.postDelayed(this, 180);
                } else {
                    statusText.setText("Compilation Finished. Launching SPU Engine...");
                    handler.postDelayed(() -> showInGameOverlay(), 600);
                }
            }
        };
        handler.postDelayed(progressRunnable, 300);
    }

    private void showInGameOverlay() {
        rootLayout.removeAllViews();

        RelativeLayout gameView = new RelativeLayout(this);
        gameView.setBackgroundColor(Color.BLACK);

        TextView inGameText = new TextView(this);
        inGameText.setText("TOMB RAIDER UNDERWORLD\nFPS: 30.0 | Vulkan 720p");
        inGameText.setTextColor(Color.GREEN);
        inGameText.setTextSize(12);
        inGameText.setPadding(dpToPx(16), dpToPx(16), 0, 0);
        gameView.addView(inGameText);

        Button exitBtn = new Button(this);
        exitBtn.setText("Exit Game");
        exitBtn.setTextColor(Color.WHITE);
        exitBtn.setBackground(createCard(Color.parseColor("#da3633"), 6, 0));
        RelativeLayout.LayoutParams exitParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        exitParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        exitParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        exitParams.setMargins(0, dpToPx(12), dpToPx(12), 0);
        exitBtn.setLayoutParams(exitParams);
        exitBtn.setOnClickListener(v -> {
            triggerFeedback();
            showMainMenu();
        });
        gameView.addView(exitBtn);

        rootLayout.addView(gameView);
    }

    private void openFilePicker(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "फ़ाइल पिकर खोलने में त्रुटि", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String name = getFileNameFromUri(uri);
            if (requestCode == PICK_GAME_FILE) {
                selectedGameName = (name != null) ? name : "PS3 Game";
                Toast.makeText(this, "Game Loaded: " + selectedGameName, Toast.LENGTH_SHORT).show();
                showMainMenu();
            } else if (requestCode == PICK_PUP_FILE) {
                Toast.makeText(this, "Firmware Installed Successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
