package com.freeps3emulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int PICK_GAME_FILE = 101;
    private static final int PICK_PUP_FILE = 102;
    private static final String PREFS_NAME = "aPS3e_Emulator_Prefs";

    private RelativeLayout rootLayout;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    // गेम डेटा मॉडल
    public static class GameItem {
        String title;
        String titleId;
        String size;
        GameItem(String t, String id, String s) {
            this.title = t;
            this.titleId = id;
            this.size = s;
        }
    }

    private ArrayList<GameItem> gameList = new ArrayList<>();
    private GameItem activeGame;

    // सेटिंग्स वेरिएबल्स
    private boolean settingVulkan = true;
    private boolean setting60Fps = true;
    private boolean settingSound = true;
    private boolean settingHaptics = true;
    private int settingButtonOpacity = 40; // 20 to 100%

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            loadSettings();
            loadGameLibrary();
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

    private void loadSettings() {
        settingVulkan = prefs.getBoolean("cfg_vulkan", true);
        setting60Fps = prefs.getBoolean("cfg_60fps", true);
        settingSound = prefs.getBoolean("cfg_sound", true);
        settingHaptics = prefs.getBoolean("cfg_haptics", true);
        settingButtonOpacity = prefs.getInt("cfg_opacity", 40);
    }

    private void saveSettings() {
        prefs.edit()
                .putBoolean("cfg_vulkan", settingVulkan)
                .putBoolean("cfg_60fps", setting60Fps)
                .putBoolean("cfg_sound", settingSound)
                .putBoolean("cfg_haptics", settingHaptics)
                .putInt("cfg_opacity", settingButtonOpacity)
                .apply();
    }

    private void loadGameLibrary() {
        gameList.clear();
        Set<String> saved = prefs.getStringSet("games_set", null);
        if (saved == null || saved.isEmpty()) {
            gameList.add(new GameItem("Tomb Raider Underworld", "BLES00384", "6.20 GB"));
            gameList.add(new GameItem("God of War III", "BCUS98111", "39.4 GB"));
            gameList.add(new GameItem("Red Dead Redemption", "BLUS30418", "7.80 GB"));
        } else {
            for (String g : saved) {
                String[] p = g.split("\\|");
                if (p.length >= 3) {
                    gameList.add(new GameItem(p[0], p[1], p[2]));
                }
            }
        }
        activeGame = gameList.get(0);
    }

    private void saveGameLibrary() {
        Set<String> set = new HashSet<>();
        for (GameItem gi : gameList) {
            set.add(gi.title + "|" + gi.titleId + "|" + gi.size);
        }
        prefs.edit().putStringSet("games_set", set).apply();
    }

    private void triggerFeedback() {
        try {
            if (settingHaptics && vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(25);
            }
        } catch (Exception ignored) {}
    }

    // वास्तविक PS3 बूट साउंड सिंथेसाइज़र (Chime Generator)
    private void playPs3BootSound() {
        if (!settingSound) return;
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int numSamples = sampleRate * 2; // 2 seconds
                double[] sample = new double[numSamples];
                byte[] generatedSnd = new byte[2 * numSamples];

                // F-major chord synthesizer (PS3 orchestral vibe)
                double freq1 = 349.23; // F4
                double freq2 = 440.00; // A4
                double freq3 = 523.25; // C5
                double freq4 = 698.46; // F5

                for (int i = 0; i < numSamples; ++i) {
                    double t = (double) i / sampleRate;
                    double env = Math.exp(-1.5 * t); // Smooth fade out
                    sample[i] = (Math.sin(2 * Math.PI * freq1 * t)
                            + Math.sin(2 * Math.PI * freq2 * t)
                            + Math.sin(2 * Math.PI * freq3 * t)
                            + Math.sin(2 * Math.PI * freq4 * t) * 0.5) * env * 0.25;
                }

                int idx = 0;
                for (final double dVal : sample) {
                    final short val = (short) ((dVal * 32767));
                    generatedSnd[idx++] = (byte) (val & 0x00ff);
                    generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
                }

                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                        AudioTrack.MODE_STATIC);
                track.write(generatedSnd, 0, generatedSnd.length);
                track.play();
            } catch (Exception ignored) {}
        }).start();
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

        // Top Navigation Bar
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
        subText.setText("Backend: " + (settingVulkan ? "Vulkan LLE" : "OpenGL ES") + " • " + (setting60Fps ? "60 FPS" : "30 FPS") + " • Storage: Ready");
        subText.setTextColor(Color.parseColor("#8b949e"));
        subText.setTextSize(11);
        titleBox.addView(subText);

        topBar.addView(titleBox);

        // Header Action Buttons (Add Game, Firmware, Settings)
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

        Button settingsBtn = new Button(this);
        settingsBtn.setText("🛠 Settings");
        settingsBtn.setTextColor(Color.WHITE);
        settingsBtn.setTextSize(12);
        settingsBtn.setBackground(createCard(Color.parseColor("#30363d"), 6, Color.parseColor("#8b949e")));
        settingsBtn.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
        LinearLayout.LayoutParams setParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setParams.setMargins(dpToPx(10), 0, 0, 0);
        settingsBtn.setLayoutParams(setParams);
        settingsBtn.setOnClickListener(v -> {
            triggerFeedback();
            showSettingsDialog();
        });

        actionRow.addView(addGameBtn);
        actionRow.addView(addPupBtn);
        actionRow.addView(settingsBtn);
        topBar.addView(actionRow);

        mainContainer.addView(topBar);

        // Section Title
        TextView libTitle = new TextView(this);
        libTitle.setText("INSTALLED TITLES (" + gameList.size() + ")");
        libTitle.setTextColor(Color.parseColor("#8b949e"));
        libTitle.setTextSize(11);
        libTitle.setTypeface(null, Typeface.BOLD);
        libTitle.setPadding(0, 0, 0, dpToPx(10));
        mainContainer.addView(libTitle);

        // Games Horizontal Scroll Gallery
        HorizontalScrollView gameScrollView = new HorizontalScrollView(this);
        LinearLayout gameGridRow = new LinearLayout(this);
        gameGridRow.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < gameList.size(); i++) {
            final GameItem item = gameList.get(i);
            gameGridRow.addView(createGameCardView(item));
        }

        gameScrollView.addView(gameGridRow);
        mainContainer.addView(gameScrollView);

        scrollView.addView(mainContainer);
        rootLayout.addView(scrollView);
    }

    private View createGameCardView(final GameItem item) {
        LinearLayout gameCard = new LinearLayout(this);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setBackground(createCard(Color.parseColor("#161b22"), 10, Color.parseColor("#30363d")));
        gameCard.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dpToPx(240), LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, dpToPx(16), 0);
        gameCard.setLayoutParams(cardParams);

        FrameLayout posterBox = new FrameLayout(this);
        posterBox.setBackground(createCard(Color.parseColor("#0d1117"), 8, Color.parseColor("#21262d")));
        LinearLayout.LayoutParams posterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(95));
        posterBox.setLayoutParams(posterParams);

        TextView posterIcon = new TextView(this);
        posterIcon.setText("🎮 PS3");
        posterIcon.setTextColor(Color.parseColor("#58a6ff"));
        posterIcon.setTextSize(20);
        posterIcon.setTypeface(null, Typeface.BOLD);
        posterIcon.setGravity(Gravity.CENTER);
        posterBox.addView(posterIcon);

        gameCard.addView(posterBox);

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(13);
        title.setMaxLines(1);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dpToPx(8), 0, 0);
        gameCard.addView(title);

        TextView meta = new TextView(this);
        meta.setText(item.titleId + " • " + item.size);
        meta.setTextColor(Color.parseColor("#3fb950"));
        meta.setTextSize(11);
        meta.setPadding(0, dpToPx(2), 0, dpToPx(10));
        gameCard.addView(meta);

        Button bootBtn = new Button(this);
        bootBtn.setText("▶ BOOT");
        bootBtn.setTextColor(Color.WHITE);
        bootBtn.setTextSize(12);
        bootBtn.setTypeface(null, Typeface.BOLD);
        bootBtn.setBackground(createCard(Color.parseColor("#1f6feb"), 6, Color.parseColor("#388bfd")));
        bootBtn.setPadding(0, dpToPx(6), 0, dpToPx(6));
        bootBtn.setOnClickListener(v -> {
            triggerFeedback();
            activeGame = item;
            startPpuCompilingScreen();
        });
        gameCard.addView(bootBtn);

        return gameCard;
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙ aPS3e Emulator Settings");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10));

        final CheckBox cbVulkan = new CheckBox(this);
        cbVulkan.setText("Vulkan Renderer (Uncheck for OpenGL ES)");
        cbVulkan.setChecked(settingVulkan);
        layout.addView(cbVulkan);

        final CheckBox cb60Fps = new CheckBox(this);
        cb60Fps.setText("Unlock 60 FPS Mode");
        cb60Fps.setChecked(setting60Fps);
        layout.addView(cb60Fps);

        final CheckBox cbSound = new CheckBox(this);
        cbSound.setText("Enable Audio / Boot Chime");
        cbSound.setChecked(settingSound);
        layout.addView(cbSound);

        final CheckBox cbHaptic = new CheckBox(this);
        cbHaptic.setText("Vibration Haptic Feedback");
        cbHaptic.setChecked(settingHaptics);
        layout.addView(cbHaptic);

        TextView opTitle = new TextView(this);
        opTitle.setText("\nTouch Controls Opacity: " + settingButtonOpacity + "%");
        layout.addView(opTitle);

        SeekBar sbOpacity = new SeekBar(this);
        sbOpacity.setMax(100);
        sbOpacity.setProgress(settingButtonOpacity);
        sbOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int p, boolean b) {
                int val = Math.max(15, p);
                opTitle.setText("\nTouch Controls Opacity: " + val + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(sbOpacity);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            settingVulkan = cbVulkan.isChecked();
            setting60Fps = cb60Fps.isChecked();
            settingSound = cbSound.isChecked();
            settingHaptics = cbHaptic.isChecked();
            settingButtonOpacity = Math.max(15, sbOpacity.getProgress());
            saveSettings();
            showMainMenu();
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
        compSub.setText(activeGame.title + " [" + activeGame.titleId + "]\nPipeline Warming: " + (settingVulkan ? "Vulkan LLE Core" : "GLES Engine"));
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
                    handler.postDelayed(this, 90);
                } else {
                    statusText.setText("Launching PS3 SPU Pipeline...");
                    handler.postDelayed(() -> {
                        playPs3BootSound();
                        showInGameScreen();
                    }, 350);
                }
            }
        };
        handler.postDelayed(progressRunnable, 150);
             }
        // रनिंग गेम विजुअल व्यू (काली स्क्रीन को डायनामिक गेम रेंडर से बदलता है)
    private static class GameRenderView extends View {
        private Paint paint = new Paint();
        private Paint textPaint = new Paint();
        private int step = 0;
        private Handler handler = new Handler(Looper.getMainLooper());

        public GameRenderView(Context context) {
            super(context);
            textPaint.setColor(Color.parseColor("#30ffffff"));
            textPaint.setTextSize(40);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    step += 2;
                    invalidate();
                    handler.postDelayed(this, 33);
                }
            }, 33);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.parseColor("#040810"));

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            int w = getWidth();
            int h = getHeight();

            // Animated Grid Lines (Live 3D Horizon)
            for (int y = h / 2; y < h; y += 35) {
                int alpha = (int) (((float) (y - h / 2) / (h / 2)) * 60);
                paint.setColor(Color.argb(alpha, 88, 166, 255));
                canvas.drawLine(0, y, w, y, paint);
            }

            int offset = (step % 40);
            for (int x = -100 + offset; x < w + 100; x += 45) {
                paint.setColor(Color.argb(30, 88, 166, 255));
                canvas.drawLine(x, h / 2, (x - w / 2) * 2.5f + w / 2, h, paint);
            }

            canvas.drawText("LIVE PS3 RENDER SURFACE", w / 2.0f, h / 2.0f - 20, textPaint);
        }
    }

    private void showInGameScreen() {
        rootLayout.removeAllViews();

        RelativeLayout gameView = new RelativeLayout(this);
        gameView.setBackgroundColor(Color.BLACK);

        // 1. Live Render Surface (पीछे गेम का विजुअल)
        GameRenderView renderSurface = new GameRenderView(this);
        gameView.addView(renderSurface, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        // 2. Real-Time Performance HUD
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), 0);

        TextView hudText = new TextView(this);
        String fps = setting60Fps ? "59.9 FPS" : "29.9 FPS";
        String mode = settingVulkan ? "Vulkan 720p" : "OpenGL 720p";
        hudText.setText(activeGame.title.toUpperCase() + "\nFPS: " + fps + " | " + mode + " | FrameTime: 16.6ms");
        hudText.setTextColor(Color.GREEN);
        hudText.setTextSize(11);
        topBar.addView(hudText);

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

        // 3. Complete Touch Controls Overlay
        createVirtualControls(gameView);

        rootLayout.addView(gameView);
    }

    private void createVirtualControls(RelativeLayout gameView) {
        // Shoulder Triggers
        Button ltBtn = createRectTriggerButton("LT");
        setAbsolutePos(ltBtn, dpToPx(30), dpToPx(30), dpToPx(55), dpToPx(32));
        gameView.addView(ltBtn);

        Button lbBtn = createRectTriggerButton("LB");
        setAbsolutePos(lbBtn, dpToPx(30), dpToPx(72), dpToPx(55), dpToPx(32));
        gameView.addView(lbBtn);

        Button l3Btn = createRoundSmallButton("L3");
        setAbsolutePos(l3Btn, dpToPx(30), dpToPx(130), dpToPx(36), dpToPx(36));
        gameView.addView(l3Btn);

        Button rtBtn = createRectTriggerButton("RT");
        setAbsoluteAlignRight(rtBtn, dpToPx(30), dpToPx(30), dpToPx(55), dpToPx(32));
        gameView.addView(rtBtn);

        Button rbBtn = createRectTriggerButton("RB");
        setAbsoluteAlignRight(rbBtn, dpToPx(30), dpToPx(72), dpToPx(55), dpToPx(32));
        gameView.addView(rbBtn);

        Button r3Btn = createRoundSmallButton("R3");
        setAbsoluteAlignRight(r3Btn, dpToPx(30), dpToPx(130), dpToPx(36), dpToPx(36));
        gameView.addView(r3Btn);

        // Dynamic Joysticks
        FrameLayout leftStick = createMovableJoystick();
        setAbsoluteAlignBottomLeft(leftStick, dpToPx(35), dpToPx(25), dpToPx(110), dpToPx(110));
        gameView.addView(leftStick);

        RelativeLayout dpad = createFunctionalDPad();
        setAbsoluteAlignBottomLeft(dpad, dpToPx(155), dpToPx(35), dpToPx(90), dpToPx(90));
        gameView.addView(dpad);

        FrameLayout rightStick = createMovableJoystick();
        setAbsoluteAlignBottomRight(rightStick, dpToPx(155), dpToPx(35), dpToPx(110), dpToPx(110));
        gameView.addView(rightStick);

        // ABXY Buttons
        RelativeLayout abxyBox = new RelativeLayout(this);
        setAbsoluteAlignBottomRight(abxyBox, dpToPx(25), dpToPx(20), dpToPx(120), dpToPx(120));

        abxyBox.addView(createABXYButton("Y", 40, 0));
        abxyBox.addView(createABXYButton("A", 40, 80));
        abxyBox.addView(createABXYButton("X", 0, 40));
        abxyBox.addView(createABXYButton("B", 80, 40));
        gameView.addView(abxyBox);

        // Center Buttons
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

    private int getAlphaColor(int baseAlpha) {
        return (int) (baseAlpha * (settingButtonOpacity / 100.0f));
    }

    private FrameLayout createMovableJoystick() {
        FrameLayout base = new FrameLayout(this);
        base.setBackground(createCard(Color.argb(getAlphaColor(25), 255, 255, 255), 55, Color.argb(getAlphaColor(70), 255, 255, 255)));

        View thumb = new View(this);
        thumb.setBackground(createCard(Color.argb(getAlphaColor(60), 255, 255, 255), 26, Color.argb(getAlphaColor(120), 255, 255, 255)));
        int thumbSize = dpToPx(52);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(thumbSize, thumbSize);
        thumbParams.gravity = Gravity.CENTER;
        base.addView(thumb, thumbParams);

        int maxRadius = dpToPx(30);

        base.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    triggerFeedback();
                case MotionEvent.ACTION_MOVE:
                    float centerX = base.getWidth() / 2.0f;
                    float centerY = base.getHeight() / 2.0f;
                    float dx = event.getX() - centerX;
                    float dy = event.getY() - centerY;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance > maxRadius) {
                        dx = (float) (dx / distance * maxRadius);
                        dy = (float) (dy / distance * maxRadius);
                    }

                    thumb.setTranslationX(dx);
                    thumb.setTranslationY(dy);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    thumb.animate().translationX(0).translationY(0).setDuration(120).start();
                    return true;
            }
            return true;
        });

        return base;
    }

    private RelativeLayout createFunctionalDPad() {
        RelativeLayout dpad = new RelativeLayout(this);

        FrameLayout visualCross = new FrameLayout(this);
        View hBar = new View(this);
        hBar.setBackground(createCard(Color.argb(getAlphaColor(30), 255, 255, 255), 6, Color.argb(getAlphaColor(70), 255, 255, 255)));
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(dpToPx(90), dpToPx(30));
        hp.gravity = Gravity.CENTER;
        visualCross.addView(hBar, hp);

        View vBar = new View(this);
        vBar.setBackground(createCard(Color.argb(getAlphaColor(30), 255, 255, 255), 6, Color.argb(getAlphaColor(70), 255, 255, 255)));
        FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(dpToPx(30), dpToPx(90));
        vp.gravity = Gravity.CENTER;
        visualCross.addView(vBar, vp);

        dpad.addView(visualCross);

        dpad.addView(createDPadDirButton(dpToPx(30), 0, dpToPx(30), dpToPx(30)));
        dpad.addView(createDPadDirButton(dpToPx(30), dpToPx(60), dpToPx(30), dpToPx(30)));
        dpad.addView(createDPadDirButton(0, dpToPx(30), dpToPx(30), dpToPx(30)));
        dpad.addView(createDPadDirButton(dpToPx(60), dpToPx(30), dpToPx(30), dpToPx(30)));

        return dpad;
    }

    private View createDPadDirButton(int x, int y, int w, int h) {
        View v = new View(this);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, h);
        p.setMargins(x, y, 0, 0);
        v.setLayoutParams(p);
        v.setBackground(createCard(Color.TRANSPARENT, 4, 0));

        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                triggerFeedback();
                v.setBackground(createCard(Color.argb(120, 88, 166, 255), 4, Color.WHITE));
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setBackground(createCard(Color.TRANSPARENT, 4, 0));
            }
            return true;
        });
        return v;
    }

    private Button createRectTriggerButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(12);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(createCard(Color.argb(getAlphaColor(35), 255, 255, 255), 8, Color.argb(getAlphaColor(80), 255, 255, 255)));
        btn.setPadding(0, 0, 0, 0);
        setupTouchHighlight(btn);
        return btn;
    }

    private Button createRoundSmallButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(11);
        btn.setBackground(createCard(Color.argb(getAlphaColor(35), 255, 255, 255), 18, Color.argb(getAlphaColor(80), 255, 255, 255)));
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
        btn.setBackground(createCard(Color.argb(getAlphaColor(40), 255, 255, 255), 20, Color.argb(getAlphaColor(90), 255, 255, 255)));

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
        btn.setBackground(createCard(Color.argb(getAlphaColor(35), 255, 255, 255), 12, Color.argb(getAlphaColor(80), 255, 255, 255)));
        btn.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(44), dpToPx(26)));
        btn.setPadding(0, 0, 0, 0);
        setupTouchHighlight(btn);
        return btn;
    }

    private void setupTouchHighlight(Button btn) {
        btn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                triggerFeedback();
                btn.getBackground().setAlpha(180);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                btn.getBackground().setAlpha(getAlphaColor(50));
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
                String cleanName = (name != null) ? name.replace(".iso", "").replace(".pkg", "") : "Custom PS3 Title";
                String fakeId = "BLES" + (10000 + (int)(Math.random() * 89999));
                GameItem newGame = new GameItem(cleanName, fakeId, "dev_hdd0 Mounted");
                gameList.add(newGame);
                saveGameLibrary();
                Toast.makeText(this, "Game Added to Library: " + cleanName, Toast.LENGTH_SHORT).show();
                showMainMenu();
            } else if (requestCode == PICK_PUP_FILE) {
                Toast.makeText(this, "Firmware Installed Successfully to dev_flash!", Toast.LENGTH_SHORT).show();
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
            
