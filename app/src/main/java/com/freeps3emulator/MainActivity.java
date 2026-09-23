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
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    private void showMainMenu() {
        hideSystemBars();
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0F0F14);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(40, 50, 40, 50);

        TextView title = new TextView(this);
        title.setText("FREE PS3 EMULATOR");
        title.setTextSize(26);
        title.setTextColor(0xFF00E5FF);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView sub = new TextView(this);
        sub.setText("GameHub Edition • Fullscreen");
        sub.setTextSize(13);
        sub.setTextColor(0xFF888888);
        sub.setGravity(Gravity.CENTER);
        layout.addView(sub);

        statusText = new TextView(this);
        statusText.setText(selectedGamePath == null ? "\nNo game loaded yet.\n" : "\nSelected Game:\n" + selectedGamePath + "\n");
        statusText.setTextSize(15);
        statusText.setTextColor(selectedGamePath == null ? 0xFF888888 : 0xFF00E676);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        Button loadBtn = new Button(this);
        loadBtn.setText("📂 SELECT GAME (ISO / PKG)");
        loadBtn.setTextSize(16);
        loadBtn.setTextColor(Color.WHITE);
        loadBtn.setBackground(createRoundBackground(0xFF1E88E5, 20, 0, 0));
        loadBtn.setPadding(40, 25, 40, 25);
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
        layout.addView(loadBtn);

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

        Button settingsBtn = new Button(this);
        settingsBtn.setText("⚙ GRAPHICS & DEVICE SETTINGS");
        settingsBtn.setTextSize(15);
        settingsBtn.setTextColor(0xFFE0E0E0);
        settingsBtn.setBackground(createRoundBackground(0xFF263238, 20, 0x55FFFFFF, 1));
        settingsBtn.setPadding(40, 20, 40, 20);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        sv.setBackgroundColor(0xFF0B0E14);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 50);

        TextView title = new TextView(this);
        title.setText("⚙ EMULATOR SETTINGS");
        title.setTextSize(24);
        title.setTextColor(0xFF00E5FF);
        layout.addView(title);

        layout.addView(createSpacer(20));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createRoundBackground(0xFF161B22, 16, 0x3300E5FF, 1));
        card.setPadding(25, 25, 25, 25);

        TextView infoTitle = new TextView(this);
        infoTitle.setText("📱 DEVICE HARDWARE SPECS");
        infoTitle.setTextSize(15);
        infoTitle.setTextColor(0xFF00E5FF);
        card.addView(infoTitle);

        card.addView(createSpacer(10));

        String specs = "Device: " + Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL + "\n"
                + "Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\n"
                + "CPU Cores: " + Runtime.getRuntime().availableProcessors() + " Cores\n"
                + "Arch: " + (Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "arm64-v8a") + "\n"
                + "GPU: Hardware Accelerated (Vulkan / GLES 3.2)\n"
                + "Formats: .ISO, .PKG, .BIN, .ELF";

        TextView infoText = new TextView(this);
        infoText.setText(specs);
        infoText.setTextSize(13);
        infoText.setTextColor(0xFFB0BEC5);
        infoText.setLineSpacing(8, 1);
        card.addView(infoText);
        layout.addView(card);

        layout.addView(createSpacer(25));

        TextView gfx = new TextView(this);
        gfx.setText("🎮 GRAPHICS CONFIGURATION");
        gfx.setTextSize(16);
        gfx.setTextColor(0xFF76FF03);
        layout.addView(gfx);

        layout.addView(createSpacer(12));

        layout.addView(createLabel("Resolution:"));
        final Spinner resSpinner = new Spinner(this);
        String[] resOptions = new String[]{"720p (Native PS3)", "1080p (Full HD)", "2K Quad HD", "480p (Fast)"};
        resSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, resOptions));
        layout.addView(resSpinner);

        layout.addView(createSpacer(15));

        layout.addView(createLabel("Target FPS:"));
        final Spinner fpsSpinner = new Spinner(this);
        String[] fpsOptions = new String[]{"60 FPS (Smooth)", "30 FPS (Battery Saver)", "Unlimited"};
        fpsSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fpsOptions));
        layout.addView(fpsSpinner);

        layout.addView(createSpacer(15));

        layout.addView(createLabel("Graphics Driver:"));
        final Spinner apiSpinner = new Spinner(this);
        String[] apiOptions = new String[]{"Vulkan (Optimal)", "OpenGL ES 3.2"};
        apiSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, apiOptions));
        layout.addView(apiSpinner);

        layout.addView(createSpacer(20));

        CheckBox vsync = new CheckBox(this);
        vsync.setText("Enable V-Sync");
        vsync.setTextColor(Color.WHITE);
        vsync.setChecked(true);
        layout.addView(vsync);

        CheckBox multiThread = new CheckBox(this);
        multiThread.setText("Multi-Threaded CPU");
        multiThread.setTextColor(Color.WHITE);
        multiThread.setChecked(true);
        layout.addView(multiThread);

        layout.addView(createSpacer(30));

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
                Toast.makeText(MainActivity.this, "Settings Applied!", Toast.LENGTH_SHORT).show();
                showMainMenu();
            }
        });
        layout.addView(saveBtn);

        layout.addView(createSpacer(12));

        Button backBtn = new Button(this);
        backBtn.setText("BACK TO MENU");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setBackground(createRoundBackground(0xFF37474F, 16, 0, 0));
        backBtn.setPadding(30, 15, 30, 15);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
        layout.addView(backBtn);

        sv.addView(layout);
        setContentView(sv);
    }

    private void showGameHubScreen() {
        hideSystemBars();
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(0xFF050508);

        TextView screenView = new TextView(this);
        screenView.setText("EMULATOR ACTIVE\n"
                + selectedResolution.split(" ")[0] + " | " + selectedFps.split(" ")[0] + " | " + selectedGraphicsApi.split(" ")[0] + "\n\n"
                + (selectedGamePath != null ? selectedGamePath : ""));
        screenView.setTextColor(0xFF666666);
        screenView.setTextSize(14);
        screenView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams scrParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        root.addView(screenView, scrParams);

        // Top Left L1 L2
        LinearLayout lShoulder = new LinearLayout(this);
        lShoulder.setOrientation(LinearLayout.HORIZONTAL);
        lShoulder.addView(createShoulderButton("L2"));
        lShoulder.addView(createSpacerH(15));
        lShoulder.addView(createShoulderButton("L1"));
        RelativeLayout.LayoutParams lsp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lsp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lsp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lsp.leftMargin = dpToPx(20);
        lsp.topMargin = dpToPx(15);
        root.addView(lShoulder, lsp);

        // Top Right R1 R2
        LinearLayout rShoulder = new LinearLayout(this);
        rShoulder.setOrientation(LinearLayout.HORIZONTAL);
        rShoulder.addView(createShoulderButton("R1"));
        rShoulder.addView(createSpacerH(15));
        rShoulder.addView(createShoulderButton("R2"));
        RelativeLayout.LayoutParams rsp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rsp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rsp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rsp.rightMargin = dpToPx(20);
        rsp.topMargin = dpToPx(15);
        root.addView(rShoulder, rsp);

        // Top Menu
        Button menuBtn = createCapsuleButton("MENU", 0x33FFFFFF);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
        RelativeLayout.LayoutParams mp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mp.topMargin = dpToPx(15);
        root.addView(menuBtn, mp);

        // D-Pad
        RelativeLayout dpad = new RelativeLayout(this);
        int btnSz = dpToPx(55);

        Button up = createCircularButton("▲", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSz);
        Button down = createCircularButton("▼", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSz);
        Button left = createCircularButton("◀", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSz);
        Button right = createCircularButton("▶", 0x33FFFFFF, 0x55FFFFFF, Color.WHITE, btnSz);

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

        int dpadSz = dpToPx(160);
        RelativeLayout.LayoutParams dpParams = new RelativeLayout.LayoutParams(dpadSz, dpadSz);
        dpParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        dpParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dpParams.leftMargin = dpToPx(20);
        dpParams.bottomMargin = dpToPx(20);

        dpad.addView(up, pUp);
        dpad.addView(down, pDown);
        dpad.addView(left, pLeft);
        dpad.addView(right, pRight);
        root.addView(dpad, dpParams);

        // PS Buttons
        RelativeLayout actions = new RelativeLayout(this);

        Button tri = createCircularButton("△", 0x2A00E676, 0x8800E676, 0xFF00E676, btnSz);
        Button cir = createCircularButton("○", 0x2AFF1744, 0x88FF1744, 0xFFFF1744, btnSz);
        Button crs = createCircularButton("✕", 0x2A2979FF, 0x882979FF, 0xFF2979FF, btnSz);
        Button sqr = createCircularButton("◻", 0x2AF50057, 0x88F50057, 0xFFF50057, btnSz);

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

        RelativeLayout.LayoutParams actParams = new RelativeLayout.LayoutParams(dpadSz, dpadSz);
        actParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        actParams.rightMargin = dpToPx(20);
        actParams.bottomMargin = dpToPx(20);

        actions.addView(tri, pTri);
        actions.addView(crs, pCrs);
        actions.addView(sqr, pSqr);
        actions.addView(cir, pCir);
        root.addView(actions, actParams);

        // Select Start
        LinearLayout selectStart = new LinearLayout(this);
        selectStart.setOrientation(LinearLayout.HORIZONTAL);
        selectStart.addView(createCapsuleButton("SELECT", 0x2AFFFFFF));
        selectStart.addView(createSpacerH(25));
        selectStart.addView(createCapsuleButton("START", 0x2AFFFFFF));

        RelativeLayout.LayoutParams ssp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ssp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ssp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ssp.bottomMargin = dpToPx(25);
        root.addView(selectStart, ssp);

        setContentView(root);
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

    private View createSpacerH(int dp) {
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
                Toast.makeText(MainActivity.this, label + " Pressed", Toast.LENGTH_SHORT).show();
            }
        });
        return btn;
    }

    private Button createShoulderButton(final String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(13);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(createRoundBackground(0x2AFFFFFF, 12, 0x44FFFFFF, 2));
        btn.setPadding(30, 12, 30, 12);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, label + " Pressed", Toast.LENGTH_SHORT).show();
            }
        });
        return btn;
    }

    private Button createCapsuleButton(final String label, int bgColor) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(12);
        btn.setTextColor(0xFFCCCCCC);
        btn.setBackground(createRoundBackground(bgColor, 30, 0x33FFFFFF, 1));
        btn.setPadding(28, 10, 28, 10);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, label + " Pressed", Toast.LENGTH_SHORT).show();
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

     
