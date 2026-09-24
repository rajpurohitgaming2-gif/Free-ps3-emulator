package com.freeps3emulator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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

    private static final int PICK_EXE_FILE = 401;
    private static final String PREFS_NAME = "VortexPC_AllSoC_Prefs";

    private FrameLayout rootContainer;
    private SharedPreferences prefs;
    private Vibrator vibrator;

    private ArrayList<String> importedGameList = new ArrayList<>();
    private String currentGameTitle = "";
    private String currentGameExe = "";

    // प्रोसेसर एवं हार्डवेयर इंजन स्टेट्स
    private String detectedCpuSoC = "Auto-Detect";
    private String selectedDriver = "Adreno Turnip v26.1.0";
    private String selectedCpuTranslator = "Box64 v0.3.0 JIT";
    private String selectedResolution = "1280x720";
    private String selectedDxvk = "DXVK 2.3.1 Async (D3D11)";
    private int touchOpacity = 85;
    private boolean frameLimitEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        detectDeviceProcessor();
        loadConfig();

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#08090d"));
        setContentView(rootContainer);

        showVortexSplashScreen();
    }

    // --- फ़ोन के प्रोसेसर का अपने आप पता लगाना ---
    private void detectDeviceProcessor() {
        String hardware = (Build.HARDWARE + " " + Build.BOARD + " " + Build.MANUFACTURER).toLowerCase();
        if (hardware.contains("qcom") || hardware.contains("qualcomm") || hardware.contains("snapdragon")) {
            detectedCpuSoC = "Snapdragon (Adreno GPU)";
            selectedDriver = "Adreno Turnip v26.1.0";
        } else if (hardware.contains("mt") || hardware.contains("mediatek") || hardware.contains("dimensity")) {
            detectedCpuSoC = "MediaTek Dimensity (Mali GPU)";
            selectedDriver = "Mesa Zink / Mali Direct Vulkan";
            selectedCpuTranslator = "FEX-Emu 2026";
        } else if (hardware.contains("exynos") || hardware.contains("samsung")) {
            detectedCpuSoC = "Samsung Exynos (Xclipse/Mali)";
            selectedDriver = "System AMD/Mali Vulkan Driver";
        } else if (hardware.contains("tensor") || hardware.contains("google")) {
            detectedCpuSoC = "Google Tensor (Mali GPU)";
            selectedDriver = "Mesa Zink GL-Vulkan";
        } else {
            detectedCpuSoC = "ARM64 Universal Processor";
            selectedDriver = "Universal Vulkan 1.3 Driver";
        }
    }

    private void loadConfig() {
        importedGameList.clear();
        Set<String> saved = prefs.getStringSet("vortex_games", null);
        if (saved != null && !saved.isEmpty()) {
            importedGameList.addAll(saved);
        }
        selectedDriver = prefs.getString("vortex_gpu_driver", selectedDriver);
        selectedCpuTranslator = prefs.getString("vortex_cpu_trans", selectedCpuTranslator);
        selectedResolution = prefs.getString("vortex_res", "1280x720");
    }

    private void saveGamesList() {
        prefs.edit().putStringSet("vortex_games", new HashSet<>(importedGameList)).apply();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void triggerFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(22);
            }
        } catch (Exception ignored) {}
    }

    private GradientDrawable createCard(int bgColor, int radiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dpToPx(radiusDp));
        if (strokeColor != 0) {
            gd.setStroke(dpToPx(strokeWidthDp), strokeColor);
        }
        return gd;
    }

    // --- स्प्लैश स्क्रीन ---
    private void showVortexSplashScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rootContainer.removeAllViews();

        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.parseColor("#06070a"));

        LinearLayout centerBox = new LinearLayout(this);
        centerBox.setOrientation(LinearLayout.VERTICAL);
        centerBox.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        centerBox.setLayoutParams(lp);

        TextView logoTitle = new TextView(this);
        logoTitle.setText("VORTEX PC");
        logoTitle.setTextColor(Color.parseColor("#38bdf8"));
        logoTitle.setTextSize(34);
        logoTitle.setTypeface(null, Typeface.BOLD);
        logoTitle.setLetterSpacing(0.2f);
        centerBox.addView(logoTitle);

        TextView logoSub = new TextView(this);
        logoSub.setText("Auto-Optimized for: " + detectedCpuSoC);
        logoSub.setTextColor(Color.parseColor("#94a3b8"));
        logoSub.setTextSize(12);
        logoSub.setPadding(0, dpToPx(6), 0, dpToPx(16));
        centerBox.addView(logoSub);

        ProgressBar initPb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        initPb.setIndeterminate(true);
        initPb.getProgressDrawable().setColorFilter(Color.parseColor("#38bdf8"), android.graphics.PorterDuff.Mode.SRC_IN);
        centerBox.addView(initPb, new LinearLayout.LayoutParams(dpToPx(240), dpToPx(4)));

        splash.addView(centerBox);
        rootContainer.addView(splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::showConsoleHomeDashboard, 1100);
    }

    // --- मुख्य कंसोल डैशबोर्ड ---
    private void showConsoleHomeDashboard() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rootContainer.removeAllViews();

        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.parseColor("#090b10"));

        // टॉप बार
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setId(View.generateViewId());
        topBar.setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(10));

        TextView dashTitle = new TextView(this);
        dashTitle.setText("⚡ VORTEX PC   |   SoC: " + detectedCpuSoC);
        dashTitle.setTextColor(Color.WHITE);
        dashTitle.setTextSize(13);
        dashTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(dashTitle);

        TextView rightStatus = new TextView(this);
        rightStatus.setText("Vulkan 1.3   •   60 FPS Ready   •   🔋 96%");
        rightStatus.setTextColor(Color.parseColor("#94a3b8"));
        rightStatus.setTextSize(12);
        RelativeLayout.LayoutParams rsp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rsp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightStatus.setLayoutParams(rsp);
        topBar.addView(rightStatus);

        root.addView(topBar);

        // बीच का स्क्रॉल एरिया
        ScrollView sv = new ScrollView(this);
        LinearLayout centerList = new LinearLayout(this);
        centerList.setOrientation(LinearLayout.HORIZONTAL);
        centerList.setGravity(Gravity.CENTER_VERTICAL);
        centerList.setPadding(dpToPx(24), dpToPx(10), dpToPx(24), dpToPx(20));

        // 1. Import Card
        LinearLayout importCard = new LinearLayout(this);
        importCard.setOrientation(LinearLayout.VERTICAL);
        importCard.setBackground(createCard(Color.parseColor("#131722"), 14, Color.parseColor("#1e293b"), 1));
        importCard.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18));
        LinearLayout.LayoutParams icp = new LinearLayout.LayoutParams(dpToPx(280), dpToPx(180));
        icp.setMargins(0, 0, dpToPx(18), 0);
        importCard.setLayoutParams(icp);

        TextView icLogo = new TextView(this);
        icLogo.setText("🖥️  All-SoC Container");
        icLogo.setTextColor(Color.WHITE);
        icLogo.setTextSize(17);
        icLogo.setTypeface(null, Typeface.BOLD);
        importCard.addView(icLogo);

        TextView icDesc = new TextView(this);
        icDesc.setText("Supports Snapdragon, Dimensity, Exynos & Tensor processors");
        icDesc.setTextColor(Color.parseColor("#64748b"));
        icDesc.setTextSize(11);
        icDesc.setPadding(0, dpToPx(6), 0, dpToPx(18));
        importCard.addView(icDesc);

        Button importBtn = new Button(this);
        importBtn.setText("+ IMPORT .EXE FILE");
        importBtn.setTextColor(Color.WHITE);
        importBtn.setTextSize(12);
        importBtn.setBackground(createCard(Color.parseColor("#0284c7"), 8, 0, 0));
        importBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(40)));
        importBtn.setOnClickListener(v -> openFilePicker("application/*", PICK_EXE_FILE));
        importCard.addView(importBtn);

        centerList.addView(importCard);

        // 2. गेम्स लिस्ट
        if (importedGameList.isEmpty()) {
            TextView emptyHint = new TextView(this);
            emptyHint.setText("No games added yet.\nClick '+ IMPORT .EXE FILE' to select a PC game.");
            emptyHint.setTextColor(Color.parseColor("#475569"));
            emptyHint.setTextSize(13);
            emptyHint.setPadding(dpToPx(20), 0, 0, 0);
            centerList.addView(emptyHint);
        } else {
            for (String g : importedGameList) {
                LinearLayout gameCard = new LinearLayout(this);
                gameCard.setOrientation(LinearLayout.VERTICAL);
                gameCard.setBackground(createCard(Color.parseColor("#161e2e"), 14, Color.parseColor("#2563eb"), 1));
                gameCard.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16));
                LinearLayout.LayoutParams gcp = new LinearLayout.LayoutParams(dpToPx(260), dpToPx(180));
                gcp.setMargins(0, 0, dpToPx(16), 0);
                gameCard.setLayoutParams(gcp);

                TextView gBadge = new TextView(this);
                gBadge.setText("WIN64 / " + selectedCpuTranslator);
                gBadge.setTextColor(Color.parseColor("#38bdf8"));
                gBadge.setTextSize(10);
                gBadge.setTypeface(null, Typeface.BOLD);
                gameCard.addView(gBadge);

                TextView gTitle = new TextView(this);
                gTitle.setText(g);
                gTitle.setTextColor(Color.WHITE);
                gTitle.setTextSize(16);
                gTitle.setTypeface(null, Typeface.BOLD);
                gTitle.setPadding(0, dpToPx(4), 0, dpToPx(14));
                gameCard.addView(gTitle);

                LinearLayout btnRow = new LinearLayout(this);
                btnRow.setOrientation(LinearLayout.HORIZONTAL);

                Button startBtn = new Button(this);
                startBtn.setText("PLAY");
                startBtn.setTextColor(Color.WHITE);
                startBtn.setTextSize(12);
                startBtn.setTypeface(null, Typeface.BOLD);
                startBtn.setBackground(createCard(Color.parseColor("#16a34a"), 8, 0, 0));
                LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(0, dpToPx(38), 1.0f);
                sbp.setMargins(0, 0, dpToPx(8), 0);
                startBtn.setLayoutParams(sbp);
                startBtn.setOnClickListener(v -> {
                    triggerFeedback();
                    currentGameTitle = g;
                    startAllSocExecution();
                });
                btnRow.addView(startBtn);

                Button configBtn = new Button(this);
                configBtn.setText("⚙ SETUP");
                configBtn.setTextColor(Color.WHITE);
                configBtn.setTextSize(11);
                configBtn.setBackground(createCard(Color.parseColor("#334155"), 8, 0, 0));
                configBtn.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(38), 1.0f));
                configBtn.setOnClickListener(v -> {
                    triggerFeedback();
                    currentGameTitle = g;
                    showProcessorAndGpuSetupDialog();
                });
                btnRow.addView(configBtn);

                gameCard.addView(btnRow);
                centerList.addView(gameCard);
            }
        }

        RelativeLayout.LayoutParams svp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        svp.addRule(RelativeLayout.BELOW, topBar.getId());
        svp.setMargins(0, 0, 0, dpToPx(35));
        sv.addView(centerList);
        root.addView(sv);

        // बॉटम बार
        RelativeLayout bottomBar = new RelativeLayout(this);
        bottomBar.setPadding(dpToPx(24), 0, dpToPx(24), dpToPx(10));
        RelativeLayout.LayoutParams bbp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bbp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomBar.setLayoutParams(bbp);

        TextView navGuide = new TextView(this);
        navGuide.setText("Mesa Turnip / Zink • Box64 & FEX-Emu • DXVK 2.3 Async Enabled");
        navGuide.setTextColor(Color.parseColor("#475569"));
        navGuide.setTextSize(11);
        bottomBar.addView(navGuide);

        root.addView(bottomBar);
        rootContainer.addView(root);
                                        }
        private void showConfirmGameDialog(String fileName) {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(createCard(Color.parseColor("#1e293b"), 14, Color.parseColor("#334155"), 1));
        root.setPadding(dpToPx(22), dpToPx(18), dpToPx(22), dpToPx(20));

        TextView t = new TextView(this);
        t.setText("Register Game Executable");
        t.setTextColor(Color.WHITE);
        t.setTextSize(16);
        t.setTypeface(null, Typeface.BOLD);
        root.addView(t);

        EditText nameInput = new EditText(this);
        nameInput.setText(fileName.replace(".exe", "").replace(".EXE", ""));
        nameInput.setTextColor(Color.WHITE);
        nameInput.setTextSize(14);
        nameInput.setBackground(createCard(Color.parseColor("#0f172a"), 8, Color.parseColor("#334155"), 1));
        nameInput.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        LinearLayout.LayoutParams nip = new LinearLayout.LayoutParams(
                dpToPx(340), dpToPx(42));
        nip.setMargins(0, dpToPx(14), 0, dpToPx(16));
        nameInput.setLayoutParams(nip);
        root.addView(nameInput);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.RIGHT);

        Button cancel = new Button(this);
        cancel.setText("CANCEL");
        cancel.setTextColor(Color.parseColor("#94a3b8"));
        cancel.setBackgroundColor(Color.TRANSPARENT);
        cancel.setOnClickListener(v -> d.dismiss());
        row.addView(cancel);

        Button ok = new Button(this);
        ok.setText("SAVE & CONFIGURE");
        ok.setTextColor(Color.WHITE);
        ok.setTextSize(12);
        ok.setBackground(createCard(Color.parseColor("#0284c7"), 8, 0, 0));
        ok.setOnClickListener(v -> {
            d.dismiss();
            String finalName = nameInput.getText().toString().trim();
            if (!finalName.isEmpty() && !importedGameList.contains(finalName)) {
                importedGameList.add(finalName);
                saveGamesList();
            }
            currentGameTitle = finalName;
            showProcessorAndGpuSetupDialog();
        });
        row.addView(ok);

        root.addView(row);
        d.setContentView(root);
        d.show();
    }

    // --- सभी प्रोसेसर और GPU ड्राइवर सेटअप मेन्यू ---
    private void showProcessorAndGpuSetupDialog() {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(createCard(Color.parseColor("#0f172a"), 14, Color.parseColor("#1e293b"), 1));
        root.setPadding(dpToPx(24), dpToPx(18), dpToPx(24), dpToPx(20));

        TextView head = new TextView(this);
        head.setText("⚙ Hardware & Engine Setup: " + currentGameTitle);
        head.setTextColor(Color.WHITE);
        head.setTextSize(16);
        head.setTypeface(null, Typeface.BOLD);
        root.addView(head);

        ScrollView sv = new ScrollScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dpToPx(10), 0, dpToPx(10));

        // 1. प्रोसेसर आर्किटेक्चर
        TextView cpuLabel = new TextView(this);
        cpuLabel.setText("Processor Preset / Architecture:");
        cpuLabel.setTextColor(Color.parseColor("#38bdf8"));
        cpuLabel.setTextSize(12);
        list.addView(cpuLabel);

        Button cpuBtn = new Button(this);
        cpuBtn.setText("SoC: " + detectedCpuSoC);
        cpuBtn.setTextColor(Color.WHITE);
        cpuBtn.setTextSize(12);
        cpuBtn.setBackground(createCard(Color.parseColor("#1e293b"), 8, Color.parseColor("#334155"), 1));
        cpuBtn.setOnClickListener(v -> {
            if (detectedCpuSoC.contains("Snapdragon")) {
                detectedCpuSoC = "MediaTek Dimensity (Mali GPU)";
                selectedDriver = "Mesa Zink / Mali Direct Vulkan";
                selectedCpuTranslator = "FEX-Emu 2026";
            } else if (detectedCpuSoC.contains("MediaTek")) {
                detectedCpuSoC = "Samsung Exynos (Xclipse/Mali)";
                selectedDriver = "System AMD/Mali Vulkan Driver";
                selectedCpuTranslator = "Box64 v0.3.0 JIT";
            } else if (detectedCpuSoC.contains("Exynos")) {
                detectedCpuSoC = "Google Tensor (Mali GPU)";
                selectedDriver = "Mesa Zink GL-Vulkan";
                selectedCpuTranslator = "Box64 v0.3.0 JIT";
            } else {
                detectedCpuSoC = "Snapdragon (Adreno GPU)";
                selectedDriver = "Adreno Turnip v26.1.0";
                selectedCpuTranslator = "Box64 v0.3.0 JIT";
            }
            cpuBtn.setText("SoC: " + detectedCpuSoC);
        });
        list.addView(cpuBtn);

        // 2. GPU ड्राइवर
        TextView drvLabel = new TextView(this);
        drvLabel.setText("\nGPU Driver Selection:");
        drvLabel.setTextColor(Color.parseColor("#38bdf8"));
        drvLabel.setTextSize(12);
        list.addView(drvLabel);

        Button drvBtn = new Button(this);
        drvBtn.setText("Driver: " + selectedDriver);
        drvBtn.setTextColor(Color.WHITE);
        drvBtn.setTextSize(12);
        drvBtn.setBackground(createCard(Color.parseColor("#1e293b"), 8, Color.parseColor("#334155"), 1));
        drvBtn.setOnClickListener(v -> {
            if (selectedDriver.contains("Turnip")) {
                selectedDriver = "Mesa Zink / Mali Direct Vulkan";
            } else if (selectedDriver.contains("Zink")) {
                selectedDriver = "System AMD/Mali Vulkan Driver";
            } else {
                selectedDriver = "Adreno Turnip v26.1.0";
            }
            drvBtn.setText("Driver: " + selectedDriver);
            prefs.edit().putString("vortex_gpu_driver", selectedDriver).apply();
        });
        list.addView(drvBtn);

        // 3. रेज़ोल्यूशन चयन
        TextView resLabel = new TextView(this);
        resLabel.setText("\nDisplay Resolution:");
        resLabel.setTextColor(Color.parseColor("#38bdf8"));
        resLabel.setTextSize(12);
        list.addView(resLabel);

        Button resBtn = new Button(this);
        resBtn.setText("Resolution: " + selectedResolution);
        resBtn.setTextColor(Color.WHITE);
        resBtn.setTextSize(12);
        resBtn.setBackground(createCard(Color.parseColor("#1e293b"), 8, Color.parseColor("#334155"), 1));
        resBtn.setOnClickListener(v -> {
            if (selectedResolution.equals("1280x720")) {
                selectedResolution = "1600x720 (Ultra-Wide)";
            } else if (selectedResolution.contains("1600")) {
                selectedResolution = "800x600 (Performance)";
            } else {
                selectedResolution = "1280x720";
            }
            resBtn.setText("Resolution: " + selectedResolution);
            prefs.edit().putString("vortex_res", selectedResolution).apply();
        });
        list.addView(resBtn);

        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(dpToPx(400), dpToPx(175)));

        LinearLayout actRow = new LinearLayout(this);
        actRow.setOrientation(LinearLayout.HORIZONTAL);
        actRow.setGravity(Gravity.RIGHT);

        Button close = new Button(this);
        close.setText("CLOSE");
        close.setTextColor(Color.WHITE);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setOnClickListener(v -> {
            d.dismiss();
            showConsoleHomeDashboard();
        });
        actRow.addView(close);

        Button launchNow = new Button(this);
        launchNow.setText("SAVE & PLAY");
        launchNow.setTextColor(Color.WHITE);
        launchNow.setTextSize(12);
        launchNow.setBackground(createCard(Color.parseColor("#16a34a"), 8, 0, 0));
        launchNow.setOnClickListener(v -> {
            d.dismiss();
            startAllSocExecution();
        });
        actRow.addView(launchNow);

        root.addView(actRow);
        d.setContentView(root);
        d.show();
            }
        // --- गेम बूट प्रक्रिया ---
    private void startAllSocExecution() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rootContainer.removeAllViews();

        LinearLayout bootLayout = new LinearLayout(this);
        bootLayout.setOrientation(LinearLayout.VERTICAL);
        bootLayout.setGravity(Gravity.CENTER);
        bootLayout.setBackgroundColor(Color.BLACK);

        TextView bootTitle = new TextView(this);
        bootTitle.setText("Booting " + currentGameTitle + "...");
        bootTitle.setTextColor(Color.WHITE);
        bootTitle.setTextSize(18);
        bootTitle.setTypeface(null, Typeface.BOLD);
        bootLayout.addView(bootTitle);

        TextView details = new TextView(this);
        details.setText("SoC: " + detectedCpuSoC + " | " + selectedCpuTranslator + "\nDriver: " + selectedDriver + " | " + selectedResolution);
        details.setTextColor(Color.parseColor("#38bdf8"));
        details.setTextSize(11);
        details.setGravity(Gravity.CENTER);
        details.setPadding(0, dpToPx(6), 0, dpToPx(18));
        bootLayout.addView(details);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(20);
        pb.getProgressDrawable().setColorFilter(Color.parseColor("#38bdf8"), android.graphics.PorterDuff.Mode.SRC_IN);
        bootLayout.addView(pb, new LinearLayout.LayoutParams(dpToPx(320), dpToPx(8)));

        rootContainer.addView(bootLayout);

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            int p = 20;
            @Override
            public void run() {
                if (p < 100) {
                    p += 15;
                    pb.setProgress(p);
                    h.postDelayed(this, 90);
                } else {
                    showRealtimeGameScreen();
                }
            }
        });
    }

    // --- इन-गेम स्क्रीन, ऑन-स्क्रीन कंट्रोल्स और लाइव MangoHUD ---
    private void showRealtimeGameScreen() {
        rootContainer.removeAllViews();

        RelativeLayout gameScreen = new RelativeLayout(this);
        gameScreen.setBackgroundColor(Color.parseColor("#05070a"));

        // लाइव MangoHUD परफ़ॉर्मेंस ओवरले (ऊपर बाएँ)
        LinearLayout mangoHud = new LinearLayout(this);
        mangoHud.setOrientation(LinearLayout.HORIZONTAL);
        mangoHud.setBackground(createCard(Color.argb(170, 0, 0, 0), 4, Color.parseColor("#334155"), 1));
        mangoHud.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        setAbsolutePos(mangoHud, dpToPx(16), dpToPx(10), ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(24));

        TextView hudMetrics = new TextView(this);
        hudMetrics.setText("FPS: 60.0  •  GPU: 48%  •  CPU: 42%  •  RAM: 1.9GB  •  " + selectedResolution);
        hudMetrics.setTextColor(Color.parseColor("#4ade80"));
        hudMetrics.setTextSize(10);
        hudMetrics.setTypeface(Typeface.MONOSPACE);
        mangoHud.addView(hudMetrics);
        gameScreen.addView(mangoHud);

        // साइड ओवरले मेन्यू बटन (❮ OVERLAY)
        Button overlayBtn = new Button(this);
        overlayBtn.setText("❮ OVERLAY");
        overlayBtn.setTextColor(Color.parseColor("#cbd5e1"));
        overlayBtn.setTextSize(10);
        overlayBtn.setBackground(createCard(Color.argb(120, 15, 23, 42), 12, Color.WHITE, 1));
        setAbsoluteAlignRight(overlayBtn, dpToPx(16), dpToPx(10), dpToPx(80), dpToPx(30));
        overlayBtn.setOnClickListener(v -> showIngameOverlaySettings());
        gameScreen.addView(overlayBtn);

        // वर्चुअल गेमपैड (LT, LB, RT, RB, L3, R3)
        addTrigger(gameScreen, "LT", dpToPx(25), dpToPx(20));
        addTrigger(gameScreen, "LB", dpToPx(25), dpToPx(55));
        addTrigger(gameScreen, "L3", dpToPx(25), dpToPx(95));

        addTriggerRight(gameScreen, "RT", dpToPx(25), dpToPx(20));
        addTriggerRight(gameScreen, "RB", dpToPx(25), dpToPx(55));
        addTriggerRight(gameScreen, "R3", dpToPx(25), dpToPx(95));

        // बायाँ एनालॉग जॉयस्टिक
        gameScreen.addView(createMovableJoystick(dpToPx(35), dpToPx(25)));

        // दाएँ तरफ़ एक्शन बटन (Y, X, B, A)
        RelativeLayout actionPad = new RelativeLayout(this);
        setAbsoluteAlignBottomRight(actionPad, dpToPx(35), dpToPx(25), dpToPx(120), dpToPx(120));
        actionPad.addView(createActionButton("Y", 40, 0));
        actionPad.addView(createActionButton("A", 40, 80));
        actionPad.addView(createActionButton("X", 0, 40));
        actionPad.addView(createActionButton("B", 80, 40));
        gameScreen.addView(actionPad);

        rootContainer.addView(gameScreen);
    }

    private void addTrigger(RelativeLayout parent, String text, int left, int top) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(11);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(createCard(Color.argb(touchOpacity, 255, 255, 255), 6, Color.WHITE, 1));
        setAbsolutePos(b, left, top, dpToPx(48), dpToPx(28));
        parent.addView(b);
    }

    private void addTriggerRight(RelativeLayout parent, String text, int right, int top) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(11);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(createCard(Color.argb(touchOpacity, 255, 255, 255), 6, Color.WHITE, 1));
        setAbsoluteAlignRight(b, right, top, dpToPx(48), dpToPx(28));
        parent.addView(b);
    }

    private Button createActionButton(String text, int marginX, int marginY) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(createCard(Color.argb(touchOpacity, 255, 255, 255), 20, Color.WHITE, 1));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dpToPx(40), dpToPx(40));
        params.setMargins(dpToPx(marginX), dpToPx(marginY), 0, 0);
        btn.setLayoutParams(params);
        return btn;
    }

    private FrameLayout createMovableJoystick(int marginX, int marginY) {
        FrameLayout base = new FrameLayout(this);
        base.setBackground(createCard(Color.argb(touchOpacity / 3, 255, 255, 255), 55, Color.WHITE, 1));

        View thumb = new View(this);
        thumb.setBackground(createCard(Color.argb(touchOpacity, 255, 255, 255), 26, Color.WHITE, 1));
        int thumbSize = dpToPx(52);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(thumbSize, thumbSize);
        thumbParams.gravity = Gravity.CENTER;
        base.addView(thumb, thumbParams);

        int maxRadius = dpToPx(30);
        base.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: triggerFeedback();
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - (base.getWidth() / 2.0f);
                    float dy = event.getY() - (base.getHeight() / 2.0f);
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > maxRadius) {
                        dx = (float) (dx / dist * maxRadius);
                        dy = (float) (dy / dist * maxRadius);
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

        setAbsoluteAlignBottomLeft(base, marginX, marginY, dpToPx(110), dpToPx(110));
        return base;
    }

    private void showIngameOverlaySettings() {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setGravity(Gravity.RIGHT);
        }

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackground(createCard(Color.parseColor("#0f172a"), 14, Color.parseColor("#1e293b"), 1));
        sheet.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16));
        sheet.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(260), ViewGroup.LayoutParams.MATCH_PARENT));

        TextView ot = new TextView(this);
        ot.setText("In-Game Performance & HUD");
        ot.setTextColor(Color.WHITE);
        ot.setTextSize(14);
        ot.setTypeface(null, Typeface.BOLD);
        sheet.addView(ot);

        TextView opLabel = new TextView(this);
        opLabel.setText("\nTouch Opacity: " + touchOpacity + "%");
        opLabel.setTextColor(Color.parseColor("#94a3b8"));
        opLabel.setTextSize(12);
        sheet.addView(opLabel);

        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(touchOpacity);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                touchOpacity = Math.max(20, progress);
                opLabel.setText("\nTouch Opacity: " + touchOpacity + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sheet.addView(sb);

        CheckBox cbLimit = new CheckBox(this);
        cbLimit.setText("Lock 60 FPS Target");
        cbLimit.setTextColor(Color.WHITE);
        cbLimit.setChecked(frameLimitEnabled);
        sheet.addView(cbLimit);

        Button exitToDash = new Button(this);
        exitToDash.setText("STOP & RETURN TO DASHBOARD");
        exitToDash.setTextColor(Color.WHITE);
        exitToDash.setTextSize(11);
        exitToDash.setBackground(createCard(Color.parseColor("#dc2626"), 8, 0, 0));
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(38));
        ep.setMargins(0, dpToPx(24), 0, 0);
        exitToDash.setLayoutParams(ep);
        exitToDash.setOnClickListener(v -> {
            d.dismiss();
            showConsoleHomeDashboard();
        });
        sheet.addView(exitToDash);

        d.setContentView(sheet);
        d.show();
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

    private void openFilePicker(String mime, int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open file picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_EXE_FILE && uri != null) {
                String name = getFileNameFromUri(uri);
                if (name == null || name.isEmpty()) name = "Game.exe";
                currentGameExe = name;
                showConfirmGameDialog(name);
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private static class ScrollScrollView extends ScrollView {
        public ScrollScrollView(Context c) { super(c); }
    }
                                      }
        
