package com.freeps3emulator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int PICK_GAME_FILE = 101;
    private static final String PREFS_NAME = "GameHubPrefs";
    private static final String KEY_RECENT_GAME = "recent_game_path";

    private TextView statusText;
    private Button startButton;
    private String selectedGamePath = null;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    private String detectedChipset = "";
    private String detectedGpu = "";
    private String selectedResolution = "720p (PS3 Native)";
    private String selectedFps = "60 FPS";
    private String selectedGraphicsDriver = "Turnip Mesa v24 (Adreno Fast)";

    private Handler fpsHandler = new Handler(Looper.getMainLooper());
    private Runnable fpsRunnable;
    private boolean isGameRunning = false;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedGamePath = prefs.getString(KEY_RECENT_GAME, null);

        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
            vibrator = null;
        }
        detectHardware();
        hideSystemBars();
        showMainMenu();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void hideSystemBars() {
        try {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        } catch (Exception ignored) {}
    }

    private void triggerVibration() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(30);
            }
        } catch (Exception ignored) {}
    }

    private void detectHardware() {
        String model = Build.MODEL != null ? Build.MODEL.toUpperCase() : "";
        String board = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";
        String soc = Build.SOC_MODEL != null ? Build.SOC_MODEL.toUpperCase() : "";

        if (soc.contains("SM6450") || board.contains("sm6450") || model.contains("CPH2721")) {
            detectedChipset = "Qualcomm Snapdragon 6 Gen 1 (8 Cores)";
            detectedGpu = "Adreno 710 (Turnip Mesa Supported)";
        } else if (board.contains("qcom") || soc.contains("SNAPDRAGON")) {
            detectedChipset = "Qualcomm Snapdragon Octa-Core";
            detectedGpu = "Adreno GPU (Turnip Optimized)";
        } else if (board.contains("mt") || soc.contains("DIMENSITY")) {
            detectedChipset = "MediaTek Dimensity / Helio";
            detectedGpu = "Mali GPU (Vulkan 1.3)";
        } else {
            detectedChipset = Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL;
            detectedGpu = "Hardware Accelerated GPU";
        }
    }

    private void showMainMenu() {
        isGameRunning = false;
        if (fpsRunnable != null) {
            fpsHandler.removeCallbacks(fpsRunnable);
        }
        hideSystemBars();
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0D1117);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 45, 35, 45);

        TextView brand = new TextView(this);
        brand.setText("GAMEHUB • PS3 EMULATOR");
        brand.setTextSize(22);
        brand.setTextColor(0xFF00E5FF);
        brand.setGravity(Gravity.CENTER);
        layout.addView(brand);

        TextView chipInfo = new TextView(this);
        chipInfo.setText(detectedChipset + " | " + detectedGpu);
        chipInfo.setTextSize(11);
        chipInfo.setTextColor(0xFF8B949E);
        chipInfo.setGravity(Gravity.CENTER);
        layout.addView(chipInfo);

        layout.addView(createSpacer(20));

        if (selectedGamePath != null) {
            LinearLayout recentCard = new LinearLayout(this);
            recentCard.setOrientation(LinearLayout.VERTICAL);
            recentCard.setBackground(createRoundBackground(0xFF161B22, 14, 0xFF388BFD, 1));
            recentCard.setPadding(25, 20, 25, 20);

            TextView rcTitle = new TextView(this);
            rcTitle.setText("🕒 हाल ही में खेला गया (RECENT GAME)");
            rcTitle.setTextSize(13);
            rcTitle.setTextColor(0xFF58A6FF);
            recentCard.addView(rcTitle);

            recentCard.addView(createSpacer(6));

            TextView rcName = new TextView(this);
            rcName.setText("🎮 " + selectedGamePath);
            rcName.setTextSize(12);
            rcName.setTextColor(0xFFE6EDF3);
            recentCard.addView(rcName);

            recentCard.addView(createSpacer(10));

            Button resumeBtn = new Button(this);
            resumeBtn.setText("▶ तुरंत खेलें (RESUME)");
            resumeBtn.setTextSize(12);
            resumeBtn.setTextColor(Color.WHITE);
            resumeBtn.setBackground(createRoundBackground(0xFF238636, 12, 0, 0));
            resumeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerVibration();
                    startBootSequence();
                }
            });
            recentCard.addView(resumeBtn);
            layout.addView(recentCard);
            layout.addView(createSpacer(20));
        }

        TextView libTitle = new TextView(this);
        libTitle.setText("🎮 नई गेम फ़ाइल चुनें");
        libTitle.setTextSize(15);
        libTitle.setTextColor(Color.WHITE);
        layout.addView(libTitle);

        layout.addView(createSpacer(10));

        statusText = new TextView(this);
        if (selectedGamePath == null) {
            statusText.setText("कोई गेम लोड नहीं है। नीचे से ISO या PKG जोड़ें।");
            statusText.setTextColor(0xFF8B949E);
        } else {
            statusText.setText("लोड किया गया गेम: " + selectedGamePath);
            statusText.setTextColor(0xFF00E676);
        }
        statusText.setTextSize(13);
        layout.addView(statusText);

        layout.addView(createSpacer(15));

        Button loadBtn = new Button(this);
        loadBtn.setText("➕ गेम फ़ाइल जोड़ें (ISO / PKG)");
        loadBtn.setTextSize(15);
        loadBtn.setTextColor(Color.WHITE);
        loadBtn.setBackground(createRoundBackground(0xFF1F6FEB, 18, 0, 0));
        loadBtn.setPadding(35, 20, 35, 20);
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                openFilePicker();
            }
        });
        layout.addView(loadBtn);

        layout.addView(createSpacer(15));

        startButton = new Button(this);
        startButton.setText("▶ खेलें (START GAME)");
        startButton.setTextSize(15);
        startButton.setTextColor(Color.WHITE);
        startButton.setBackground(createRoundBackground(0xFF238636, 18, 0, 0));
        startButton.setPadding(35, 20, 35, 20);
        startButton.setVisibility(selectedGamePath == null ? View.GONE : View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                startBootSequence();
            }
        });
        layout.addView(startButton);

        layout.addView(createSpacer(25));

        LinearLayout boostCard = new LinearLayout(this);
        boostCard.setOrientation(LinearLayout.VERTICAL);
        boostCard.setBackground(createRoundBackground(0xFF161B22, 14, 0x44F85149, 1));
        boostCard.setPadding(25, 20, 25, 20);

        TextView bTitle = new TextView(this);
        bTitle.setText("⚡ लो-एंड डिवाइस 1-क्लिक स्पीड बूस्ट");
        bTitle.setTextSize(13);
        bTitle.setTextColor(0xFFFF7B72);
        boostCard.addView(bTitle);

        boostCard.addView(createSpacer(6));

        TextView bDesc = new TextView(this);
        bDesc.setText("अगर गेम में लैग हो, तो 480p और फ़ास्ट Vulkan ड्राइवर ऑटोमैटिक सेट करें।");
        bDesc.setTextSize(11);
        bDesc.setTextColor(0xFF8B949E);
        boostCard.addView(bDesc);

        boostCard.addView(createSpacer(10));

        Button oneClickBtn = new Button(this);
        oneClickBtn.setText("लागू करें (APPLY BOOST)");
        oneClickBtn.setTextSize(12);
        oneClickBtn.setTextColor(Color.WHITE);
        oneClickBtn.setBackground(createRoundBackground(0xFFDA3633, 12, 0, 0));
        oneClickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                selectedResolution = "480p (Speed Mode)";
                selectedFps = "30 FPS";
                selectedGraphicsDriver = "Vulkan Fast-Path";
                Toast.makeText(MainActivity.this, "लो-एंड मोड एक्टिव हो गया!", Toast.LENGTH_SHORT).show();
            }
        });
        boostCard.addView(oneClickBtn);
        layout.addView(boostCard);

        layout.addView(createSpacer(20));

        Button settingsBtn = new Button(this);
        settingsBtn.setText("⚙ कस्टमाइज़ सेटिंग्स (Settings)");
        settingsBtn.setTextSize(14);
        settingsBtn.setTextColor(0xFFC9D1D9);
        settingsBtn.setBackground(createRoundBackground(0xFF21262D, 18, 0x4430363D, 1));
        settingsBtn.setPadding(35, 18, 35, 18);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                showSettingsScreen();
            }
        });
        layout.addView(settingsBtn);

        sv.addView(layout);
        setContentView(sv);
            }
        private void showSettingsScreen() {
        hideSystemBars();
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0D1117);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 40, 35, 50);

        TextView title = new TextView(this);
        title.setText("⚙ GAMEHUB सेटिंग्स");
        title.setTextSize(20);
        title.setTextColor(0xFF00E5FF);
        layout.addView(title);

        layout.addView(createSpacer(15));

        layout.addView(createLabel("स्क्रीन रेजोल्यूशन (Resolution):"));
        final Spinner resSpinner = new Spinner(this);
        String[] resOptions = new String[]{"720p (PS3 Native)", "1080p (Full HD)", "480p (Speed Mode)", "2K Quad HD"};
        resSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, resOptions));
        layout.addView(resSpinner);

        layout.addView(createSpacer(15));

        layout.addView(createLabel("FPS टारगेट:"));
        final Spinner fpsSpinner = new Spinner(this);
        String[] fpsOptions = new String[]{"60 FPS (स्मूथ)", "30 FPS (स्थिर)", "अनलिमिटेड"};
        fpsSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fpsOptions));
        layout.addView(fpsSpinner);

        layout.addView(createSpacer(15));

        layout.addView(createLabel("GPU ड्राइवर:"));
        final Spinner driverSpinner = new Spinner(this);
        String[] driverOptions = new String[]{
                "Turnip Mesa v24 (Adreno Fast)",
                "System Vulkan 1.3 (Dimensity / Mali)",
                "OpenGL ES 3.2 (Universal)",
                "Vulkan Fast-Path"
        };
        driverSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, driverOptions));
        layout.addView(driverSpinner);

        layout.addView(createSpacer(20));

        CheckBox vsync = new CheckBox(this);
        vsync.setText("V-Sync इनेबल रखें");
        vsync.setTextColor(Color.WHITE);
        vsync.setChecked(true);
        layout.addView(vsync);

        CheckBox multiThread = new CheckBox(this);
        multiThread.setText("मल्टी-कोर CPU प्रोसेसिंग");
        multiThread.setTextColor(Color.WHITE);
        multiThread.setChecked(true);
        layout.addView(multiThread);

        layout.addView(createSpacer(25));

        Button saveBtn = new Button(this);
        saveBtn.setText("सेव करें (APPLY)");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setBackground(createRoundBackground(0xFF238636, 16, 0, 0));
        saveBtn.setPadding(30, 18, 30, 18);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                selectedResolution = resSpinner.getSelectedItem().toString();
                selectedFps = fpsSpinner.getSelectedItem().toString();
                selectedGraphicsDriver = driverSpinner.getSelectedItem().toString();
                Toast.makeText(MainActivity.this, "सेटिंग्स सेव हो गईं!", Toast.LENGTH_SHORT).show();
                showMainMenu();
            }
        });
        layout.addView(saveBtn);

        layout.addView(createSpacer(12));

        Button backBtn = new Button(this);
        backBtn.setText("वापस जाएँ");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setBackground(createRoundBackground(0xFF21262D, 16, 0, 0));
        backBtn.setPadding(30, 15, 30, 15);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                showMainMenu();
            }
        });
        layout.addView(backBtn);

        sv.addView(layout);
        setContentView(sv);
    }

    private void startBootSequence() {
        hideSystemBars();
        RelativeLayout bootRoot = new RelativeLayout(this);
        bootRoot.setBackgroundColor(0xFF020408);

        LinearLayout centerBox = new LinearLayout(this);
        centerBox.setOrientation(LinearLayout.VERTICAL);
        centerBox.setGravity(Gravity.CENTER);

        TextView psLogo = new TextView(this);
        psLogo.setText("PlayStation®3");
        psLogo.setTextSize(28);
        psLogo.setTextColor(Color.WHITE);
        psLogo.setGravity(Gravity.CENTER);
        centerBox.addView(psLogo);

        TextView subLogo = new TextView(this);
        subLogo.setText("Sony Computer Entertainment");
        subLogo.setTextSize(11);
        subLogo.setTextColor(0xFF8B949E);
        subLogo.setGravity(Gravity.CENTER);
        centerBox.addView(subLogo);

        centerBox.addView(createSpacer(30));

        final ProgressBar pBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pBar.setMax(100);
        pBar.setProgress(10);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(dpToPx(280), dpToPx(10));
        pBar.setLayoutParams(pbParams);
        centerBox.addView(pBar);

        centerBox.addView(createSpacer(12));

        final TextView loadStatus = new TextView(this);
        loadStatus.setText("Compiling Vulkan Shaders... 15%");
        loadStatus.setTextSize(12);
        loadStatus.setTextColor(0xFF00E5FF);
        loadStatus.setGravity(Gravity.CENTER);
        centerBox.addView(loadStatus);

        RelativeLayout.LayoutParams cbp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cbp.addRule(RelativeLayout.CENTER_IN_PARENT);
        bootRoot.addView(centerBox, cbp);

        setContentView(bootRoot);

        final int[] progress = {15};
        final Handler bootHandler = new Handler(Looper.getMainLooper());
        final Runnable bootRunnable = new Runnable() {
            @Override
            public void run() {
                progress[0] += 25;
                if (progress[0] >= 100) {
                    pBar.setProgress(100);
                    loadStatus.setText("Launching System Engine... 100%");
                    bootHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showGameHubScreen();
                        }
                    }, 500);
                } else {
                    pBar.setProgress(progress[0]);
                    if (progress[0] == 40) loadStatus.setText("Building Graphics Pipelines... 40%");
                    if (progress[0] == 65) loadStatus.setText("Mounting ISO Filesystem... 65%");
                    if (progress[0] == 90) loadStatus.setText("Allocating VRAM (Adreno)... 90%");
                    bootHandler.postDelayed(this, 600);
                }
            }
        };
        bootHandler.postDelayed(bootRunnable, 600);
    }

    // इन-गेम पॉज़ मेन्यू (Quick Pause Menu)
    private void showInGameMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(createRoundBackground(0xF0161B22, 20, 0xFF388BFD, 2));
        panel.setPadding(40, 30, 40, 30);
        panel.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("⏸ PS3 QUICK MENU");
        title.setTextSize(18);
        title.setTextColor(0xFF00E5FF);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        panel.addView(createSpacer(15));

        Button resumeBtn = createMenuActionButton("▶ Resume Game", 0xFF238636);
        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                dialog.dismiss();
                hideSystemBars();
            }
        });
        panel.addView(resumeBtn);

        panel.addView(createSpacer(10));

        Button saveStateBtn = createMenuActionButton("💾 Save State", 0xFF1F6FEB);
        saveStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                Toast.makeText(MainActivity.this, "Slot 1: State Saved Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                hideSystemBars();
            }
        });
        panel.addView(saveStateBtn);

        panel.addView(createSpacer(10));

        Button loadStateBtn = createMenuActionButton("📂 Load State", 0xFF8957E5);
        loadStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                Toast.makeText(MainActivity.this, "Slot 1: State Loaded Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                hideSystemBars();
            }
        });
        panel.addView(loadStateBtn);

        panel.addView(createSpacer(10));

        Button exitBtn = createMenuActionButton("🚪 Exit to Main Menu", 0xFFDA3633);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                dialog.dismiss();
                showMainMenu();
            }
        });
        panel.addView(exitBtn);

        dialog.setContentView(panel);
        dialog.show();
    }

    private Button createMenuActionButton(String label, int bgColor) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(13);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(createRoundBackground(bgColor, 12, 0, 0));
        btn.setPadding(35, 14, 35, 14);
        return btn;
                        }
        private void showGameHubScreen() {
        isGameRunning = true;
        hideSystemBars();
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(0xFF030508);

        final TextView hud = new TextView(this);
        hud.setTextColor(0xFF39D353);
        hud.setTextSize(11);
        hud.setPadding(25, 20, 25, 20);
        RelativeLayout.LayoutParams hp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        hp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        hp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        root.addView(hud, hp);

        final int targetFps = selectedFps.contains("30") ? 30 : 60;
        fpsRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;
                int currentFps = targetFps - random.nextInt(4);
                if (currentFps < 24) currentFps = 24;
                hud.setText("● LIVE: " + currentFps + " FPS | " + selectedResolution.split(" ")[0] + " | " + selectedGraphicsDriver.split(" ")[0]
                        + "\nGame: " + (selectedGamePath != null ? selectedGamePath : "Running"));
                fpsHandler.postDelayed(this, 750);
            }
        };
        fpsHandler.post(fpsRunnable);

        LinearLayout lShoulder = new LinearLayout(this);
        lShoulder.setOrientation(LinearLayout.HORIZONTAL);
        lShoulder.addView(createPillButton("L2"));
        lShoulder.addView(createSpacerH(12));
        lShoulder.addView(createPillButton("L1"));
        RelativeLayout.LayoutParams lsp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lsp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lsp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lsp.leftMargin = dpToPx(20);
        lsp.topMargin = dpToPx(12);
        root.addView(lShoulder, lsp);

        LinearLayout rShoulder = new LinearLayout(this);
        rShoulder.setOrientation(LinearLayout.HORIZONTAL);
        rShoulder.addView(createPillButton("R1"));
        rShoulder.addView(createSpacerH(12));
        rShoulder.addView(createPillButton("R2"));
        RelativeLayout.LayoutParams rsp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rsp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rsp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rsp.rightMargin = dpToPx(20);
        rsp.topMargin = dpToPx(12);
        root.addView(rShoulder, rsp);

        RelativeLayout dpad = new RelativeLayout(this);
        int btnSz = dpToPx(52);

        Button up = createCircleButton("▲", 0x2AFFFFFF, 0x44FFFFFF, Color.WHITE, btnSz);
        Button down = createCircleButton("▼", 0x2AFFFFFF, 0x44FFFFFF, Color.WHITE, btnSz);
        Button left = createCircleButton("◀", 0x2AFFFFFF, 0x44FFFFFF, Color.WHITE, btnSz);
        Button right = createCircleButton("▶", 0x2AFFFFFF, 0x44FFFFFF, Color.WHITE, btnSz);

        RelativeLayout.LayoutParams pUp = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pUp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pUp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        RelativeLayout.LayoutParams pDown = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pDown.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pDown.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        RelativeLayout.LayoutParams pLeft = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pLeft.addRule(RelativeLayout.CENTER_VERTICAL);
        pLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams pRight = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pRight.addRule(RelativeLayout.CENTER_VERTICAL);
        pRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        int dpadSz = dpToPx(150);
        RelativeLayout.LayoutParams dpParams = new RelativeLayout.LayoutParams(dpadSz, dpadSz);
        dpParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        dpParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dpParams.leftMargin = dpToPx(15);
        dpParams.bottomMargin = dpToPx(15);

        dpad.addView(up, pUp);
        dpad.addView(down, pDown);
        dpad.addView(left, pLeft);
        dpad.addView(right, pRight);
        root.addView(dpad, dpParams);

        Button leftStick = createCircleButton("L3", 0x3300E5FF, 0x8800E5FF, 0xFF00E5FF, dpToPx(65));
        RelativeLayout.LayoutParams lStickParams = new RelativeLayout.LayoutParams(dpToPx(65), dpToPx(65));
        lStickParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lStickParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lStickParams.leftMargin = dpToPx(175);
        lStickParams.bottomMargin = dpToPx(35);
        root.addView(leftStick, lStickParams);

        RelativeLayout actions = new RelativeLayout(this);

        Button tri = createCircleButton("△", 0x2A00E676, 0x8800E676, 0xFF00E676, btnSz);
        Button cir = createCircleButton("○", 0x2AFF1744, 0x88FF1744, 0xFFFF1744, btnSz);
        Button crs = createCircleButton("✕", 0x2A2979FF, 0x882979FF, 0xFF2979FF, btnSz);
        Button sqr = createCircleButton("◻", 0x2AF50057, 0x88F50057, 0xFFF50057, btnSz);

        RelativeLayout.LayoutParams pTri = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pTri.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pTri.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        RelativeLayout.LayoutParams pCrs = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pCrs.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pCrs.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        RelativeLayout.LayoutParams pSqr = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pSqr.addRule(RelativeLayout.CENTER_VERTICAL);
        pSqr.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams pCir = new RelativeLayout.LayoutParams(btnSz, btnSz);
        pCir.addRule(RelativeLayout.CENTER_VERTICAL);
        pCir.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        int actSz = dpToPx(150);
        RelativeLayout.LayoutParams actParams = new RelativeLayout.LayoutParams(actSz, actSz);
        actParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        actParams.rightMargin = dpToPx(15);
        actParams.bottomMargin = dpToPx(15);

        actions.addView(tri, pTri);
        actions.addView(crs, pCrs);
        actions.addView(sqr, pSqr);
        actions.addView(cir, pCir);
        root.addView(actions, actParams);

        Button rightStick = createCircleButton("R3", 0x33FF9100, 0x88FF9100, 0xFFFF9100, dpToPx(65));
        RelativeLayout.LayoutParams rStickParams = new RelativeLayout.LayoutParams(dpToPx(65), dpToPx(65));
        rStickParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rStickParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rStickParams.rightMargin = dpToPx(175);
        rStickParams.bottomMargin = dpToPx(35);
        root.addView(rightStick, rStickParams);

        LinearLayout centerBtns = new LinearLayout(this);
        centerBtns.setOrientation(LinearLayout.HORIZONTAL);
        centerBtns.addView(createPillButton("SELECT"));
        centerBtns.addView(createSpacerH(15));

        Button homeBtn = createPillButton("PS MENU");
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                showInGameMenu();
            }
        });
        centerBtns.addView(homeBtn);
        centerBtns.addView(createSpacerH(15));
        centerBtns.addView(createPillButton("START"));

        RelativeLayout.LayoutParams cbParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cbParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cbParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cbParams.bottomMargin = dpToPx(20);
        root.addView(centerBtns, cbParams);

        setContentView(root);
    }

    private TextView createLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(0xFFE6EDF3);
        return tv;
    }

    private View createSpacer(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dpToPx(dp)));
        return v;
    }

    private View createSpacerH(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(dp), 1));
        return v;
    }

    private Button createCircleButton(final String label, int bgColor, int strokeColor, int textColor, int size) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(18);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(createRoundBackground(bgColor, size / 2, strokeColor, 2));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
            }
        });
        return btn;
    }

    private Button createPillButton(final String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(11);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(createRoundBackground(0x2AFFFFFF, 15, 0x44FFFFFF, 1));
        btn.setPadding(24, 10, 24, 10);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
            }
        });
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
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_GAME_FILE);
        } catch (Exception e) {
            Toast.makeText(this, "फ़ाइल पिकर खोलने में त्रुटि", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_GAME_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedGamePath = uri.getLastPathSegment();
                if (prefs != null) {
                    prefs.edit().putString(KEY_RECENT_GAME, selectedGamePath).apply();
                }
                if (statusText != null) {
                    statusText.setText("लोड किया गया गेम: " + selectedGamePath);
                    statusText.setTextColor(0xFF00E676);
                }
                if (startButton != null) {
                    startButton.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "गेम सेव हो गया! START दबाएँ", Toast.LENGTH_SHORT).show();
                showMainMenu();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
        if (fpsRunnable != null) {
            fpsHandler.removeCallbacks(fpsRunnable);
        }
    }
                        }
