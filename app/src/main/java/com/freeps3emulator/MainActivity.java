package com.freeps3emulator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int PICK_PKG_FILE = 101;
    private static final int PICK_PUP_FILE = 102;
    private static final int PICK_ISO_DIR = 103;
    private static final String PREFS_NAME = "PS3_Emulator_Settings";

    private FrameLayout rootContainer;
    private SharedPreferences prefs;
    private ArrayList<String> gameTitles = new ArrayList<>();

    private int ppuThreads = 2;
    private int llvmThreads = 4;
    private boolean llvmPrecomp = true;
    private boolean spuAccurate = true;
    private int spursThreads = 6;
    private int clockScale = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadPreferences();

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#1f1f1f"));
        setContentView(rootContainer);

        showSelectGameScreen();
    }

    private void loadPreferences() {
        ppuThreads = prefs.getInt("ppu_threads", 2);
        llvmThreads = prefs.getInt("llvm_threads", 4);
        llvmPrecomp = prefs.getBoolean("llvm_precomp", true);
        spuAccurate = prefs.getBoolean("spu_accurate", true);
        spursThreads = prefs.getInt("spurs_threads", 6);
        clockScale = prefs.getInt("clock_scale", 100);
    }

    private void savePreferences() {
        prefs.edit()
                .putInt("ppu_threads", ppuThreads)
                .putInt("llvm_threads", llvmThreads)
                .putBoolean("llvm_precomp", llvmPrecomp)
                .putBoolean("spu_accurate", spuAccurate)
                .putInt("spurs_threads", spursThreads)
                .putInt("clock_scale", clockScale)
                .apply();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void showSelectGameScreen() {
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
            Toast.makeText(this, "Refreshing Game Library...", Toast.LENGTH_SHORT).show();
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
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        body.setLayoutParams(bodyParams);

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
                TextView gView = new TextView(this);
                gView.setText("🎮  " + g);
                gView.setTextColor(Color.WHITE);
                gView.setTextSize(16);
                gView.setPadding(0, dpToPx(12), 0, dpToPx(12));
                list.addView(gView);
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
                "Install Firmware",
                "Install EDAT/RAP/PKG",
                "Key Mappers",
                "User Data Manager",
                "About",
                "VirtualPadEdit",
                "Open File Manager",
                "Set (*.iso) Directory",
                "Quick Start Page",
                "Buy Emulator Premium"
        };

        for (String item : items) {
            TextView tv = new TextView(this);
            tv.setText(item);
            tv.setTextSize(15);
            tv.setPadding(0, dpToPx(10), 0, dpToPx(10));

            if (item.equals("Buy Emulator Premium")) {
                tv.setTextColor(Color.parseColor("#1a237e"));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTextColor(Color.parseColor("#212121"));
            }

            tv.setOnClickListener(v -> {
                popup.dismiss();
                handlePopupAction(item);
            });
            menuLayout.addView(tv);
        }

        popup.setContentView(menuLayout);
        popup.setWidth(dpToPx(220));
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popup.setElevation(dpToPx(8));
        }

        popup.showAsDropDown(anchor, -dpToPx(180), 0);
    }

    private void handlePopupAction(String item) {
        switch (item) {
            case "Install Firmware":
                openFilePicker("*/*", PICK_PUP_FILE);
                break;
            case "Install EDAT/RAP/PKG":
                openFilePicker("*/*", PICK_PKG_FILE);
                break;
            case "Set (*.iso) Directory":
                openFolderPicker(PICK_ISO_DIR);
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
            case "About":
                Toast.makeText(this, "Free PS3 Emulator Core v1.0", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
                break;
        }
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
                    case "Core":
                        showCoreSettingsScreen();
                        break;
                    case "Video":
                        showVideoSettingsScreen();
                        break;
                    case "Audio":
                        showAudioSettingsScreen();
                        break;
                    case "Input/Output":
                        showIOSettingsScreen();
                        break;
                    case "System":
                        showSystemSettingsScreen();
                        break;
                    case "Reset as Default":
                        Toast.makeText(this, "Settings Reset to Defaults", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, cat + " Settings Opened", Toast.LENGTH_SHORT).show();
                        break;
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
        LinearLayout content = getSettingsScrollContent();

        addSettingSubText(content, "PPU Decoder", "Recompiler (LLVM)");
        addSliderWithLabel(content, "PPU Threads", ppuThreads, 8, val -> ppuThreads = val);
        addCheckBox(content, "PPU Debug", false, null);
        addCheckBox(content, "PPU Calling History", false, null);
        addCheckBox(content, "Save LLVM logs", false, null);
        addSettingSubText(content, "Use LLVM CPU", "ARM64-v8a Architecture");
        addSliderWithLabel(content, "Max LLVM Compile Threads", llvmThreads, 16, val -> llvmThreads = val);
        addCheckBox(content, "LLVM Precompilation", llvmPrecomp, val -> llvmPrecomp = val);
        addSettingSubText(content, "Thread Scheduler Mode", "Operating System");
        addSettingSubText(content, "SPU Decoder", "Recompiler (LLVM)");
        addSliderWithLabel(content, "SPU Reservation Busy Waiting Percentage", 100, 100, null);
        addCheckBox(content, "SPU Reservation Busy Waiting Enabled", false, null);
        addSliderWithLabel(content, "Preferred SPU Threads", 0, 6, null);
        addSliderWithLabel(content, "SPU delay penalty", 3, 10, null);
        addCheckBox(content, "SPU loop detection", false, null);
        addSliderWithLabel(content, "Max SPURS Threads", spursThreads, 6, val -> spursThreads = val);
        addSettingSubText(content, "SPU Block Size", "Safe");
        addCheckBox(content, "Accurate SPU Reservations", spuAccurate, val -> spuAccurate = val);
        addCheckBox(content, "SPU Verification", true, null);
        addCheckBox(content, "SPU Cache", true, null);
        addSliderWithLabel(content, "Clocks scale", clockScale, 200, val -> clockScale = val);
    }

    private void showVideoSettingsScreen() {
        showGenericSettingsHeader("Video");
        LinearLayout content = getSettingsScrollContent();

        addSettingSubText(content, "Renderer", "Vulkan (LLE Core)");
        addSettingSubText(content, "Graphics Device", "Adreno / Mali GPU Driver");
        addSettingSubText(content, "Aspect Ratio", "16:9");
        addSettingSubText(content, "Resolution", "1280x720 (Default 720p)");
        addSliderWithLabel(content, "Resolution Scale", 100, 300, null);
        addCheckBox(content, "Write Color Buffers", false, null);
        addCheckBox(content, "Strict Rendering Mode", false, null);
        addCheckBox(content, "VSync", true, null);
        addCheckBox(content, "Frame Limit (60 FPS)", true, null);
        addSettingSubText(content, "Anti-Aliasing", "Auto / FXAA");
        addSettingSubText(content, "Anisotropic Filter", "16x");
        addCheckBox(content, "Multithreaded RSX", true, null);
    }

    private void showAudioSettingsScreen() {
        showGenericSettingsHeader("Audio");
        LinearLayout content = getSettingsScrollContent();

        addSettingSubText(content, "Audio Backend", "OpenSL ES / Oboe Low Latency");
        addSliderWithLabel(content, "Master Volume", 100, 100, null);
        addCheckBox(content, "Enable Audio DSP", true, null);
        addCheckBox(content, "Audio Buffering", true, null);
        addSliderWithLabel(content, "Audio Buffer Duration (ms)", 100, 250, null);
        addSettingSubText(content, "Audio Channels", "Stereo (Downmix 5.1)");
        addCheckBox(content, "Time Stretching (Prevent Cracking)", true, null);
    }

    private void showIOSettingsScreen() {
        showGenericSettingsHeader("Input/Output");
        LinearLayout content = getSettingsScrollContent();

        addSettingSubText(content, "Pad Handler", "Virtual Touch Pad (DualShock 3)");
        addSettingSubText(content, "Keyboard Handler", "Android Virtual Keyboard");
        addSettingSubText(content, "Mouse Handler", "Touch Absolute Cursor");
        addCheckBox(content, "Enable Vibration / Haptic Feedback", true, null);
        addSliderWithLabel(content, "Touch Stick Deadzone", 15, 50, null);
        addSliderWithLabel(content, "On-Screen Controls Opacity (%)", 45, 100, null);
        addCheckBox(content, "Auto-hide Touch Pad on Gamepad Connect", true, null);
    }

    private void showSystemSettingsScreen() {
        showGenericSettingsHeader("System");
        LinearLayout content = getSettingsScrollContent();

        addSettingSubText(content, "Console Region", "USA / Europe (Auto)");
        addSettingSubText(content, "Console Language", "English (United States)");
        addSettingSubText(content, "Firmware Version", "4.91 LLE (dev_flash)");
        addSettingSubText(content, "PS3 Model Type", "Slim (CECH-2000)");
        addCheckBox(content, "Automatic System Updates", false, null);
        addCheckBox(content, "Enable PSN Emulation (RPCN)", false, null);
        addSettingSubText(content, "Storage Root Path", "/storage/emulated/0/dev_hdd0");
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
        back.setOnClickListener(v -> {
            savePreferences();
            showMainSettingsScreen();
        });
        header.addView(back);

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
        row.setPadding(0, dpToPx(10), 0, dpToPx(10));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(16);
        row.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(sub);
        tvSub.setTextColor(Color.parseColor("#9e9e9e"));
        tvSub.setTextSize(12);
        row.addView(tvSub);

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
        row.setPadding(0, dpToPx(10), 0, dpToPx(10));

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
        LinearLayout.LayoutParams sbParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        sb.setLayoutParams(sbParams);

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

        sliderRow.addView(sb);
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
            if (requestCode == PICK_PKG_FILE || requestCode == PICK_ISO_DIR) {
                String name = (uri != null) ? getFileNameFromUri(uri) : "Custom PS3 Game";
                if (name == null || name.isEmpty()) name = "Tomb Raider Underworld [BLES00384]";
                gameTitles.add(name);
                Toast.makeText(this, "Game Mounted: " + name, Toast.LENGTH_SHORT).show();
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
