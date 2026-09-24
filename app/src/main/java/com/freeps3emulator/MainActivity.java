package com.freeps3emulator;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PICK_EXE_FILE = 1001;
    private SharedPreferences prefs;
    private LinearLayout gamesContainer;
    private TextView emptyText;
    private String currentSelectedExe = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        prefs = getSharedPreferences("VortexPCPrefs", Context.MODE_PRIVATE);
        buildGameHubUI();
    }

    private void buildGameHubUI() {
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.parseColor("#090C10"));

        // Header Bar
        RelativeLayout header = new RelativeLayout(this);
        header.setId(View.generateViewId());
        header.setBackgroundColor(Color.parseColor("#161B22"));
        header.setPadding(30, 20, 30, 20);

        // 3-Line Menu Button (☰)
        Button btnMenu = new Button(this);
        btnMenu.setId(View.generateViewId());
        btnMenu.setText("☰");
        btnMenu.setTextSize(24);
        btnMenu.setTextColor(Color.WHITE);
        btnMenu.setBackground(createCardBg("#21262D", "#30363D", 8));
        RelativeLayout.LayoutParams pMenu = new RelativeLayout.LayoutParams(110, 80);
        pMenu.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        pMenu.addRule(RelativeLayout.CENTER_VERTICAL);
        header.addView(btnMenu, pMenu);

        btnMenu.setOnClickListener(v -> openGameHubMasterSettings());

        // App Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText("VORTEX PC EMULATOR");
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams pTitle = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pTitle.addRule(RelativeLayout.RIGHT_OF, btnMenu.getId());
        pTitle.addRule(RelativeLayout.CENTER_VERTICAL);
        pTitle.leftMargin = 25;
        header.addView(tvTitle, pTitle);

        // Top Status Info (SoC & Vulkan)
        TextView tvInfo = new TextView(this);
        String soc = Build.HARDWARE.toUpperCase();
        tvInfo.setText("Hardware: " + soc + " | Vulkan 1.3 | Proton 11");
        tvInfo.setTextColor(Color.parseColor("#8B949E"));
        tvInfo.setTextSize(12);
        RelativeLayout.LayoutParams pInfo = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pInfo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        pInfo.addRule(RelativeLayout.CENTER_VERTICAL);
        header.addView(tvInfo, pInfo);

        RelativeLayout.LayoutParams pHead = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pHead.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        root.addView(header, pHead);

        // Main Horizontal Scroll for Games (GameHub Style)
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        RelativeLayout.LayoutParams pHsv = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pHsv.addRule(RelativeLayout.BELOW, header.getId());
        pHsv.bottomMargin = 70;

        gamesContainer = new LinearLayout(this);
        gamesContainer.setOrientation(LinearLayout.HORIZONTAL);
        gamesContainer.setPadding(40, 40, 40, 40);
        gamesContainer.setGravity(Gravity.CENTER_VERTICAL);

        // "+ Import .exe" Card
        LinearLayout addCard = new LinearLayout(this);
        addCard.setOrientation(LinearLayout.VERTICAL);
        addCard.setBackground(createCardBg("#161B22", "#30363D", 16));
        addCard.setPadding(35, 35, 35, 35);
        addCard.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pCard = new LinearLayout.LayoutParams(380, 460);
        pCard.rightMargin = 30;

        TextView tvAddIcon = new TextView(this);
        tvAddIcon.setText("💻");
        tvAddIcon.setTextSize(42);
        addCard.addView(tvAddIcon);

        TextView tvAddTitle = new TextView(this);
        tvAddTitle.setText("Add PC Game");
        tvAddTitle.setTextSize(18);
        tvAddTitle.setTextColor(Color.WHITE);
        tvAddTitle.setTypeface(null, Typeface.BOLD);
        tvAddTitle.setPadding(0, 15, 0, 10);
        addCard.addView(tvAddTitle);

        Button btnImport = new Button(this);
        btnImport.setText("+ IMPORT .EXE FILE");
        btnImport.setTextColor(Color.WHITE);
        btnImport.setTextSize(12);
        btnImport.setTypeface(null, Typeface.BOLD);
        btnImport.setBackground(createCardBg("#1F6FEB", "#388BFD", 10));
        btnImport.setPadding(20, 10, 20, 10);
        btnImport.setOnClickListener(v -> openFilePicker("application/x-msdownload", PICK_EXE_FILE));
        addCard.addView(btnImport);

        gamesContainer.addView(addCard, pCard);

        emptyText = new TextView(this);
        emptyText.setText("No games added yet.\nTap '☰' for Global Master Settings or Import .exe above.");
        emptyText.setTextColor(Color.parseColor("#8B949E"));
        emptyText.setTextSize(14);
        emptyText.setGravity(Gravity.CENTER);
        gamesContainer.addView(emptyText);

        hsv.addView(gamesContainer);
        root.addView(hsv, pHsv);

        // Footer Bar
        TextView footer = new TextView(this);
        footer.setText("DXVK 2.3.1 Async • Mesa Turnip / System GPU • FEX-Emu • VC++ 2022 • PulseAudio");
        footer.setTextColor(Color.parseColor("#484F58"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams pFoot = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60);
        pFoot.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        root.addView(footer, pFoot);

        setContentView(root);
    }
        // ==========================================
    // ☰ MASTER GLOBAL SETTINGS DIALOG (GAMEHUB LITE STYLE)
    // ==========================================
    private void openGameHubMasterSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.parseColor("#0D1117"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 30, 35, 30);

        // Dialog Title
        TextView dlgTitle = new TextView(this);
        dlgTitle.setText("⚙ Global Master Emulator Settings");
        dlgTitle.setTextSize(20);
        dlgTitle.setTextColor(Color.WHITE);
        dlgTitle.setTypeface(null, Typeface.BOLD);
        dlgTitle.setPadding(0, 0, 0, 20);
        layout.addView(dlgTitle);

        // 1. General Settings
        layout.addView(createSectionHeader("1. General Settings"));
        Spinner spRes = createSpinner("Screen Resolution:", new String[]{"720p (1280x720) - Default", "960x544 (PS Vita / Balanced)", "1080p (FHD)", "800x600 (4:3 Classic)", "Custom Resolution"}, "pref_resolution", layout);
        CheckBox cbFullscreen = createCheckBox("Fullscreen Immersion", "pref_fullscreen", true, layout);
        Spinner spOrient = createSpinner("Orientation:", new String[]{"Sensor Landscape (Auto)", "Reverse Landscape", "Fixed Landscape"}, "pref_orientation", layout);

        // 2. GPU & Graphics Driver
        layout.addView(createSectionHeader("2. GPU & Graphics Driver"));
        Spinner spGpu = createSpinner("Driver Preset:", new String[]{
                "Mesa Turnip v26.2.0 (Snapdragon Adreno 7xx/8xx)",
                "Mesa Turnip v25.0 (Snapdragon Adreno 6xx)",
                "System Default Driver (Mali Dimensity / Exynos / Tensor)",
                "Zink (OpenGL over Vulkan Translation)"
        }, "pref_gpu_driver", layout);

        // 3. DirectX & Vulkan Wrappers
        layout.addView(createSectionHeader("3. DirectX & Vulkan Wrappers"));
        Spinner spDxvk = createSpinner("DXVK Direct3D Version:", new String[]{
                "DXVK 2.3.1 Async (Fastest, Shaders Stutter Fix)",
                "DXVK 3.0.2 Sync (Accurate, Visual Clarity)",
                "VKD3D-Proton 3.0.1 (DirectX 12)"
        }, "pref_dxvk_ver", layout);

        // 4. CPU & System Translators
        layout.addView(createSectionHeader("4. CPU & System Translators"));
        Spinner spProton = createSpinner("Proton Layer:", new String[]{"Proton 11 ARM64X (2026 Engine)", "Proton 10 Compatibility", "Wine 9.0 Vanilla"}, "pref_proton", layout);
        Spinner spCpu = createSpinner("CPU Translator:", new String[]{"FEX 2026 (Fix Core / Dimensity & Snapdragon)", "Box64 Dynamic Recompiler", "Box86/Box64 Hybrid"}, "pref_cpu_trans", layout);

        // 5. Performance & Memory (RAM/Swap)
        layout.addView(createSectionHeader("5. Performance & Memory (RAM/Swap)"));
        CheckBox cbEsync = createCheckBox("Esync / Fsync Multi-threading (Prevents CPU bottlenecks)", "pref_esync", true, layout);
        Spinner spSwap = createSpinner("Swap Memory Limit (ZRAM / Pagefile):", new String[]{"4GB Swap File (Recommended)", "2GB Swap File", "8GB Swap File (Heavy Games)", "Disabled"}, "pref_swap", layout);

        // 6. Audio Driver Engine
        layout.addView(createSectionHeader("6. Audio Driver Engine"));
        Spinner spAudio = createSpinner("Audio Backend:", new String[]{"PulseAudio (Low Latency / Glitch Fix)", "ALSA (Native Linux Driver)"}, "pref_audio", layout);

        // 7. Add-ons & Runtime Installer
        layout.addView(createSectionHeader("7. Add-ons & Runtime Installer"));
        CheckBox cbVcRadish = createCheckBox("Install VC++ 2022 (VC Radish Runtime)", "pref_vc_radish", true, layout);
        CheckBox cbVulkanRt = createCheckBox("Install Vulkan Runtime (Vulkan RT)", "pref_vulkan_rt", true, layout);

        // 8. Advanced Environment Variables
        layout.addView(createSectionHeader("8. Advanced Environment Variables"));
        EditText etEnv = new EditText(this);
        etEnv.setText(prefs.getString("pref_env_vars", "DXVK_ASYNC=1 MESA_EXTENSION_OVERRIDE=GL_EXT_gpu_shader4"));
        etEnv.setTextColor(Color.WHITE);
        etEnv.setTextSize(13);
        etEnv.setBackground(createCardBg("#161B22", "#30363D", 8));
        etEnv.setPadding(20, 20, 20, 20);
        layout.addView(etEnv);

        // 9. In-Game Overlay & InputBridge
        layout.addView(createSectionHeader("9. In-Game Overlay & InputBridge"));
        CheckBox cbFlowMode = createCheckBox("AI Frame Generation (Flow Mode Interpolation)", "pref_flow_mode", true, layout);
        CheckBox cbExtremeTrans = createCheckBox("Extreme Translation Presets (Aggressive JIT)", "pref_extreme_jit", true, layout);
        CheckBox cbGamepad = createCheckBox("Enable Virtual Controller Overlay (InputBridge)", "pref_gamepad", true, layout);

        // Action Buttons Row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT);
        btnRow.setPadding(0, 30, 0, 10);

        Button btnSave = new Button(this);
        btnSave.setText("SAVE CONFIGURATION");
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTypeface(null, Typeface.BOLD);
        btnSave.setBackground(createCardBg("#238636", "#2EA043", 10));

        btnRow.addView(btnSave);
        layout.addView(btnRow);

        sv.addView(layout);
        builder.setView(sv);

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            prefs.edit()
                    .putString("pref_env_vars", etEnv.getText().toString())
                    .putBoolean("pref_fullscreen", cbFullscreen.isChecked())
                    .putBoolean("pref_esync", cbEsync.isChecked())
                    .putBoolean("pref_vc_radish", cbVcRadish.isChecked())
                    .putBoolean("pref_vulkan_rt", cbVulkanRt.isChecked())
                    .putBoolean("pref_flow_mode", cbFlowMode.isChecked())
                    .putBoolean("pref_extreme_jit", cbExtremeTrans.isChecked())
                    .putBoolean("pref_gamepad", cbGamepad.isChecked())
                    .apply();

            Toast.makeText(this, "Master Settings Applied Successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
                }
        private TextView createSectionHeader(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#58A6FF"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, 25, 0, 10);
        return tv;
    }

    private Spinner createSpinner(String label, String[] items, String prefKey, LinearLayout container) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.parseColor("#C9D1D9"));
        tv.setTextSize(12);
        tv.setPadding(0, 5, 0, 5);
        container.addView(tv);

        Spinner sp = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        sp.setAdapter(adapter);
        sp.setBackground(createCardBg("#161B22", "#30363D", 8));
        container.addView(sp);
        return sp;
    }

    private CheckBox createCheckBox(String text, String prefKey, boolean defVal, LinearLayout container) {
        CheckBox cb = new CheckBox(this);
        cb.setText(text);
        cb.setTextColor(Color.parseColor("#C9D1D9"));
        cb.setTextSize(13);
        cb.setChecked(prefs.getBoolean(prefKey, defVal));
        cb.setPadding(10, 10, 10, 10);
        container.addView(cb);
        return cb;
    }

    private GradientDrawable createCardBg(String bgColor, String strokeColor, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(bgColor));
        gd.setStroke(2, Color.parseColor(strokeColor));
        gd.setCornerRadius(radius);
        return gd;
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
                currentSelectedExe = name;
                addGameCardToUI(name);
            }
        }
    }

    private void addGameCardToUI(String gameName) {
        emptyText.setVisibility(View.GONE);

        LinearLayout gameCard = new LinearLayout(this);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setBackground(createCardBg("#161B22", "#58A6FF", 16));
        gameCard.setPadding(30, 30, 30, 30);
        gameCard.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pCard = new LinearLayout.LayoutParams(380, 460);
        pCard.rightMargin = 30;

        TextView icon = new TextView(this);
        icon.setText("🎮");
        icon.setTextSize(48);
        gameCard.addView(icon);

        TextView title = new TextView(this);
        title.setText(gameName);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 15, 0, 15);
        title.setGravity(Gravity.CENTER);
        gameCard.addView(title);

        Button btnLaunch = new Button(this);
        btnLaunch.setText("▶ LAUNCH GAME");
        btnLaunch.setTextColor(Color.WHITE);
        btnLaunch.setTypeface(null, Typeface.BOLD);
        btnLaunch.setBackground(createCardBg("#238636", "#2EA043", 10));
        btnLaunch.setOnClickListener(v -> Toast.makeText(this, "Launching " + gameName + " with Global Config...", Toast.LENGTH_LONG).show());
        gameCard.addView(btnLaunch);

        gamesContainer.addView(gameCard, 1);
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
}
