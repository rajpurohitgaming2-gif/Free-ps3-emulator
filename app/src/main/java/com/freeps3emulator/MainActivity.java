package com.freeps3emulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int PICK_PKG_FILE = 101;
    private static final int PICK_PUP_FILE = 102;
    private static final int PICK_ISO_DIR = 103;
    private static final int PICK_SINGLE_ISO = 104;
    private static final String PREFS_NAME = "PS3_VideoExact_Settings";

    private FrameLayout rootContainer;
    private SharedPreferences prefs;
    private Vibrator vibrator;

    private ArrayList<String> gameTitles = new ArrayList<>();
    private String activeBootGame = "Tomb Raider Underworld [BLES00384]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        loadSavedGames();

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#1f1f1f"));
        setContentView(rootContainer);

        showSelectGameScreen();
    }

    private void loadSavedGames() {
        gameTitles.clear();
        Set<String> saved = prefs.getStringSet("saved_ps3_games", null);
        if (saved != null && !saved.isEmpty()) {
            gameTitles.addAll(saved);
        }

        // dev_hdd0/game आंतरिक फ़ोल्डर ऑटो-स्कैन
        try {
            File hdd0 = new File(getExternalFilesDir(null), "dev_hdd0/game");
            if (hdd0.exists() && hdd0.isDirectory()) {
                File[] list = hdd0.listFiles();
                if (list != null) {
                    for (File f : list) {
                        if (f.isDirectory() && !gameTitles.contains(f.getName())) {
                            gameTitles.add(f.getName());
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveGamesList() {
        Set<String> set = new HashSet<>(gameTitles);
        prefs.edit().putStringSet("saved_ps3_games", set).apply();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void triggerFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(20);
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

    private void showSelectGameScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        rootContainer.removeAllViews();

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#212121"));

        RelativeLayout headerBar = new RelativeLayout(this);
        headerBar.setBackgroundColor(Color.parseColor("#191919"));
        headerBar.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        TextView title = new TextView(this);
        title.setText("Select Game");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        headerBar.addView(title);

        LinearLayout rightIcons = new LinearLayout(this);
        rightIcons.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rightIcons.setLayoutParams(iconParams);

        TextView refreshBtn = new TextView(this);
        refreshBtn.setText("⟳");
        refreshBtn.setTextColor(Color.WHITE);
        refreshBtn.setTextSize(22);
        refreshBtn.setPadding(dpToPx(10), 0, dpToPx(14), 0);
        refreshBtn.setOnClickListener(v -> {
            loadSavedGames();
            Toast.makeText(this, "Game library refreshed (" + gameTitles.size() + " found)", Toast.LENGTH_SHORT).show();
            showSelectGameScreen();
        });
        rightIcons.addView(refreshBtn);

        TextView settingsBtn = new TextView(this);
        settingsBtn.setText("🔧");
        settingsBtn.setTextColor(Color.WHITE);
        settingsBtn.setTextSize(18);
        settingsBtn.setPadding(dpToPx(6), 0, dpToPx(14), 0);
        settingsBtn.setOnClickListener(v -> showMainSettingsScreen());
        rightIcons.addView(settingsBtn);

        TextView dotsBtn = new TextView(this);
        dotsBtn.setText("⋮");
        dotsBtn.setTextColor(Color.WHITE);
        dotsBtn.setTextSize(22);
        dotsBtn.setTypeface(null, Typeface.BOLD);
        dotsBtn.setPadding(dpToPx(10), 0, dpToPx(4), 0);
        dotsBtn.setOnClickListener(v -> showThreeDotsPopup(dotsBtn));
        rightIcons.addView(dotsBtn);

        headerBar.addView(rightIcons);
        mainLayout.addView(headerBar);

        FrameLayout body = new FrameLayout(this);
        body.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        if (gameTitles.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("The list of games is empty, please add games by installing\npkg or setting the *.iso directory");
            emptyText.setTextColor(Color.parseColor("#9e9e9e"));
            emptyText.setTextSize(14);
            emptyText.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams ep = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            ep.gravity = Gravity.CENTER;
            body.addView(emptyText, ep);
        } else {
            ScrollView sv = new ScrollView(this);
            LinearLayout list = new LinearLayout(this);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            for (String g : gameTitles) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setBackground(createCard(Color.parseColor("#2a2a2a"), 6, Color.parseColor("#383838")));
                row.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, dpToPx(10));
                row.setLayoutParams(lp);

                TextView gView = new TextView(this);
                gView.setText("🎮  " + g);
                gView.setTextColor(Color.WHITE);
                gView.setTextSize(15);
                gView.setTypeface(null, Typeface.BOLD);
                row.addView(gView);

                TextView sub = new TextView(this);
                sub.setText("PS3 Disc Image (.iso/pkg) • Ready to Execute");
                sub.setTextColor(Color.parseColor("#3fb950"));
                sub.setTextSize(11);
                sub.setPadding(0, dpToPx(4), 0, 0);
                row.addView(sub);

                row.setOnClickListener(v -> {
                    triggerFeedback();
                    activeBootGame = g;
                    startPpuCompilingScreen();
                });

                list.addView(row);
            }
            sv.addView(list);
            body.addView(sv);
        }

        mainLayout.addView(body);
        rootContainer.addView(mainLayout);
    }
        private void showThreeDotsPopup(View anchor) {
        PopupWindow popup = new PopupWindow(this);
        LinearLayout menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.WHITE);
        menuLayout.setPadding(dpToPx(16), dpToPx(12), dpToPx(24), dpToPx(12));

        String[] items = new String[]{
                "Install Firmware", "Install EDAT/RAP/PKG", "Key Mappers", "User Data Manager",
                "About", "VirtualPadEdit", "Open File Manager", "Set (*.iso) Directory",
                "Quick Start Page", "Buy Emulator Premium"
        };

        for (String item : items) {
            TextView tv = new TextView(this);
            tv.setText(item);
            tv.setTextSize(15);
            tv.setPadding(0, dpToPx(10), 0, dpToPx(10));
            tv.setTextColor(item.equals("Buy Emulator Premium") ? Color.parseColor("#1a237e") : Color.parseColor("#212121"));
            if (item.equals("Buy Emulator Premium")) tv.setTypeface(null, Typeface.BOLD);

            tv.setOnClickListener(v -> {
                popup.dismiss();
                switch (item) {
                    case "Set (*.iso) Directory":
                        openFolderPicker(PICK_ISO_DIR);
                        break;
                    case "Install EDAT/RAP/PKG":
                        openFilePicker("*/*", PICK_PKG_FILE);
                        break;
                    case "Install Firmware":
                        openFilePicker("*/*", PICK_PUP_FILE);
                        break;
                    case "Key Mappers":
                        showKeyMappersScreen();
                        break;
                    case "About":
                        showAboutScreen();
                        break;
                    case "Open File Manager":
                        try {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Could not open file manager", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
            menuLayout.addView(tv);
        }

        popup.setContentView(menuLayout);
        popup.setWidth(dpToPx(220));
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popup.setElevation(dpToPx(8));
        popup.showAsDropDown(anchor, -dpToPx(180), 0);
    }

    private void showKeyMappersScreen() {
        showGenericSettingsHeader("Key Mappers");
        LinearLayout content = getSettingsScrollContent();

        String[][] keys = {
                {"left", "21"}, {"up", "19"}, {"right", "22"}, {"down", "20"},
                {"square", "99"}, {"cross", "96"}, {"circle", "97"}, {"triangle", "100"},
                {"L1", "102"}, {"L2", "104"}, {"L3", "0"}, {"R1", "103"},
                {"R2", "105"}, {"R3", "0"}, {"start", "108"}, {"select", "109"}, {"PS", "0"}
        };

        for (String[] k : keys) {
            addSettingSubText(content, k[0] + ":", k[1]);
        }
        addCheckBox(content, "Enable Vibrator", false, null);

        Button resetBtn = new Button(this);
        resetBtn.setText("RESET AS DEFAULT");
        resetBtn.setTextColor(Color.WHITE);
        resetBtn.setBackgroundColor(Color.parseColor("#333333"));
        resetBtn.setOnClickListener(v -> Toast.makeText(this, "Keymap Reset to Default", Toast.LENGTH_SHORT).show());
        content.addView(resetBtn);
    }

    private void showAboutScreen() {
        showGenericSettingsHeader("About");
        LinearLayout content = getSettingsScrollContent();

        addCheckBox(content, "Enable Log", false, null);
        addSettingSubText(content, "CPU", "[Cortex-A78*4 + Cortex-A55*4 (armv8.2-a)]:\n* fp\n* asimd\n* aes\n* pmull\n* sha1\n* sha2\n* crc32\n* atomics\n* fphp\n* asimdhp");
        addSettingSubText(content, "GPU", "[Adreno (TM) 710 (Vulkan: 1.3.284)]:\n* VK_ANDROID_external_format_resolve\n* VK_EXT_blend_operation_advanced\n* VK_EXT_border_color_swizzle\n* VK_EXT_color_write_enable\n* VK_EXT_conservative_rasterization\n* VK_EXT_custom_border_color\n* VK_EXT_depth_clamp_zero_one");

        TextView bottomNav = new TextView(this);
        bottomNav.setText("GRATITUDE   OPEN SOURCE LICENSES   UPDATE LOG");
        bottomNav.setTextColor(Color.WHITE);
        bottomNav.setTextSize(12);
        bottomNav.setTypeface(null, Typeface.BOLD);
        bottomNav.setPadding(0, dpToPx(24), 0, dpToPx(16));
        content.addView(bottomNav);
    }

    private void showMainSettingsScreen() {
        rootContainer.removeAllViews();

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#212121"));

        RelativeLayout header = new RelativeLayout(this);
        header.setBackgroundColor(Color.parseColor("#191919"));
        header.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        TextView back = new TextView(this);
        back.setText("←  Settings");
        back.setTextColor(Color.WHITE);
        back.setTextSize(20);
        back.setOnClickListener(v -> showSelectGameScreen());
        header.addView(back);

        main.addView(header);

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));

        String[] categories = new String[]{
                "Core", "Video", "Audio", "Input/Output", "System", "Savestate", "Miscellaneous", "Reset as Default"
        };

        for (String cat : categories) {
            TextView item = new TextView(this);
            item.setText(cat);
            item.setTextColor(Color.WHITE);
            item.setTextSize(17);
            item.setPadding(0, dpToPx(16), 0, dpToPx(16));

            item.setOnClickListener(v -> {
                switch (cat) {
                    case "Core": showCoreSettingsScreen(); break;
                    case "Video": showVideoSettingsScreen(); break;
                    case "Audio": showAudioSettingsScreen(); break;
                    case "Input/Output": showIOSettingsScreen(); break;
                    case "System": showSystemSettingsScreen(); break;
                    case "Savestate": showSavestateSettingsScreen(); break;
                    case "Miscellaneous": showMiscellaneousSettingsScreen(); break;
                    case "Reset as Default": showResetDefaultDialog(); break;
                }
            });
            list.addView(item);
        }

        sv.addView(list);
        main.addView(sv);
        rootContainer.addView(main);
    }

    private void showCoreSettingsScreen() {
        showGenericSettingsHeader("Core");
        LinearLayout c = getSettingsScrollContent();

        addSettingSubText(c, "PPU Decoder", "Recompiler (LLVM)");
        addSliderWithLabel(c, "PPU Threads", 2, 8, null);
        addCheckBox(c, "PPU Debug", false, null);
        addCheckBox(c, "PPU Calling History", false, null);
        addCheckBox(c, "Save LLVM logs", false, null);
        addSettingSubText(c, "Use LLVM CPU", "");
        addSliderWithLabel(c, "Max LLVM Compile Threads", 4, 16, null);
        addCheckBox(c, "LLVM Precompilation", true, null);
        addSettingSubText(c, "Thread Scheduler Mode", "Operating System");
        addSettingSubText(c, "Thread Affinity Mask", "");
        addCheckBox(c, "Set DAZ and FTZ", false, null);
        addSettingSubText(c, "SPU Decoder", "Recompiler (LLVM)");
        addSliderWithLabel(c, "SPU Reservation Busy Waiting Percentage", 100, 100, null);
        addCheckBox(c, "SPU Reservation Busy Waiting Enabled", false, null);
        addSliderWithLabel(c, "SPU GETLLAR Busy Waiting Percentage", 100, 100, null);
        addCheckBox(c, "Disable SPU GETLLAR Spin Optimization", false, null);
        addCheckBox(c, "SPU Debug", false, null);
        addCheckBox(c, "MFC Debug", false, null);
        addSliderWithLabel(c, "Preferred SPU Threads", 0, 6, null);
        addSliderWithLabel(c, "SPU delay penalty", 3, 10, null);
        addCheckBox(c, "SPU loop detection", false, null);
        addSliderWithLabel(c, "Max SPURS Threads", 6, 6, null);
        addSettingSubText(c, "SPU Block Size", "Safe");
        addCheckBox(c, "Accurate SPU DMA", false, null);
        addCheckBox(c, "Accurate SPU Reservations", true, null);
        addCheckBox(c, "Accurate Cache Line Stores", false, null);
        addCheckBox(c, "Accurate RSX reservation access", false, null);
        addSettingSubText(c, "RSX FIFO Fetch Accuracy", "Ordered & Atomic");
        addCheckBox(c, "SPU Verification", true, null);
        addCheckBox(c, "SPU Cache", true, null);
        addCheckBox(c, "SPU Profiler", false, null);
        addCheckBox(c, "PPU Profiler", false, null);
        addSliderWithLabel(c, "MFC Commands Shuffling Limit", 0, 10, null);
        addSliderWithLabel(c, "MFC Commands Timeout", 0, 100, null);
        addCheckBox(c, "MFC Commands Shuffling In Steps", false, null);
        addSettingSubText(c, "SPU XFloat Accuracy", "Approximate");
        addSliderWithLabel(c, "Accurate PPU 128-byte Reservation Op Max..", 0, 10, null);
        addSliderWithLabel(c, "Stub PPU Traps", 0, 10, null);
        addCheckBox(c, "Precise SPU Verification", false, null);
        addCheckBox(c, "PPU LLVM Java Mode Handling", true, null);
        addCheckBox(c, "PPU Vector NaN Handling", true, null);
        addCheckBox(c, "Use Accurate DFMA", true, null);
        addCheckBox(c, "PPU Set Saturation Bit", false, null);
        addCheckBox(c, "PPU Accurate Non-Java Mode", false, null);
        addCheckBox(c, "PPU Accurate Vector NaN Values", false, null);
        addCheckBox(c, "PPU Set FPCC Bits", false, null);
        addCheckBox(c, "Debug Console Mode", false, null);
        addCheckBox(c, "Hook static functions", false, null);
        addSettingSubText(c, "Libraries Control", "");
        addCheckBox(c, "HLE lwmutex", false, null);
        addSliderWithLabel(c, "Clocks scale", 100, 200, null);
        addSliderWithLabel(c, "SPU Wake-Up Delay", 0, 100, null);
        addSliderWithLabel(c, "SPU Wake-Up Delay Thread Mask", 63, 63, null);
        addSliderWithLabel(c, "Max CPU Preempt Count", 0, 10, null);
        addCheckBox(c, "Allow RSX CPU Preemptions", true, null);
        addSettingSubText(c, "Sleep Timers Accuracy", "As Host");
        addSliderWithLabel(c, "Usleep Time Addend", 0, 100, null);
        addSliderWithLabel(c, "Performance Report Threshold", 500, 1000, null);
        addCheckBox(c, "Enable Performance Report", false, null);
        addCheckBox(c, "Assume External Debugger", false, null);
    }

    private void showVideoSettingsScreen() {
        showGenericSettingsHeader("Video");
        LinearLayout c = getSettingsScrollContent();

        addSettingSubText(c, "Renderer", "Vulkan");
        addSettingSubText(c, "Resolution", "1280x720");
        addSettingSubText(c, "Aspect ratio", "16:9");
        addSettingSubText(c, "Frame limit", "Auto");
        addSliderWithLabel(c, "Second Frame Limit", 0, 120, null);
        addSettingSubText(c, "MSAA", "Auto");
        addSettingSubText(c, "Shader Mode", "");

        addClickableSubText(c, "Shader Precision", "High", v -> showRadioChoiceDialog("Shader Precision", new String[]{"Ultra", "High", "Low", "Auto"}, 1));
        addClickableSubText(c, "VSync Mode", "Disabled", v -> showRadioChoiceDialog("VSync Mode", new String[]{"Disabled", "Adaptive", "Full"}, 0));

        addCheckBox(c, "Write Color Buffers", false, null);
        addCheckBox(c, "Write Depth Buffer", false, null);
        addCheckBox(c, "Read Color Buffers", false, null);
        addCheckBox(c, "Read Depth Buffer", false, null);
        addCheckBox(c, "Handle RSX Memory Tiling", false, null);
        addCheckBox(c, "Log shader programs", false, null);
        addCheckBox(c, "Debug output", false, null);
        addCheckBox(c, "Debug overlay", false, null);
        addCheckBox(c, "Renderdoc Compatibility Mode", false, null);
        addCheckBox(c, "Use GPU texture scaling", false, null);
        addCheckBox(c, "Stretch To Display Area", false, null);
        addCheckBox(c, "Force High Precision Z buffer", false, null);
        addCheckBox(c, "Strict Rendering Mode", true, null);
        addCheckBox(c, "Disable ZCull Occlusion Queries", false, null);
        addCheckBox(c, "Disable Video Output", false, null);
        addCheckBox(c, "Disable Vertex Cache", false, null);
        addCheckBox(c, "Disable FIFO Reordering", false, null);
        addCheckBox(c, "Enable Frame Skip", false, null);
        addCheckBox(c, "Force CPU Blit", false, null);
        addCheckBox(c, "Disable On-Disk Shader Cache", false, null);
        addCheckBox(c, "Disable Vulkan Memory Allocator", false, null);
        addCheckBox(c, "Use full RGB output range", true, null);
        addCheckBox(c, "Strict Texture Flushing", false, null);
        addCheckBox(c, "Multithreaded RSX", false, null);
        addCheckBox(c, "Relaxed ZCULL Sync", false, null);
        addCheckBox(c, "Force Hardware MSAA Resolve", false, null);
        addCheckBox(c, "3D Display Enabled", false, null);
        addSettingSubText(c, "3D Display Mode", "Disabled");
        addSliderWithLabel(c, "Screen size in inches", 24, 100, null);
        addCheckBox(c, "Debug Program Analyser", false, null);
        addCheckBox(c, "Accurate ZCULL stats", true, null);
        addSliderWithLabel(c, "Consecutive Frames To Draw", 1, 10, null);
        addSliderWithLabel(c, "Consecutive Frames To Skip", 1, 10, null);
        addSliderWithLabel(c, "Resolution Scale", 100, 300, null);
        addSliderWithLabel(c, "Anisotropic Filter Override", 0, 16, null);
        addSliderWithLabel(c, "Texture LOD Bias Addend", 0, 10, null);
        addSliderWithLabel(c, "Minimum Scalable Dimension", 16, 64, null);
        addSliderWithLabel(c, "Shader Compiler Threads", 0, 16, null);
        addSliderWithLabel(c, "Driver Recovery Timeout", 1000000, 2000000, null);
        addSliderWithLabel(c, "Driver Wake-Up Delay", 0, 100, null);
        addSliderWithLabel(c, "Vblank Rate", 60, 120, null);
        addCheckBox(c, "Vblank NTSC Fixup", false, null);
        addCheckBox(c, "DECR memory layout", false, null);
        addCheckBox(c, "Allow Host GPU Labels", false, null);
        addCheckBox(c, "Disable Asynchronous Memory Mana..", false, null);
        addSettingSubText(c, "Output Scaling Mode", "Bilinear");
        addCheckBox(c, "Record With Overlays", true, null);
        addCheckBox(c, "Disable Hardware ColorSpace Remap..", false, null);
        addSliderWithLabel(c, "FidelityFX CAS Sharpening Intensity", 50, 100, null);

        addClickableSubText(c, "Vertex Buffer Upload Mode", "Auto", v -> showRadioChoiceDialog("Vertex Buffer Upload Mode", new String[]{"Auto", "Buffer View", "Buffer"}, 0));
        addClickableSubText(c, "Vulkan", "", v -> showVulkanSubScreen());
        addClickableSubText(c, "Performance Overlay", "", v -> showPerformanceOverlaySubScreen());
        addSettingSubText(c, "Shader Loading Dialog", "");
    }

    private void showVulkanSubScreen() {
        showGenericSettingsHeader("Vulkan");
        LinearLayout c = getSettingsScrollContent();

        addSettingSubText(c, "Adapter", "");
        addCheckBox(c, "Force primitive restart flag", false, null);
        addSettingSubText(c, "Exclusive Fullscreen Mode", "Automatic");
        addCheckBox(c, "Asynchronous Texture Streaming", false, null);
        addSettingSubText(c, "Asynchronous Queue Scheduler", "Safe");
        addSliderWithLabel(c, "VRAM allocation limit (MB)", 65536, 65536, null);
        addCheckBox(c, "Use Re-BAR for GPU uploads", true, null);
        addCheckBox(c, "Use Custom Driver", false, null);
        addSettingSubText(c, "Custom Driver Library Path", "");
        addCheckBox(c, "Custom Driver Force Max Clocks", false, null);
    }

    private void showPerformanceOverlaySubScreen() {
        showGenericSettingsHeader("Performance Overlay");
        LinearLayout c = getSettingsScrollContent();

        addCheckBox(c, "Enabled", false, null);
        addCheckBox(c, "Enable Framerate Graph", false, null);
        addCheckBox(c, "Enable Frametime Graph", false, null);
        addSliderWithLabel(c, "Framerate datapoints", 50, 200, null);
        addSliderWithLabel(c, "Frametime datapoints", 170, 300, null);
        addSettingSubText(c, "Detail level", "Medium");
        addSettingSubText(c, "Framerate graph detail level", "All");
        addSettingSubText(c, "Frametime graph detail level", "All");
        addSliderWithLabel(c, "Metrics update interval (ms)", 350, 1000, null);
        addSliderWithLabel(c, "Font size (px)", 10, 30, null);
        addSettingSubText(c, "Position", "Top Left");
        addSettingSubText(c, "Font", "");
        addSliderWithLabel(c, "Horizontal Margin (px)", 4, 50, null);
        addSliderWithLabel(c, "Vertical Margin (px)", 7, 50, null);
        addCheckBox(c, "Center Horizontally", false, null);
        addCheckBox(c, "Center Vertically", false, null);
        addSliderWithLabel(c, "Opacity (%)", 70, 100, null);
                    }
        private void showAudioSettingsScreen() {
        showGenericSettingsHeader("Audio");
        LinearLayout c = getSettingsScrollContent();

        addSettingSubText(c, "Renderer", "Cubeb");
        addSettingSubText(c, "Audio Provider", "CellAudio");
        addSettingSubText(c, "RSXAudio Avport", "HDMI 0");
        addCheckBox(c, "Dump to file", false, null);
        addCheckBox(c, "Convert to 16 bit", false, null);
        addSettingSubText(c, "Audio Format", "Stereo");
        addSliderWithLabel(c, "Audio Formats", 0, 10, null);
        addSettingSubText(c, "Audio Channel Layout", "Automatic");
        addSliderWithLabel(c, "Master Volume", 100, 100, null);
        addCheckBox(c, "Enable Buffering", false, null);
        addSliderWithLabel(c, "Desired Audio Buffer Duration", 34, 100, null);
        addCheckBox(c, "Enable Time Stretching", false, null);
        addCheckBox(c, "Disable Sampling Skip", false, null);
        addSliderWithLabel(c, "Time Stretching Threshold", 75, 100, null);
        addSettingSubText(c, "Microphone Type", "Null");
        addSettingSubText(c, "Music Handler", "Qt");
    }

    private void showIOSettingsScreen() {
        showGenericSettingsHeader("Input/Output");
        LinearLayout c = getSettingsScrollContent();

        addSettingSubText(c, "Pad Handler", "Virtual Pad");
        addSettingSubText(c, "Keyboard Handler", "Null");
        addSettingSubText(c, "Mouse Handler", "Null");
        addSettingSubText(c, "Camera", "Fake");
        addSettingSubText(c, "Camera type", "Unknown");
    }

    private void showSystemSettingsScreen() {
        showGenericSettingsHeader("System");
        LinearLayout c = getSettingsScrollContent();

        addSettingSubText(c, "License Area", "SCEA");
        addSettingSubText(c, "Language", "English (US)");
        addSettingSubText(c, "Keyboard Type", "English keyboard (US standard)");
        addSettingSubText(c, "Enter button assignment", "Enter with cross");
        addSettingSubText(c, "Date Format", "ddmmyyyy");
        addSettingSubText(c, "Time Format", "clock24");
        addSliderWithLabel(c, "Console time offset (s)", 0, 100, null);
        addSettingSubText(c, "Console PSID", "");
        addSettingSubText(c, "Process ARGV", "");
    }

    private void showSavestateSettingsScreen() {
        showGenericSettingsHeader("Savestate");
        LinearLayout c = getSettingsScrollContent();

        addCheckBox(c, "Start Paused", false, null);
        addCheckBox(c, "Suspend Emulation Savestate Mode", false, null);
        addCheckBox(c, "Compatible Savestate Mode", false, null);
        addCheckBox(c, "Inspection Mode Savestates", false, null);
        addCheckBox(c, "Save Disc Game Data", false, null);
        addSliderWithLabel(c, "Maximum SaveState Files", 4, 10, null);
        addSliderWithLabel(c, "Maximum SaveState Files Space (MiB)", 4096, 8192, null);
    }

    private void showMiscellaneousSettingsScreen() {
        showGenericSettingsHeader("Miscellaneous");
        LinearLayout c = getSettingsScrollContent();

        addCheckBox(c, "Automatically start games after boot", true, null);
        addCheckBox(c, "Exit RPCS3 when process finishes", false, null);
        addCheckBox(c, "Pause emulation on RPCS3 focus loss", false, null);
        addCheckBox(c, "Start games in fullscreen mode", false, null);
        addCheckBox(c, "Prevent display sleep while running ga..", true, null);
        addCheckBox(c, "Show trophy popups", true, null);
        addCheckBox(c, "Show RPCN popups", true, null);
        addCheckBox(c, "Show shader compilation hint", true, null);
        addCheckBox(c, "Show PPU compilation hint", false, null);
        addCheckBox(c, "Show autosave/autoload hint", true, null);
        addCheckBox(c, "Show pressure intensity toggle hint", true, null);
        addCheckBox(c, "Show analog limiter toggle hint", true, null);
        addCheckBox(c, "Show mouse and keyboard toggle hint", false, null);
        addCheckBox(c, "Show fatal error hints", false, null);
        addCheckBox(c, "Show capture hints", false, null);
        addCheckBox(c, "Use native user interface", true, null);
        addCheckBox(c, "Silence All Logs", false, null);
        addCheckBox(c, "Pause Emulation During Home Menu", false, null);
        addCheckBox(c, "Play music during boot sequence", true, null);
        addCheckBox(c, "Enable GameMode", false, null);
        addSettingSubText(c, "Font File Selection", "From Firmware");
        addSettingSubText(c, "Custom Font File Path", "");
    }

    // --- PPU/SPU COMPILING & IN-GAME SCREEN ---
    private void startPpuCompilingScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rootContainer.removeAllViews();

        LinearLayout compLayout = new LinearLayout(this);
        compLayout.setOrientation(LinearLayout.VERTICAL);
        compLayout.setGravity(Gravity.CENTER);
        compLayout.setBackgroundColor(Color.BLACK);

        TextView compTitle = new TextView(this);
        compTitle.setText("Compiling PPU / SPU Executables...");
        compTitle.setTextColor(Color.WHITE);
        compTitle.setTextSize(18);
        compTitle.setTypeface(null, Typeface.BOLD);
        compLayout.addView(compTitle);

        TextView compSub = new TextView(this);
        compSub.setText(activeBootGame + "\nDriver: Vulkan 1.3 LLE Core • Precaching Shaders");
        compSub.setTextColor(Color.parseColor("#8b949e"));
        compSub.setTextSize(11);
        compSub.setGravity(Gravity.CENTER);
        compSub.setPadding(0, dpToPx(6), 0, dpToPx(20));
        compLayout.addView(compSub);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(15);
        compLayout.addView(pb, new LinearLayout.LayoutParams(dpToPx(340), dpToPx(12)));

        TextView statusText = new TextView(this);
        statusText.setText("Compiling module 48 of 320 (15%)");
        statusText.setTextColor(Color.parseColor("#58a6ff"));
        statusText.setTextSize(11);
        statusText.setPadding(0, dpToPx(8), 0, 0);
        compLayout.addView(statusText);

        rootContainer.addView(compLayout);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            int progress = 15;
            @Override
            public void run() {
                if (progress < 100) {
                    progress += 6;
                    pb.setProgress(progress);
                    statusText.setText("Compiling module " + (progress * 3) + " of 320 (" + progress + "%)");
                    handler.postDelayed(this, 70);
                } else {
                    statusText.setText("Booting PS3 Core...");
                    handler.postDelayed(() -> showInGameScreen(), 200);
                }
            }
        });
    }

    private void showInGameScreen() {
        rootContainer.removeAllViews();

        RelativeLayout gameView = new RelativeLayout(this);
        gameView.setBackgroundColor(Color.parseColor("#05080e"));

        // Performance HUD & Exit
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), 0);

        TextView hud = new TextView(this);
        hud.setText(activeBootGame.toUpperCase() + "\nFPS: 59.8 | Vulkan 720p | FrameTime: 16.7ms");
        hud.setTextColor(Color.GREEN);
        hud.setTextSize(11);
        hud.setTypeface(Typeface.MONOSPACE);
        topBar.addView(hud);

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
            showSelectGameScreen();
        });
        topBar.addView(exitBtn);

        gameView.addView(topBar);

        createVirtualControls(gameView);
        rootContainer.addView(gameView);
    }

    private void createVirtualControls(RelativeLayout gameView) {
        Button ltBtn = createTriggerButton("LT");
        setAbsolutePos(ltBtn, dpToPx(30), dpToPx(30), dpToPx(55), dpToPx(32));
        gameView.addView(ltBtn);

        Button lbBtn = createTriggerButton("LB");
        setAbsolutePos(lbBtn, dpToPx(30), dpToPx(72), dpToPx(55), dpToPx(32));
        gameView.addView(lbBtn);

        Button l3Btn = createRoundButton("L3");
        setAbsolutePos(l3Btn, dpToPx(30), dpToPx(130), dpToPx(36), dpToPx(36));
        gameView.addView(l3Btn);

        Button rtBtn = createTriggerButton("RT");
        setAbsoluteAlignRight(rtBtn, dpToPx(30), dpToPx(30), dpToPx(55), dpToPx(32));
        gameView.addView(rtBtn);

        Button rbBtn = createTriggerButton("RB");
        setAbsoluteAlignRight(rbBtn, dpToPx(30), dpToPx(72), dpToPx(55), dpToPx(32));
        gameView.addView(rbBtn);

        Button r3Btn = createRoundButton("R3");
        setAbsoluteAlignRight(r3Btn, dpToPx(30), dpToPx(130), dpToPx(36), dpToPx(36));
        gameView.addView(r3Btn);

        // Movable Joysticks
        gameView.addView(createMovableJoystick(dpToPx(35), dpToPx(25), true));
        gameView.addView(createFunctionalDPad(dpToPx(155), dpToPx(35)));
        gameView.addView(createMovableJoystick(dpToPx(155), dpToPx(35), false));

        // ABXY
        RelativeLayout abxyBox = new RelativeLayout(this);
        setAbsoluteAlignBottomRight(abxyBox, dpToPx(25), dpToPx(20), dpToPx(120), dpToPx(120));
        abxyBox.addView(createABXYButton("Y", 40, 0));
        abxyBox.addView(createABXYButton("A", 40, 80));
        abxyBox.addView(createABXYButton("X", 0, 40));
        abxyBox.addView(createABXYButton("B", 80, 40));
        gameView.addView(abxyBox);

        // Center Select/Menu
        LinearLayout centerMenu = new LinearLayout(this);
        centerMenu.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cParams.setMargins(0, 0, 0, dpToPx(15));
        centerMenu.setLayoutParams(cParams);

        Button sel = createCapsuleButton("❐");
        Button menu = createCapsuleButton("☰");
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(26));
        mp.setMargins(dpToPx(8), 0, 0, 0);
        menu.setLayoutParams(mp);

        centerMenu.addView(sel);
        centerMenu.addView(menu);
        gameView.addView(centerMenu);
    }

    private FrameLayout createMovableJoystick(int marginX, int marginY, boolean isLeft) {
        FrameLayout base = new FrameLayout(this);
        base.setBackground(createCard(Color.argb(25, 255, 255, 255), 55, Color.argb(70, 255, 255, 255)));

        View thumb = new View(this);
        thumb.setBackground(createCard(Color.argb(60, 255, 255, 255), 26, Color.argb(120, 255, 255, 255)));
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

        if (isLeft) {
            setAbsoluteAlignBottomLeft(base, marginX, marginY, dpToPx(110), dpToPx(110));
        } else {
            setAbsoluteAlignBottomRight(base, marginX, marginY, dpToPx(110), dpToPx(110));
        }
        return base;
    }

    private RelativeLayout createFunctionalDPad(int leftMargin, int bottomMargin) {
        RelativeLayout dpad = new RelativeLayout(this);
        setAbsoluteAlignBottomLeft(dpad, leftMargin, bottomMargin, dpToPx(90), dpToPx(90));

        FrameLayout visualCross = new FrameLayout(this);
        View hBar = new View(this);
        hBar.setBackground(createCard(Color.argb(30, 255, 255, 255), 6, Color.argb(70, 255, 255, 255)));
        visualCross.addView(hBar, new FrameLayout.LayoutParams(dpToPx(90), dpToPx(30), Gravity.CENTER));

        View vBar = new View(this);
        vBar.setBackground(createCard(Color.argb(30, 255, 255, 255), 6, Color.argb(70, 255, 255, 255)));
        visualCross.addView(vBar, new FrameLayout.LayoutParams(dpToPx(30), dpToPx(90), Gravity.CENTER));
        dpad.addView(visualCross);

        dpad.addView(createDPadDirButton(dpToPx(30), 0));
        dpad.addView(createDPadDirButton(dpToPx(30), dpToPx(60)));
        dpad.addView(createDPadDirButton(0, dpToPx(30)));
        dpad.addView(createDPadDirButton(dpToPx(60), dpToPx(30)));
        return dpad;
    }

    private View createDPadDirButton(int x, int y) {
        View v = new View(this);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(dpToPx(30), dpToPx(30));
        p.setMargins(x, y, 0, 0);
        v.setLayoutParams(p);
        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                triggerFeedback();
                v.setBackground(createCard(Color.argb(120, 56, 139, 253), 4, Color.WHITE));
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setBackgroundColor(Color.TRANSPARENT);
            }
            return true;
        });
        return v;
    }

    private Button createTriggerButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(12);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(createCard(Color.argb(35, 255, 255, 255), 8, Color.argb(80, 255, 255, 255)));
        setupTouchHighlight(btn);
        return btn;
    }

    private Button createRoundButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#d0d0d0"));
        btn.setTextSize(11);
        btn.setBackground(createCard(Color.argb(35, 255, 255, 255), 18, Color.argb(80, 255, 255, 255)));
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
        setupTouchHighlight(btn);
        return btn;
    }

    private void setupTouchHighlight(Button btn) {
        btn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                triggerFeedback();
                btn.getBackground().setAlpha(180);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                btn.getBackground().setAlpha(50);
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

    // --- कॉमन डायलॉग्स और फ़ाइल हैंडलिंग ---
    private void showResetDefaultDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset as Default?");
        b.setMessage("Are you sure you want to reset all configurations to default?");
        b.setPositiveButton("OK", (dialog, which) -> Toast.makeText(this, "Settings Reset", Toast.LENGTH_SHORT).show());
        b.setNegativeButton("CANCEL", null);
        b.show();
    }

    private void showRadioChoiceDialog(String title, String[] items, int selectedIdx) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(title);
        b.setSingleChoiceItems(items, selectedIdx, (dialog, which) -> {
            Toast.makeText(this, items[which] + " Selected", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        b.setNegativeButton("CANCEL", null);
        b.show();
    }

    private void showGenericSettingsHeader(String titleText) {
        rootContainer.removeAllViews();
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#212121"));

        RelativeLayout header = new RelativeLayout(this);
        header.setBackgroundColor(Color.parseColor("#191919"));
        header.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        TextView back = new TextView(this);
        back.setText("←  " + titleText);
        back.setTextColor(Color.WHITE);
        back.setTextSize(20);
        back.setOnClickListener(v -> showMainSettingsScreen()
                                main.addView(header);

        ScrollView sv = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setTag("SETTINGS_CONTENT_BOX");
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(24));

        sv.addView(content);
        main.addView(sv);
        rootContainer.addView(main);
    }

    private LinearLayout getSettingsScrollContent() {
        return (LinearLayout) rootContainer.findViewWithTag("SETTINGS_CONTENT_BOX");
    }

    private void addSettingSubText(LinearLayout parent, String title, String sub) {
        if (parent == null) return;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(16);
        row.addView(tvTitle);

        if (!sub.isEmpty()) {
            TextView tvSub = new TextView(this);
            tvSub.setText(sub);
            tvSub.setTextColor(Color.parseColor("#9e9e9e"));
            tvSub.setTextSize(12);
            row.addView(tvSub);
        }
        parent.addView(row);
    }

    private void addClickableSubText(LinearLayout parent, String title, String sub, View.OnClickListener listener) {
        if (parent == null) return;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(16);
        row.addView(tvTitle);

        if (!sub.isEmpty()) {
            TextView tvSub = new TextView(this);
            tvSub.setText(sub);
            tvSub.setTextColor(Color.parseColor("#9e9e9e"));
            tvSub.setTextSize(12);
            row.addView(tvSub);
        }
        row.setOnClickListener(listener);
        parent.addView(row);
    }

    private void addCheckBox(LinearLayout parent, String title, boolean checked, ValueChangeCallback<Boolean> cb) {
        if (parent == null) return;
        CheckBox cbView = new CheckBox(this);
        cbView.setText(title);
        cbView.setTextColor(Color.WHITE);
        cbView.setTextSize(15);
        cbView.setChecked(checked);
        cbView.setPadding(0, dpToPx(8), 0, dpToPx(8));
        cbView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cb != null) cb.onChange(isChecked);
        });
        parent.addView(cbView);
    }

    private void addSliderWithLabel(LinearLayout parent, String title, int initial, int max, ValueChangeCallback<Integer> cb) {
        if (parent == null) return;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(15);
        row.addView(tv);

        LinearLayout sliderRow = new LinearLayout(this);
        sliderRow.setOrientation(LinearLayout.HORIZONTAL);
        sliderRow.setGravity(Gravity.CENTER_VERTICAL);

        SeekBar sb = new SeekBar(this);
        sb.setMax(max);
        sb.setProgress(initial);
        sliderRow.addView(sb, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView valTv = new TextView(this);
        valTv.setText(String.valueOf(initial));
        valTv.setTextColor(Color.WHITE);
        valTv.setTextSize(14);
        valTv.setPadding(dpToPx(12), 0, 0, 0);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valTv.setText(String.valueOf(progress));
                if (cb != null) cb.onChange(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sliderRow.addView(valTv);
        row.addView(sliderRow);
        parent.addView(row);
    }

    interface ValueChangeCallback<T> {
        void onChange(T val);
    }

    private void openFilePicker(String mime, int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mime);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open file picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFolderPicker(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open folder picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_ISO_DIR && uri != null) {
                // पूरे फ़ोल्डर में मौजूद सभी .ISO को स्कैन करना
                try {
                    DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
                    if (pickedDir != null && pickedDir.isDirectory()) {
                        int count = 0;
                        for (DocumentFile file : pickedDir.listFiles()) {
                            String name = file.getName();
                            if (name != null && (name.toLowerCase().endsWith(".iso") || name.toLowerCase().endsWith(".pkg"))) {
                                if (!gameTitles.contains(name)) {
                                    gameTitles.add(name);
                                    count++;
                                }
                            }
                        }
                        saveGamesList();
                        Toast.makeText(this, "Mounted " + count + " game(s) from directory", Toast.LENGTH_SHORT).show();
                        showSelectGameScreen();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error scanning directory", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == PICK_PKG_FILE || requestCode == PICK_SINGLE_ISO) {
                String name = (uri != null) ? getFileNameFromUri(uri) : "Custom PS3 Game.iso";
                if (name == null || name.isEmpty()) name = "PS3 Game.iso";
                if (!gameTitles.contains(name)) {
                    gameTitles.add(name);
                    saveGamesList();
                }
                Toast.makeText(this, "Game Loaded: " + name, Toast.LENGTH_SHORT).show();
                showSelectGameScreen();
            } else if (requestCode == PICK_PUP_FILE) {
                Toast.makeText(this, "PS3 Firmware (PUP) Installed to dev_flash!", Toast.LENGTH_LONG).show();
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
        }
