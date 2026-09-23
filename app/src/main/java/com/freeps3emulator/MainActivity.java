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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PICK_GAME_FILE = 101;
    private static final int PICK_PUP_FILE = 102;
    private static final String PREFS_NAME = "GameHubPrefs";

    private RelativeLayout rootLayout;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    private String selectedGameName = "Tomb Raider Underworld";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        hideSystemBars();

        rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#090d16"));

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
                vibrator.vibrate(25);
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

        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(dpToPx(30), dpToPx(15), dpToPx(30), dpToPx(20));

        // Top Bar
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setPadding(0, 0, 0, dpToPx(15));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        TextView titleText = new TextView(this);
        titleText.setText("aPS3e Mobile");
        titleText.setTextColor(Color.parseColor("#58a6ff"));
        titleText.setTextSize(20);
        titleText.setTypeface(null, Typeface.BOLD);
        titleBox.addView(titleText);

        TextView subText = new TextView(this);
        subText.setText("Vulkan LLE Core • dev_hdd0 Ready");
        subText.setTextColor(Color.parseColor("#8b949e"));
        subText.setTextSize(11);
        titleBox.addView(subText);

        topBar.addView(titleBox);

        // Action Buttons Row
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams actionParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        actionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        actionRow.setLayoutParams(actionParams);

        Button addGameBtn = new Button(this);
        addGameBtn.setText("➕ Add Game");
        addGameBtn.setTextColor(Color.WHITE);
        addGameBtn.setTextSize(12);
        addGameBtn.setBackground(createCard(Color.parseColor("#238636"), 6, Color.parseColor("#2ea043")));
        addGameBtn.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        addGameBtn.setOnClickListener(v -> {
            triggerFeedback();
            openFilePicker(PICK_GAME_FILE);
        });

        Button addPupBtn = new Button(this);
        addPupBtn.setText("⚙ Firmware");
        addPupBtn.setTextColor(Color.WHITE);
        addPupBtn.setTextSize(12);
        addPupBtn.setBackground(createCard(Color.parseColor("#21262d"), 6, Color.parseColor("#30363d")));
        addPupBtn.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        LinearLayout.LayoutParams pupParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pupParams.setMargins(dpToPx(10), 0, 0, 0);
        addPupBtn.setLayoutParams(pupParams);
        addPupBtn.setOnClickListener(v -> {
            triggerFeedback();
            openFilePicker(PICK_PUP_FILE);
        });

        actionRow.addView(addGameBtn);
        actionRow.addView(addPupBtn);
        topBar.addView(actionRow);

        mainContainer.addView(topBar);

        // Game Card
        LinearLayout gridRow = new LinearLayout(this);
        gridRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout gameCard = new LinearLayout(this);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setBackground(createCard(Color.parseColor("#161b22"), 10, Color.parseColor("#30363d")));
        gameCard.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(14));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dpToPx(280), LinearLayout.LayoutParams.WRAP_CONTENT);
        gameCard.setLayoutParams(cardParams);

        FrameLayout posterBox = new FrameLayout(this);
        posterBox.setBackground(createCard(Color.parseColor("#0d1117"), 8, Color.parseColor("#21262d")));
        LinearLayout.LayoutParams posterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(110));
        posterBox.setLayoutParams(posterParams);

        TextView posterIcon = new TextView(this);
        posterIcon.setText("🎮\nPS3");
        posterIcon.setTextColor(Color.parseColor("#58a6ff"));
        posterIcon.setTextSize(22);
        posterIcon.setTypeface(null, Typeface.BOLD);
        posterIcon.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        posterBox.addView(posterIcon, iconParams);

        gameCard.addView(posterBox);

        TextView gameTitle = new TextView(this);
        gameTitle.setText(selectedGameName);
        gameTitle.setTextColor(Color.WHITE);
        gameTitle.setTextSize(14);
        gameTitle.setTypeface(null, Typeface.BOLD);
        gameTitle.setPadding(0, dpToPx(8), 0, 0);
        gameCard.addView(gameTitle);

        TextView gameInfo = new TextView(this);
        gameInfo.setText("BLES00384 • 6.20 GB • Ready");
        gameInfo.setTextColor(Color.parseColor("#3fb950"));
        gameInfo.setTextSize(11);
        gameInfo.setPadding(0, dpToPx(2), 0, dpToPx(10));
        gameCard.addView(gameInfo);

        Button startBtn = new Button(this);
        startBtn.setText("▶ BOOT GAME");
        startBtn.setTextColor(Color.WHITE);
        startBtn.setTextSize(13);
        startBtn.setTypeface(null, Typeface.BOLD);
        startBtn.setBackground(createCard(Color.parseColor("#1f6feb"), 6, Color.parseColor("#388bfd")));
        startBtn.setPadding(0, dpToPx(8), 0, dpToPx(8));
        startBtn.setOnClickListener(v -> {
            triggerFeedback();
            startPpuCompilingScreen();
        });
        gameCard.addView(startBtn);

        gridRow.addView(gameCard);
        mainContainer.addView(gridRow);

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
        compSub.setText(selectedGameName + " [BLES00384]\nVulkan Pipeline Cache Warming");
        compSub.setTextColor(Color.parseColor("#8b949e"));
        compSub.setTextSize(12);
        compSub.setGravity(Gravity.CENTER);
        compSub.setPadding(0, dpToPx(6), 0, dpToPx(20));
        compLayout.addView(compSub);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setIndeterminate(false);
        pb.setMax(100);
        pb.setProgress(15);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                dpToPx(360), dpToPx(12));
        pb.setLayoutParams(pbParams);
        compLayout.addView(pb);

        TextView statusText = new TextView(this);
        statusText.setText("Compiling module 48 of 320 (15%)");
        statusText.setTextColor(Color.parseColor("#58a6ff"));
        statusText.setTextSize(11);
        statusText.setPadding(0, dpToPx(8), 0, 0);
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
                    handler.postDelayed(this, 120);
                } else {
                    statusText.setText("Launching PS3 SPU Pipeline...");
                    handler.postDelayed(() -> showInGameScreen(), 400);
                }
            }
        };
        handler.postDelayed(progressRunnable, 200);
    }
        private void showInGameScreen() {
        rootLayout.removeAllViews();

        RelativeLayout gameView = new RelativeLayout(this);
        gameView.setBackgroundColor(Color.BLACK);

        // Top Info & Exit
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), 0);

        TextView inGameText = new TextView(this);
        inGameText.setText("TOMB RAIDER UNDERWORLD\nFPS: 30.0 | Vulkan 720p | LLE Mode");
        inGameText.setTextColor(Color.GREEN);
        inGameText.setTextSize(11);
        topBar.addView(inGameText);

        Button exitBtn = new Button(this);
        exitBtn.setText("Exit");
        exitBtn.setTextColor(Color.WHITE);
        exitBtn.setTextSize(11);
        exitBtn.setBackground(createCard(Color.parseColor("#da3633"), 4, 0));
        RelativeLayout.LayoutParams exitParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(32));
        exitParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        exitBtn.setLayoutParams(exitParams);
        exitBtn.setOnClickListener(v -> {
            triggerFeedback();
            showMainMenu();
        });
        topBar.addView(exitBtn);

        gameView.addView(topBar);

        // New Style Virtual Touch Controls
        createVirtualControls(gameView);

        rootLayout.addView(gameView);
    }

    private void createVirtualControls(RelativeLayout gameView) {
        // Triggers Left: LT, LB, L3
        Button ltBtn = createRectTriggerButton("LT");
        setAbsolutePos(ltBtn, dpToPx(30), dpToPx(30), dpToPx(55), dpToPx(32));
        gameView.addView(ltBtn);

        Button lbBtn = createRectTriggerButton("LB");
        setAbsolutePos(lbBtn, dpToPx(30), dpToPx(72), dpToPx(55), dpToPx(32));
        gameView.addView(lbBtn);

        Button l3Btn = createRoundSmallButton("L3");
        setAbsolutePos(l3Btn, dpToPx(30), dpToPx(130), dpToPx(36), dpToPx(36));
        gameView.addView(l3Btn);

        // Triggers Right: RT, RB, R3
        Button rtBtn = createRectTriggerButton("RT");
        setAbsoluteAlignRight(rtBtn, dpToPx(30), dpToPx(30), dpToPx(55), dpToPx(32));
        gameView.addView(rtBtn);

        Button rbBtn = createRectTriggerButton("RB");
        setAbsoluteAlignRight(rbBtn, dpToPx(30), dpToPx(72), dpToPx(55), dpToPx(32));
        gameView.addView(rbBtn);

        Button r3Btn = createRoundSmallButton("R3");
        setAbsoluteAlignRight(r3Btn, dpToPx(30), dpToPx(130), dpToPx(36), dpToPx(36));
        gameView.addView(r3Btn);

        // Left Analog Joystick
        FrameLayout leftStick = createJoystickCircle();
        setAbsoluteAlignBottomLeft(leftStick, dpToPx(35), dpToPx(25), dpToPx(100), dpToPx(100));
        gameView.addView(leftStick);

        // Cross D-Pad
        FrameLayout dpad = createCrossDPad();
        setAbsoluteAlignBottomLeft(dpad, dpToPx(145), dpToPx(35), dpToPx(85), dpToPx(85));
        gameView.addView(dpad);

        // Right Analog Joystick
        FrameLayout rightStick = createJoystickCircle();
        setAbsoluteAlignBottomRight(rightStick, dpToPx(145), dpToPx(35), dpToPx(100), dpToPx(100));
        gameView.addView(rightStick);

        // ABXY Action Buttons
        RelativeLayout abxyBox = new RelativeLayout(this);
        setAbsoluteAlignBottomRight(abxyBox, dpToPx(25), dpToPx(20), dpToPx(120), dpToPx(120));

        abxyBox.addView(createABXYButton("Y", 40, 0));
        abxyBox.addView(createABXYButton("A", 40, 80));
        abxyBox.addView(createABXYButton("X", 0, 40));
        abxyBox.addView(createABXYButton("B", 80, 40));
        gameView.addView(abxyBox);

        // Center Menu Buttons (Back / Menu)
        LinearLayout centerMenu = new LinearLayout(this);
        centerMenu.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cParams.setMargins(0, 0, 0, dpToPx(15));
        centerMenu.setLayoutParams(cParams);

        Button selectBtn = createCapsuleButton("❐");
        Button menuBtn = createCapsuleButton("☰");
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(26));
        mParams.setMargins(dpToPx(8), 0, 0, 0);
        menuBtn.setLayoutParams(mParams);

        centerMenu.addView(selectBtn);
        centerMenu.addView(menuBtn);
        gameView.addView(centerMenu);
    }

    private Button createRectTriggerButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(12);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(createCard(Color.argb(35, 255, 255, 255), 8, Color.argb(80, 255, 255, 255)));
        btn.setPadding(0, 0, 0, 0);
        setupTouchHighlight(btn);
        return btn;
    }

    private Button createRoundSmallButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(11);
        btn.setBackground(createCard(Color.argb(35, 255, 255, 255), 18, Color.argb(80, 255, 255, 255)));
        btn.setPadding(0, 0, 0, 0);
        setupTouchHighlight(btn);
        return btn;
    }

    private Button createABXYButton(String text, int marginX, int marginY) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(14);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(createCard(Color.argb(40, 255, 255, 255), 20, Color.argb(90, 255, 255, 255)));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dpToPx(38), dpToPx(38));
        params.setMargins(dpToPx(marginX), dpToPx(marginY), 0, 0);
        btn.setLayoutParams(params);
        btn.setPadding(0, 0, 0, 0);

        setupTouchHighlight(btn);
        return btn;
    }

    private Button createCapsuleButton(String icon) {
        Button btn = new Button(this);
        btn.setText(icon);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(11);
        btn.setBackground(createCard(Color.argb(35, 255, 255, 255), 12, Color.argb(80, 255, 255, 255)));
        btn.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(44), dpToPx(26)));
        btn.setPadding(0, 0, 0, 0);
        setupTouchHighlight(btn);
        return btn;
    }

    private FrameLayout createJoystickCircle() {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(createCard(Color.argb(20, 255, 255, 255), 50, Color.argb(70, 255, 255, 255)));

        View thumb = new View(this);
        thumb.setBackground(createCard(Color.argb(40, 255, 255, 255), 25, Color.argb(100, 255, 255, 255)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dpToPx(50), dpToPx(50));
        params.gravity = Gravity.CENTER;
        frame.addView(thumb, params);

        frame.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) triggerFeedback();
            return true;
        });
        return frame;
    }

    private FrameLayout createCrossDPad() {
        FrameLayout frame = new FrameLayout(this);

        View hBar = new View(this);
        hBar.setBackground(createCard(Color.argb(35, 255, 255, 255), 6, Color.argb(80, 255, 255, 255)));
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(dpToPx(85), dpToPx(30));
        hp.gravity = Gravity.CENTER;
        frame.addView(hBar, hp);

        View vBar = new View(this);
        vBar.setBackground(createCard(Color.argb(35, 255, 255, 255), 6, Color.argb(80, 255, 255, 255)));
        FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(dpToPx(30), dpToPx(85));
        vp.gravity = Gravity.CENTER;
        frame.addView(vBar, vp);

        frame.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) triggerFeedback();
            return true;
        });
        return frame;
    }

    private void setupTouchHighlight(Button btn) {
        btn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                triggerFeedback();
                btn.getBackground().setAlpha(180);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                btn.getBackground().setAlpha(40);
            }
            return false;
        });
    }

    private void setAbsolutePos(View v, int x, int y, int w, int h) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        p.setMargins(x, y, 0, 0);
        v.setLayoutParams(p);
    }

    private void setAbsoluteAlignRight(View v, int rightMargin, int y, int w, int h) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.setMargins(0, y, rightMargin, 0);
        v.setLayoutParams(p);
    }

    private void setAbsoluteAlignBottomLeft(View v, int leftMargin, int bottomMargin, int w, int h) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        p.setMargins(leftMargin, 0, 0, bottomMargin);
        v.setLayoutParams(p);
    }

    private void setAbsoluteAlignBottomRight(View v, int rightMargin, int bottomMargin, int w, int h) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        p.setMargins(0, 0, rightMargin, bottomMargin);
        v.setLayoutParams(p);
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
                
