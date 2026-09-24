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

    private static final int PICK_EXE_FILE = 301;
    private static final String PREFS_NAME = "GameHub_Lite_Prefs";

    private FrameLayout rootContainer;
    private SharedPreferences prefs;
    private Vibrator vibrator;

    private ArrayList<String> gameTitles = new ArrayList<>();
    private String selectedGame = "Grand Theft Auto V";
    private String selectedExe = "GTAVLauncher.exe";

    private int touchOpacity = 80;
    private boolean frameLimitEnabled = true;
    private boolean frameGenEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        loadSavedGames();

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#090a0f"));
        setContentView(rootContainer);

        showConsoleHomeUI();
    }

    private void loadSavedGames() {
        gameTitles.clear();
        Set<String> saved = prefs.getStringSet("gh_games", null);
        if (saved != null && !saved.isEmpty()) {
            gameTitles.addAll(saved);
        } else {
            gameTitles.add("Grand Theft Auto V");
        }
    }

    private void saveGames() {
        prefs.edit().putStringSet("gh_games", new HashSet<>(gameTitles)).apply();
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

    private GradientDrawable createCard(int bgColor, int radiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dpToPx(radiusDp));
        if (strokeColor != 0) {
            gd.setStroke(dpToPx(strokeWidthDp), strokeColor);
        }
        return gd;
    }

    private void showConsoleHomeUI() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rootContainer.removeAllViews();

        RelativeLayout home = new RelativeLayout(this);
        home.setBackgroundColor(Color.parseColor("#090b10"));

        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setId(View.generateViewId());
        topBar.setPadding(dpToPx(24), dpToPx(14), dpToPx(24), dpToPx(10));

        TextView dashTitle = new TextView(this);
        dashTitle.setText("☰   LB  Dashboard  RB");
        dashTitle.setTextColor(Color.WHITE);
        dashTitle.setTextSize(14);
        dashTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(dashTitle);

        TextView rightStatus = new TextView(this);
        rightStatus.setText("🔍   📶  🔋 97%   15:26");
        rightStatus.setTextColor(Color.parseColor("#cccccc"));
        rightStatus.setTextSize(13);
        RelativeLayout.LayoutParams rsp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rsp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightStatus.setLayoutParams(rsp);
        topBar.addView(rightStatus);

        home.addView(topBar);

        LinearLayout centerRow = new LinearLayout(this);
        centerRow.setOrientation(LinearLayout.HORIZONTAL);
        centerRow.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams crp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        crp.addRule(RelativeLayout.BELOW, topBar.getId());
        crp.setMargins(dpToPx(24), 0, dpToPx(24), dpToPx(40));
        centerRow.setLayoutParams(crp);

        LinearLayout pcCard = new LinearLayout(this);
        pcCard.setOrientation(LinearLayout.VERTICAL);
        pcCard.setBackground(createCard(Color.parseColor("#151821"), 12, Color.parseColor("#2a3142"), 1));
        pcCard.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16));
        LinearLayout.LayoutParams pcp = new LinearLayout.LayoutParams(dpToPx(280), dpToPx(165));
        pcp.setMargins(0, 0, dpToPx(20), 0);
        pcCard.setLayoutParams(pcp);

        TextView pcLogo = new TextView(this);
        pcLogo.setText("🪟  PC Emulator");
        pcLogo.setTextColor(Color.WHITE);
        pcLogo.setTextSize(16);
        pcLogo.setTypeface(null, Typeface.BOLD);
        pcCard.addView(pcLogo);

        TextView pcSub = new TextView(this);
        pcSub.setText("Import PC games, play AAA titles on mobile");
        pcSub.setTextColor(Color.parseColor("#8b949e"));
        pcSub.setTextSize(11);
        pcSub.setPadding(0, dpToPx(4), 0, dpToPx(16));
        pcCard.addView(pcSub);

        Button importBtn = new Button(this);
        importBtn.setText("Import PC games");
        importBtn.setTextColor(Color.WHITE);
        importBtn.setTextSize(12);
        importBtn.setBackground(createCard(Color.parseColor("#2563eb"), 6, 0, 0));
        importBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(38)));
        importBtn.setOnClickListener(v -> openFilePicker("application/*", PICK_EXE_FILE));
        pcCard.addView(importBtn);

        centerRow.addView(pcCard);

        for (String g : gameTitles) {
            LinearLayout gCard = new LinearLayout(this);
            gCard.setOrientation(LinearLayout.VERTICAL);
            gCard.setBackground(createCard(Color.parseColor("#1c2230"), 12, Color.parseColor("#3b82f6"), 1));
            gCard.setPadding(dpToPx(18), dpToPx(14), dpToPx(18), dpToPx(14));
            LinearLayout.LayoutParams gcp = new LinearLayout.LayoutParams(dpToPx(240), dpToPx(165));
            gcp.setMargins(0, 0, dpToPx(16), 0);
            gCard.setLayoutParams(gcp);

            TextView gBadge = new TextView(this);
            gBadge.setText("Ⓐ Launcher Game");
            gBadge.setTextColor(Color.parseColor("#60a5fa"));
            gBadge.setTextSize(11);
            gCard.addView(gBadge);

            TextView gName = new TextView(this);
            gName.setText(g);
            gName.setTextColor(Color.WHITE);
            gName.setTextSize(16);
            gName.setTypeface(null, Typeface.BOLD);
            gName.setPadding(0, dpToPx(6), 0, dpToPx(12));
            gCard.addView(gName);

            LinearLayout bRow = new LinearLayout(this);
            bRow.setOrientation(LinearLayout.HORIZONTAL);

            Button launchBtn = new Button(this);
            launchBtn.setText("START");
            launchBtn.setTextColor(Color.WHITE);
            launchBtn.setTextSize(12);
            launchBtn.setTypeface(null, Typeface.BOLD);
            launchBtn.setBackground(createCard(Color.parseColor("#16a34a"), 6, 0, 0));
            LinearLayout.LayoutParams lbp = new LinearLayout.LayoutParams(0, dpToPx(36), 1.0f);
            lbp.setMargins(0, 0, dpToPx(8), 0);
            launchBtn.setLayoutParams(lbp);
            launchBtn.setOnClickListener(v -> {
                triggerFeedback();
                selectedGame = g;
                startComponentDownloaderScreen();
            });
            bRow.addView(launchBtn);

            Button settingsBtn = new Button(this);
            settingsBtn.setText("⚙ Settings");
            settingsBtn.setTextColor(Color.WHITE);
            settingsBtn.setTextSize(11);
            settingsBtn.setBackground(createCard(Color.parseColor("#374151"), 6, 0, 0));
            settingsBtn.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(36), 1.0f));
            settingsBtn.setOnClickListener(v -> {
                triggerFeedback();
                selectedGame = g;
                showPcGameSettingsScreen();
            });
            bRow.addView(settingsBtn);

            gCard.addView(bRow);
            centerRow.addView(gCard);
        }

        home.addView(centerRow);

        RelativeLayout bNav = new RelativeLayout(this);
        bNav.setPadding(dpToPx(24), 0, dpToPx(24), dpToPx(10));
        RelativeLayout.LayoutParams bnp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bnp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bNav.setLayoutParams(bnp);

        TextView bNavText = new TextView(this);
        bNavText.setText("Ⓨ Search    Ⓧ Menu    Ⓐ Select    Ⓑ Back");
        bNavText.setTextColor(Color.parseColor("#6b7280"));
        bNavText.setTextSize(12);
        RelativeLayout.LayoutParams bntp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bntp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        bNavText.setLayoutParams(bntp);
        bNav.addView(bNavText);

        home.addView(bNav);
        rootContainer.addView(home);
                                               }
        private void showUploadCoverDialog(String fileName) {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(createCard(Color.parseColor("#1f2430"), 12, Color.parseColor("#374151"), 1));
        root.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(18));

        TextView t = new TextView(this);
        t.setText("Upload cover image / Confirm Game");
        t.setTextColor(Color.WHITE);
        t.setTextSize(16);
        t.setTypeface(null, Typeface.BOLD);
        root.addView(t);

        EditText nameEdit = new EditText(this);
        nameEdit.setText(fileName.replace(".exe", "").replace(".EXE", ""));
        nameEdit.setTextColor(Color.WHITE);
        nameEdit.setTextSize(13);
        nameEdit.setBackground(createCard(Color.parseColor("#111827"), 6, Color.parseColor("#374151"), 1));
        nameEdit.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
        LinearLayout.LayoutParams nep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(40));
        nep.setMargins(0, dpToPx(12), 0, dpToPx(12));
        nameEdit.setLayoutParams(nep);
        root.addView(nameEdit);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.RIGHT);

        Button cancel = new Button(this);
        cancel.setText("Cancel");
        cancel.setTextColor(Color.parseColor("#9ca3af"));
        cancel.setBackgroundColor(Color.TRANSPARENT);
        cancel.setOnClickListener(v -> d.dismiss());
        row.addView(cancel);

        Button confirm = new Button(this);
        confirm.setText("Confirm");
        confirm.setTextColor(Color.WHITE);
        confirm.setBackground(createCard(Color.parseColor("#2563eb"), 6, 0, 0));
        confirm.setOnClickListener(v -> {
            d.dismiss();
            selectedGame = nameEdit.getText().toString().trim();
            if (!gameTitles.contains(selectedGame)) {
                gameTitles.add(selectedGame);
                saveGames();
            }
            startComponentDownloaderScreen();
        });
        row.addView(confirm);

        root.addView(row);
        d.setContentView(root);
        d.show();
    }

    private void startComponentDownloaderScreen() {
        rootContainer.removeAllViews();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0e1118"));

        RelativeLayout header = new RelativeLayout(this);
        header.setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(12));
        header.setBackgroundColor(Color.parseColor("#181d28"));

        TextView title = new TextView(this);
        title.setText("Download Task   |   Game Management");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header);

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(20));

        String[][] tasks = {
                {"vkd3d-2.12", "2.52MB", "100%"},
                {"Fex-20251025", "18.4MB", "100%"},
                {"Firmware (Wine 9.0)", "164.23MB", "100%"},
                {"proton10.0-arm64x-2", "206.76MB", "100%"},
                {"base", "79.56MB", "100%"},
                {"dxvk-2.3.1-async", "7.55MB", "100%"},
                {"turnip_v25.0.0_R1", "2.12MB", "100%"}
        };

        for (String[] t : tasks) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackground(createCard(Color.parseColor("#171c26"), 8, Color.parseColor("#2a3242"), 1));
            row.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(8));
            row.setLayoutParams(lp);

            RelativeLayout topR = new RelativeLayout(this);
            TextView tName = new TextView(this);
            tName.setText("📦  " + t[0] + " (" + t[1] + ")");
            tName.setTextColor(Color.WHITE);
            tName.setTextSize(13);
            topR.addView(tName);

            TextView status = new TextView(this);
            status.setText("Completed");
            status.setTextColor(Color.parseColor("#10b981"));
            status.setTextSize(11);
            RelativeLayout.LayoutParams sp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            sp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            status.setLayoutParams(sp);
            topR.addView(status);

            row.addView(topR);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress(100);
            pb.getProgressDrawable().setColorFilter(Color.parseColor("#38bdf8"), android.graphics.PorterDuff.Mode.SRC_IN);
            row.addView(pb, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6)));

            list.addView(row);
        }

        sv.addView(list);
        root.addView(sv);
        rootContainer.addView(root);

        new Handler(Looper.getMainLooper()).postDelayed(this::showInGameOverlayScreen, 1800);
    }

    private void showPcGameSettingsScreen() {
        rootContainer.removeAllViews();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#090b10"));

        RelativeLayout header = new RelativeLayout(this);
        header.setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(12));
        header.setBackgroundColor(Color.parseColor("#131722"));

        TextView back = new TextView(this);
        back.setText("← Game Settings");
        back.setTextColor(Color.WHITE);
        back.setTextSize(17);
        back.setTypeface(null, Typeface.BOLD);
        back.setOnClickListener(v -> showConsoleHomeUI());
        header.addView(back);

        root.addView(header);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout sidebar = new LinearLayout(this);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setBackgroundColor(Color.parseColor("#0f131c"));
        sidebar.setPadding(dpToPx(14), dpToPx(16), dpToPx(14), dpToPx(16));
        sidebar.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(160), LinearLayout.LayoutParams.MATCH_PARENT));

        String[] tabs = {"General", "Compatibility", "Backup / Restore", "Component", "Touch Controls", "Developer Options"};
        for (String tab : tabs) {
            TextView tv = new TextView(this);
            tv.setText(tab);
            tv.setTextColor(tab.equals("General") ? Color.WHITE : Color.parseColor("#9ca3af"));
            tv.setTextSize(13);
            tv.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12));
            if (tab.equals("General")) tv.setBackground(createCard(Color.parseColor("#1e2536"), 6, 0, 0));
            sidebar.addView(tv);
        }
        body.addView(sidebar);

        ScrollView sv = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(24));

        addSettingItem(content, "Enter Container Desktop", "Launch Wine Container desktop environment");
        addSettingItem(content, "Startup File Path", selectedExe);
        addSettingItem(content, "Language", "en_GB");
        addSettingItem(content, "Game Resolution", "1280 x 720 (16:9)");
        addSettingItem(content, "Enable MangoHUD", "Performance Overlay: ON");
        addSettingItem(content, "Compatibility Layer", "proton10.0-arm64x-2");
        addSettingItem(content, "Translation Params", "Extreme (Game Presets)");
        addSettingItem(content, "GPU Driver", "turnip_v26.1.0_b8");
        addSettingItem(content, "DXVK Version", "dxvk-v2.6.1-async");
        addSettingItem(content, "VKD3D Version", "vkd3d-proton-3.0.1");

        sv.addView(content);
        body.addView(sv);
        root.addView(body);

        rootContainer.addView(root);
    }

    private void addSettingItem(LinearLayout parent, String title, String sub) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.WHITE);
        t.setTextSize(14);
        t.setTypeface(null, Typeface.BOLD);
        row.addView(t);

        TextView s = new TextView(this);
        s.setText(sub);
        s.setTextColor(Color.parseColor("#9ca3af"));
        s.setTextSize(11);
        row.addView(s);

        parent.addView(row);
                           }
        private void showInGameOverlayScreen() {
        rootContainer.removeAllViews();

        RelativeLayout gameView = new RelativeLayout(this);
        gameView.setBackgroundColor(Color.parseColor("#05080e"));

        LinearLayout mangoHud = new LinearLayout(this);
        mangoHud.setOrientation(LinearLayout.HORIZONTAL);
        mangoHud.setBackground(createCard(Color.argb(160, 0, 0, 0), 4, 0, 0));
        mangoHud.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
        setAbsolutePos(mangoHud, dpToPx(16), dpToPx(8), ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(24));

        TextView hudTv = new TextView(this);
        hudTv.setText("GPU: 52% | CPU: 47% | RAM: 82% | D8VK 60 FPS");
        hudTv.setTextColor(Color.parseColor("#4ade80"));
        hudTv.setTextSize(10);
        hudTv.setTypeface(Typeface.MONOSPACE);
        mangoHud.addView(hudTv);
        gameView.addView(mangoHud);

        Button sideMenuTrigger = new Button(this);
        sideMenuTrigger.setText("❮ MENU");
        sideMenuTrigger.setTextColor(Color.parseColor("#9ca3af"));
        sideMenuTrigger.setTextSize(10);
        sideMenuTrigger.setBackground(createCard(Color.argb(100, 20, 20, 25), 14, Color.WHITE, 1));
        setAbsoluteAlignRight(sideMenuTrigger, dpToPx(10), dpToPx(10), dpToPx(65), dpToPx(32));
        sideMenuTrigger.setOnClickListener(v -> showGameOverlaySidebar());
        gameView.addView(sideMenuTrigger);

        addTrigger(gameView, "LT", dpToPx(25), dpToPx(20));
        addTrigger(gameView, "LB", dpToPx(25), dpToPx(55));
        addTrigger(gameView, "L3", dpToPx(25), dpToPx(95));

        addTriggerRight(gameView, "RT", dpToPx(25), dpToPx(20));
        addTriggerRight(gameView, "RB", dpToPx(25), dpToPx(55));
        addTriggerRight(gameView, "R3", dpToPx(25), dpToPx(95));

        gameView.addView(createMovableJoystick(dpToPx(35), dpToPx(20)));

        RelativeLayout actionBox = new RelativeLayout(this);
        setAbsoluteAlignBottomRight(actionBox, dpToPx(30), dpToPx(20), dpToPx(120), dpToPx(120));
        actionBox.addView(createPadBtn("Y", 40, 0));
        actionBox.addView(createPadBtn("A", 40, 80));
        actionBox.addView(createPadBtn("X", 0, 40));
        actionBox.addView(createPadBtn("B", 80, 40));
        gameView.addView(actionBox);

        rootContainer.addView(gameView);
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

    private Button createPadBtn(String text, int marginX, int marginY) {
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

    private void showGameOverlaySidebar() {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setGravity(Gravity.RIGHT);
        }

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.HORIZONTAL);
        sheet.setBackground(createCard(Color.parseColor("#151821"), 12, Color.parseColor("#2a3242"), 1));
        sheet.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

        LinearLayout iconBar = new LinearLayout(this);
        iconBar.setOrientation(LinearLayout.VERTICAL);
        iconBar.setPadding(0, 0, dpToPx(10), 0);

        TextView icoControls = new TextView(this);
        icoControls.setText("🎮\nControls");
        icoControls.setTextColor(Color.WHITE);
        icoControls.setTextSize(10);
        icoControls.setGravity(Gravity.CENTER);
        iconBar.addView(icoControls);

        TextView icoPerf = new TextView(this);
        icoPerf.setText("\n📈\nPerf");
        icoPerf.setTextColor(Color.parseColor("#9ca3af"));
        icoPerf.setTextSize(10);
        icoPerf.setGravity(Gravity.CENTER);
        iconBar.addView(icoPerf);

        sheet.addView(iconBar);

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(240), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView opLabel = new TextView(this);
        opLabel.setText("Touch Control Opacity: " + touchOpacity + "%");
        opLabel.setTextColor(Color.WHITE);
        opLabel.setTextSize(12);
        list.addView(opLabel);

        SeekBar opSeek = new SeekBar(this);
        opSeek.setMax(100);
        opSeek.setProgress(touchOpacity);
        opSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                touchOpacity = progress;
                opLabel.setText("Touch Control Opacity: " + touchOpacity + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        list.addView(opSeek);

        CheckBox cbFrame = new CheckBox(this);
        cbFrame.setText("Enable Frame Limit (60 FPS)");
        cbFrame.setTextColor(Color.WHITE);
        cbFrame.setChecked(frameLimitEnabled);
        list.addView(cbFrame);

        CheckBox cbGen = new CheckBox(this);
        cbGen.setText("Frame Generation (Flow 0.60)");
        cbGen.setTextColor(Color.WHITE);
        cbGen.setChecked(frameGenEnabled);
        list.addView(cbGen);

        Button exitGame = new Button(this);
        exitGame.setText("Exit Game to Dashboard");
        exitGame.setTextColor(Color.WHITE);
        exitGame.setTextSize(11);
        exitGame.setBackground(createCard(Color.parseColor("#dc2626"), 6, 0, 0));
        LinearLayout.LayoutParams egp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(38));
        egp.setMargins(0, dpToPx(16), 0, 0);
        exitGame.setLayoutParams(egp);
        exitGame.setOnClickListener(v -> {
            d.dismiss();
            showConsoleHomeUI();
        });
        list.addView(exitGame);

        sv.addView(list);
        sheet.addView(sv);

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
                selectedExe = name;
                showUploadCoverDialog(name);
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
        
