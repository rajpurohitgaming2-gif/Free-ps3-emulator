package com.freeps3emulator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_GAME_FILE = 101;
    private TextView statusText;
    private Button startButton;
    private String selectedGamePath = null;

    private String selectedResolution = "720p (Native PS3)";
    private String selectedFps = "60 FPS";
    private String selectedGraphicsApi = "Vulkan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        showMainMenu();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    // नेविगेशन बार और स्टेटस बार को पूरी तरह छुपाने के लिए
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    // 1. मुख्य मेन्यू स्क्रीन
    private void showMainMenu() {
        hideSystemUI();
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0F0F14);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(40, 60, 40, 60);

        TextView titleText = new TextView(this);
        titleText.setText("FREE PS3 EMULATOR");
        titleText.setTextSize(26);
        titleText.setTextColor(0xFF00E5FF);
        titleText.setGravity(Gravity.CENTER);
        layout.addView(titleText);

        TextView subTitle = new TextView(this);
        subTitle.setText("GameHub Edition • Fullscreen Mode");
        subTitle.setTextSize(13);
        subTitle.setTextColor(0xFF888888);
        subTitle.setGravity(Gravity.CENTER);
        layout.addView(subTitle);

        statusText = new TextView(this);
        statusText.setText(selectedGamePath == null ? "\nNo game loaded yet.\n" : "\nSelected Game:\n" + selectedGamePath + "\n");
        statusText.setTextSize(15);
        statusText.setTextColor(selectedGamePath == null ? 0xFF888888 : 0xFF00E676);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        Button loadButton = new Button(this);
        loadButton.setText("📂 SELECT GAME (ISO / PKG)");
        loadButton.setTextSize(16);
        loadButton.setTextColor(Color.WHITE);
        loadButton.setBackground(createRoundBackground(0xFF1E88E5, 20, 0, 0));
        loadButton.setPadding(40, 25, 40, 25);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
        layout.addView(loadButton);

        layout.addView(createSpacer(25));

        startButton = new Button(this);
        startButton.setText("▶ START GAME");
        startButton.setTextSize(16);
        startButton.setTextColor(Color.WHITE);
        startButton.setBackground(createRoundBackground(0xFF00C853, 20, 0, 0));
        startButton.setPadding(40, 25, 40, 25);
        startButton.setVisibility(selectedGamePath == null ? View.GONE : View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGameHubScreen();
            }
        });
        layout.addView(startButton);

        layout.addView(createSpacer(25));

        Button settingsButton = new Button(this);
        settingsButton.setText("⚙ GRAPHICS & DEVICE SETTINGS");
        settingsButton.setTextSize(15);
        settingsButton.setTextColor(0xFFE0E0E0);
        settingsButton.setBackground(createRoundBackground(0xFF263238, 20, 0x55FFFFFF, 1));
        settingsButton.setPadding(40, 20, 40, 20);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsScreen();
            }
        });
        layout.addView(settingsButton);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    // 2. सेटिंग्स स्क्रीन (Graphics, Resolution & Hardware Info)
    private void showSettingsScreen() {
        hideSystemUI();
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0B0E14);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 50, 40, 60);

        TextView title = new TextView(this);
        title.setText("⚙ EMULATOR SETTINGS");
        title.setTextSize(24);
        title.setTextColor(0xFF00E5FF);
        layout.addView(title);

        layout.addView(createSpacer(25));

        LinearLayout infoCard = new LinearLayout(this);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setBackground(createRoundBackground(0xFF161B22, 16, 0x3300E5FF, 1));
        infoCard.setPadding(30, 30, 30, 30);

        TextView infoTitle = new TextView(this);
        infoTitle.setText("📱 DEVICE & HARDWARE SPECS");
        infoTitle.setTextSize(16);
        infoTitle.setTextColor(0xFF00E5FF);
        infoCard.addView(infoTitle);

        infoCard.addView(createSpacer(10));

        String deviceSpecs = "Device Model: " + Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL + "\n" +
                "Android Version: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n" +
                "CPU Cores: " + Runtime.getRuntime().availableProcessors() + " Cores Detected\n" +
                "Architecture: " + (Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "arm64-v8a") + "\n" +
                "GPU Render Mode: Hardware Accelerated (Vulkan / GLES 3.2)\n" +
                "Supported Formats: .ISO, .PKG, .BIN, .ELF";

        TextView infoDetails = new TextView(this);
        infoDetails.setText(deviceSpecs);
        infoDetails.setTextSize(13);
        infoDetails.setTextColor(0xFFB0BEC5);
        infoDetails.setLineSpacing(10, 1);
        infoCard.addView(infoDetails);
        layout.addView(infoCard);

        layout.addView(createSpacer(30));

        TextView gfxHeader = new TextView(this);
        gfxHeader.setText("🎮 GRAPHICS CONFIGURATION");
        gfxHeader.setTextSize(16);
        gfxHeader.setTextColor(0xFF76FF03);
        layout.addView(gfxHeader);

        layout.addView(createSpacer(15));

        layout.addView(createLabel("Resolution Scaling:"));
        final Spinner resSpinner = new Spinner(this);
        String[] resolutions = new String[]{"720p (Native PS3 - Balanced)", "1080p (Full HD - High End)", "2K Quad HD (Ultra GPU)", "480p (Performance - Low End)"};
        ArrayAdapter<String> resAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, resolutions);
        resSpinner.setAdapter(resAdapter);
        layout.addView(resSpinner);

        layout.addView(createSpacer(20));

        layout.addView(createLabel("Frame Rate (FPS Target):"));
        final Spinner fpsSpinner = new Spinner(this);
        String[] fpsOptions = new String[]{"60 FPS (Smooth)", "30 FPS (Battery Saver)", "Unlimited (Unlocked)"};
        ArrayAdapter<String> fpsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fpsOptions);
        fpsSpinner.setAdapter(fpsAdapter);
        layout.addView(fpsSpinner);

        layout.addView(createSpacer(20));

        layout.addView(createLabel("Graphics Backend (Driver):"));
        final Spinner apiSpinner = new Spinner(this);
        String[] apis = new String[]{"Vulkan (Recommended for Adreno & Mali)", "OpenGL ES 3.2 (Compatibility Mode)"};
        ArrayAdapter<String> apiAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, apis);
        apiSpinner.setAdapter(apiAdapter);
        layout.addView(apiSpinner);

        layout.addView(createSpacer(25));

        CheckBox vsyncCheck = new CheckBox(this);
        vsyncCheck.setText("Enable V-Sync (Prevent Screen Tearing)");
        vsyncCheck.setTextColor(Color.WHITE);
        vsyncCheck.setChecked(true);
        layout.addView(vsyncCheck);

        CheckBox multiThreadCheck = new CheckBox(this);
        multiThreadCheck.setText("Multi-Threaded CPU Emulation (Boost FPS)");
        multiThreadCheck.setTextColor(Color.WHITE);
        multiThreadCheck.setChecked(true);
        layout.addView(multiThreadCheck);

        layout.addView(createSpacer(35));

        Button saveBtn = new Button(this);
        saveBtn.setText("SAVE & APPLY");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setBackground(createRoundBackground(0xFF00C853, 16, 0, 0));
        saveBtn.setPadding(30, 20, 30, 20);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedResolution = resSpinner.getSelectedItem().toString();
                selectedFps = fpsSpinner.getSelectedItem().toString();
                selectedGraphicsApi = apiSpinner.getSelectedItem().toString();
                Toast.makeText(MainActivity.this, "Settings Applied Successfully!", Toast.LENGTH_SHORT).show();
                showMainMenu();
            }
        });
        layout.addView(saveBtn);

        layout.addView(createSpacer(15));

        Button cancelBtn = new Button(this);
        cancelBtn.setText("BACK TO MENU");
        cancelBtn.setTextColor(Color.WHITE);
        cancelBtn.setBackground(createRoundBackground(0xFF37474F, 16, 0, 0));
        cancelBtn.setPadding(30, 15, 30, 15);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
        layout.addView(cancelBtn);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    // 3. गेम स्क्रीन और टच कंट्रोलर
    private void showGameHubScreen() {
        hideSystemUI();
        RelativeLayout gameLayout = new RelativeLayout(this);
        gameLayout.setBackgroundColor(0xFF050508);

        TextView screenView = new TextView(this);
        screenView.setText("EMULATOR ACTIVE\n" +
                "Target: " + selectedResolution.split(" ")[0] + " | " + selectedFps.split(" ")[0] + " | " + selectedGraphicsApi.split(" ")[0] + "\n\n" +
                "Running: " + (selectedGamePath != null ? selectedGamePath : ""));
        screenView.setTextColor(0xFF777777);
        screenView.setTextSize(14);
        screenView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams screenParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        gameLayout.addView(screenView, screenParams);

        // L1, L2 (Top Left)
        LinearLayout leftShoulder = new LinearLayout(this);
        leftShoulder.setOrientation(LinearLayout.HORIZONTAL);
        leftShoulder.addView(createShoulderButton("L2"));
        leftShoulder.addView(createSpacerHorizontal(15));
        leftShoulder.addView(createShoulderButton("L1"));

        RelativeLayout.LayoutParams lsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lsParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lsParams.leftMargin = dpToPx(20);
        lsParams.topMargin = dpToPx(15);
        gameLayout.addView(leftShoulder, lsParams);

        // R1, R2 (Top Right)
        LinearLayout rightShoulder = new LinearLayout(this);
        rightShoulder.setOrientation(LinearLayout.HORIZONTAL);
        rightShoulder.addView(createShoulderButton("R1"));
        rightShoulder.addView(createSpacerHorizontal(15));
        rightShoulder.addView(createShoulderButton("R2"));

        RelativeLayout.LayoutParams rsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rsParams.rightMargin = dpToPx(20);
        rsParams.topMargin = dpToPx(15);
        gameLayout.addView(rightShoulder, rsParams);

        // सेंटर मेन्यू बटन
        Button btnExit = createCapsuleButton("MENU", 0x33FFFFFF);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
        RelativeLayout.LayoutParams exitParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        exitParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        exitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        exitParams.topMargin = dpToPx(15);
        gameLayout.addView(btnExit, exitParams);

        // D-PAD (Left Side)
        RelativeLayout dpadLayout = new RelativeLayout(this);
        int btnSize = dpToPx(55);

        Button up = createCircularButton("▲", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);
        Button down = createCircularButton("▼", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);
        Button left = createCircularButton("◀", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);
        Button right = createCircularButton("▶", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSize);

        up.setId(1001);
        down.setId(1002);
        left.setId(1003);
        right.setId(1004);

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
        dpadBoxParams.leftMargin = dpToPx(20);
        dpadBoxParams.bottomMargin = dpToPx(20);

        dpadLayout.addView(up, pUp);
        dpadLayout.addView(down, pDown);
        dpadLayout.addView(left, pLeft);
        dpadLayout.addView(right, pRight);
        gameLayout.addView(dpadLayout, dpadBoxParams);

        // PS सिंबल्स (△, ○, ✕, ◻ - Right Side)
        RelativeLayout actionLayout = new RelativeLayout(this);

        Button triangle = createCircularButton("△", 0x2A00E676, 0x8800E676, 0xFF00E676, btnSize);
        Button circle = createCircularButton("○", 0x2AFF1744, 0x88FF1744, 0xFFFF1744, btnSize);
        Button cross = createCircularButton("✕", 0x2A2979FF, 0x882979FF, 0xFF2979FF, btnSize);
        Button square = createCircularButton("◻", 0x2AF50057, 0x88F50057, 0xFFF50057, btnSize);

        triangle.setId(2001);
        circle.setId(2002);
        cross.setId(2003);
        square.setId(2004);

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
        actionBoxParams.rightMargin = dpToPx(20);
        actionBoxParams.bottomMargin = dpToPx(20);

        actionLayout.addView(triangle, pTri);
        actionLayout.addView(cross, pCross);
        actionLayout.addView(square, pSq);
        actionLayout.addView(circle, pCir);
        gameLayout.addView(actionLayout, actionBoxParams);

        // SELECT & START (Bottom Center)
        LinearLayout centerPills = new LinearLayout(this);
        centerPills.setOrientation(LinearLayout.HORIZONTAL);
        centerPills.addView(createCapsuleButton("SELECT", 0x2AFFFFFF));
        centerPills.addView(createSpacerHorizontal(25));
        centerPills.addView(createCapsuleButton("START", 0x2AFFFFFF));

        RelativeLayout.LayoutParams cpParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cpParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cpParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cpParams.bottomMargin = dpToPx(25);
        gameLayout.addView(centerPills, cpParams);

        setContentView(gameLayout);
    }

    private TextView createLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFFEEEEEE);
        return tv;
    }

    private View createSpacer(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dpToPx(dp)));
        return v;
    }

    private View createSpacerHorizontal(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(dp), 1));
        return v;
    }

    private Button createCircularButton(final String label, int bgColor, int strokeColor, int textColor, int size) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(20);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(createRoundBackground(bgColor, size / 2, strokeColor, 2));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           
