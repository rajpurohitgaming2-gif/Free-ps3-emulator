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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int PICK_EXE_FILE = 201;
    private static final String PREFS_NAME = "GameHub_Prefs";

    private FrameLayout rootContainer;
    private SharedPreferences prefs;
    private Vibrator vibrator;

    private ArrayList<String> installedGames = new ArrayList<>();
    private String activeGameTitle = "GTA IV";
    private String activeExeName = "GTAIV.exe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        loadGames();

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#121212"));
        setContentView(rootContainer);

        showGameHubHomeScreen();
    }

    private void loadGames() {
        installedGames.clear();
        Set<String> set = prefs.getStringSet("gamehub_games", null);
        if (set != null && !set.isEmpty()) {
            installedGames.addAll(set);
        } else {
            installedGames.add("GTA IV");
        }
    }

    private void saveGames() {
        prefs.edit().putStringSet("gamehub_games", new HashSet<>(installedGames)).apply();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void triggerFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(25);
            }
        } catch (Exception ignored) {}
    }

    private GradientDrawable createShape(int bgColor, int radiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dpToPx(radiusDp));
        if (strokeColor != 0) {
            gd.setStroke(dpToPx(strokeWidthDp), strokeColor);
        }
        return gd;
    }

    // --- GAMEHUB HOME SCREEN ---
    private void showGameHubHomeScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        rootContainer.removeAllViews();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));

        // Top Header
        RelativeLayout header = new RelativeLayout(this);
        header.setBackgroundColor(Color.parseColor("#1a1a1a"));
        header.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        TextView title = new TextView(this);
        title.setText("GameHub PC");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        Button addExeBtn = new Button(this);
        addExeBtn.setText("+ Import EXE");
        addExeBtn.setTextColor(Color.WHITE);
        addExeBtn.setTextSize(12);
        addExeBtn.setBackground(createShape(Color.parseColor("#2979ff"), 6, 0, 0));
        RelativeLayout.LayoutParams abp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(36));
        abp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        abp.addRule(RelativeLayout.CENTER_VERTICAL);
        addExeBtn.setLayoutParams(abp);
        addExeBtn.setOnClickListener(v -> openFilePicker("application/*", PICK_EXE_FILE));
        header.addView(addExeBtn);

        root.addView(header);

        // Games Grid / List
        ScrollView sv = new ScrollScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        for (String game : installedGames) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(createShape(Color.parseColor("#1e1e1e"), 10, Color.parseColor("#2c2c2c"), 1));
            card.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(14));
            card.setLayoutParams(lp);

            TextView gTitle = new TextView(this);
            gTitle.setText("🖥️ " + game);
            gTitle.setTextColor(Color.WHITE);
            gTitle.setTextSize(17);
            gTitle.setTypeface(null, Typeface.BOLD);
            card.addView(gTitle);

            TextView gSub = new TextView(this);
            gSub.setText("Executable: " + (game.equals("GTA IV") ? "GTAIV.exe" : game + ".exe") + " • Tap for Layout & Run");
            gSub.setTextColor(Color.parseColor("#80cbc4"));
            gSub.setTextSize(12);
            gSub.setPadding(0, dpToPx(4), 0, dpToPx(12));
            card.addView(gSub);

            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);

            Button layoutBtn = new Button(this);
            layoutBtn.setText("Keys and Layout");
            layoutBtn.setTextColor(Color.WHITE);
            layoutBtn.setTextSize(11);
            layoutBtn.setBackground(createShape(Color.parseColor("#333333"), 6, 0, 0));
            LinearLayout.LayoutParams lbp = new LinearLayout.LayoutParams(0, dpToPx(36), 1.0f);
            lbp.setMargins(0, 0, dpToPx(8), 0);
            layoutBtn.setLayoutParams(lbp);
            layoutBtn.setOnClickListener(v -> {
                triggerFeedback();
                activeGameTitle = game;
                showKeysAndLayoutPopup();
            });
            btnRow.addView(layoutBtn);

            Button playBtn = new Button(this);
            playBtn.setText("PLAY");
            playBtn.setTextColor(Color.WHITE);
            playBtn.setTextSize(12);
            playBtn.setTypeface(null, Typeface.BOLD);
            playBtn.setBackground(createShape(Color.parseColor("#00c853"), 6, 0, 0));
            playBtn.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(36), 1.0f));
            playBtn.setOnClickListener(v -> {
                triggerFeedback();
                activeGameTitle = game;
                startWineExecution();
            });
            btnRow.addView(playBtn);

            card.addView(btnRow);
            list.addView(card);
        }

        sv.addView(list);
        root.addView(sv);
        rootContainer.addView(root);
    }
        // --- AAPKI BHEJI PHOTO WALA KEYS & LAYOUT POPUP ---
    private void showKeysAndLayoutPopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        FrameLayout dialogRoot = new FrameLayout(this);
        dialogRoot.setBackground(createShape(Color.parseColor("#18181a"), 14, Color.parseColor("#2a2a2e"), 1));
        dialogRoot.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        // Header: < Keys and Layout + Close X
        RelativeLayout headRow = new RelativeLayout(this);
        headRow.setPadding(0, 0, 0, dpToPx(10));

        TextView backHead = new TextView(this);
        backHead.setText("❮  Keys and Layout");
        backHead.setTextColor(Color.WHITE);
        backHead.setTextSize(16);
        backHead.setTypeface(null, Typeface.BOLD);
        headRow.addView(backHead);

        TextView closeX = new TextView(this);
        closeX.setText("✕");
        closeX.setTextColor(Color.parseColor("#aaaaaa"));
        closeX.setTextSize(18);
        RelativeLayout.LayoutParams cxp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cxp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeX.setLayoutParams(cxp);
        closeX.setOnClickListener(v -> dialog.dismiss());
        headRow.addView(closeX);

        content.addView(headRow);

        // Top Tabs: Ranking, Custom, Icon Manager
        LinearLayout tabsRow = new LinearLayout(this);
        tabsRow.setOrientation(LinearLayout.HORIZONTAL);
        tabsRow.setPadding(0, dpToPx(6), 0, dpToPx(10));

        tabsRow.addView(createLayoutTabButton("📊 Ranking", false));
        tabsRow.addView(createLayoutTabButton("✎ Custom", true));
        tabsRow.addView(createLayoutTabButton("🎨 Icon Manager", false));
        content.addView(tabsRow);

        // Controller Preview Box (Photo wala dark layout box)
        RelativeLayout previewBox = new RelativeLayout(this);
        previewBox.setBackground(createShape(Color.parseColor("#28282c"), 10, Color.parseColor("#3a3a3e"), 1));
        LinearLayout.LayoutParams pbp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(150));
        pbp.setMargins(0, dpToPx(6), 0, dpToPx(10));
        previewBox.setLayoutParams(pbp);

        // Preview Controls (Icons inside preview box)
        TextView dummyJoy = new TextView(this);
        dummyJoy.setText("🕹️");
        dummyJoy.setTextSize(24);
        setAbsolutePos(dummyJoy, dpToPx(20), dpToPx(60), dpToPx(40), dpToPx(40));
        previewBox.addView(dummyJoy);

        TextView shootIco = new TextView(this);
        shootIco.setText("🎯");
        shootIco.setTextSize(18);
        setAbsoluteAlignRight(shootIco, dpToPx(30), dpToPx(25), dpToPx(30), dpToPx(30));
        previewBox.addView(shootIco);

        TextView runIco = new TextView(this);
        runIco.setText("🏃");
        runIco.setTextSize(18);
        setAbsoluteAlignRight(runIco, dpToPx(65), dpToPx(60), dpToPx(30), dpToPx(30));
        previewBox.addView(runIco);

        TextView carIco = new TextView(this);
        carIco.setText("🚗");
        carIco.setTextSize(18);
        setAbsoluteAlignRight(carIco, dpToPx(25), dpToPx(60), dpToPx(30), dpToPx(30));
        previewBox.addView(carIco);

        content.addView(previewBox);

        // Title Info: GTA IV 📱 (Tag: Game, 13.8K)
        LinearLayout infoRow = new LinearLayout(this);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);
        infoRow.setGravity(Gravity.CENTER_VERTICAL);
        infoRow.setPadding(0, dpToPx(4), 0, dpToPx(14));

        TextView titleTv = new TextView(this);
        titleTv.setText(activeGameTitle + " 📱");
        titleTv.setTextColor(Color.WHITE);
        titleTv.setTextSize(16);
        titleTv.setTypeface(null, Typeface.BOLD);
        infoRow.addView(titleTv);

        TextView tagTv = new TextView(this);
        tagTv.setText("  Game  ");
        tagTv.setTextColor(Color.parseColor("#ba68c8"));
        tagTv.setTextSize(11);
        tagTv.setBackground(createShape(Color.parseColor("#372545"), 4, 0, 0));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(dpToPx(8), 0, 0, 0);
        tagTv.setLayoutParams(tlp);
        infoRow.addView(tagTv);

        TextView downCount = new TextView(this);
        downCount.setText("  ⬇ 13.8K");
        downCount.setTextColor(Color.parseColor("#888888"));
        downCount.setTextSize(11);
        infoRow.addView(downCount);

        content.addView(infoRow);

        // 4 Action Circular Buttons: Apply, Edit, Copy, Share Code
        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.CENTER);
        actionsRow.setPadding(0, dpToPx(6), 0, dpToPx(8));

        actionsRow.addView(createCircleActionItem("⬇", "Apply", v -> {
            dialog.dismiss();
            triggerFeedback();
            startWineExecution();
        }));

        actionsRow.addView(createCircleActionItem("✎", "Edit", v -> {
            Toast.makeText(this, "Layout Editor: Drag & Reposition Icons", Toast.LENGTH_SHORT).show();
        }));

        actionsRow.addView(createCircleActionItem("❐", "Copy", v -> {
            Toast.makeText(this, "Layout Code Copied!", Toast.LENGTH_SHORT).show();
        }));

        actionsRow.addView(createCircleActionItem("↺", "Share Code", v -> {
            Toast.makeText(this, "Share Code: GH-GTA4-9982", Toast.LENGTH_SHORT).show();
        }));

        content.addView(actionsRow);

        dialogRoot.addView(content);
        dialog.setContentView(dialogRoot);

        int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }

    private Button createLayoutTabButton(String text, boolean selected) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(10);
        b.setTextColor(selected ? Color.WHITE : Color.parseColor("#aaaaaa"));
        b.setBackground(createShape(selected ? Color.parseColor("#383840") : Color.parseColor("#1f1f22"), 6, 0, 0));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dpToPx(34), 1.0f);
        p.setMargins(dpToPx(3), 0, dpToPx(3), 0);
        b.setLayoutParams(p);
        return b;
    }

    private LinearLayout createCircleActionItem(String icon, String label, View.OnClickListener l) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        col.setLayoutParams(cp);

        Button b = new Button(this);
        b.setText(icon);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setBackground(createShape(Color.parseColor("#2a2a30"), 25, Color.parseColor("#3e3e46"), 1));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
        b.setLayoutParams(bp);
        b.setOnClickListener(l);
        col.addView(b);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.parseColor("#cccccc"));
        tv.setTextSize(11);
        tv.setPadding(0, dpToPx(6), 0, 0);
        col.addView(tv);

        return col;
            }
        // --- WINE / BOX64 GAME BOOT SCREEN ---
    private void startWineExecution() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rootContainer.removeAllViews();

        LinearLayout loader = new LinearLayout(this);
        loader.setOrientation(LinearLayout.VERTICAL);
        loader.setGravity(Gravity.CENTER);
        loader.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("Launching " + activeGameTitle + " via Box64 / Wine...");
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        loader.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Executable: " + activeExeName + "\nDirectX 9 / Vulkan D8VK • Initializing Gamepad Hook");
        sub.setTextColor(Color.parseColor("#80cbc4"));
        sub.setTextSize(11);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dpToPx(6), 0, dpToPx(16));
        loader.addView(sub);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(20);
        loader.addView(pb, new LinearLayout.LayoutParams(dpToPx(320), dpToPx(10)));

        rootContainer.addView(loader);

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            int progress = 20;
            @Override
            public void run() {
                if (progress < 100) {
                    progress += 10;
                    pb.setProgress(progress);
                    h.postDelayed(this, 80);
                } else {
                    showInGameTouchScreen();
                }
            }
        });
    }

    // --- PHOTO WALA IN-GAME CONTROLLER SCREEN ---
    private void showInGameTouchScreen() {
        rootContainer.removeAllViews();

        RelativeLayout gameView = new RelativeLayout(this);
        gameView.setBackgroundColor(Color.parseColor("#0a0a0c"));

        // Top HUD
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), 0);

        TextView hud = new TextView(this);
        hud.setText(activeGameTitle.toUpperCase() + " | D8VK 60 FPS | Wine 9.0 | RAM: 1.8GB");
        hud.setTextColor(Color.GREEN);
        hud.setTextSize(11);
        hud.setTypeface(Typeface.MONOSPACE);
        topBar.addView(hud);

        Button exitBtn = new Button(this);
        exitBtn.setText("EXIT");
        exitBtn.setTextColor(Color.WHITE);
        exitBtn.setTextSize(11);
        exitBtn.setBackground(createShape(Color.parseColor("#d32f2f"), 4, 0, 0));
        RelativeLayout.LayoutParams ep = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(32));
        ep.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        exitBtn.setLayoutParams(ep);
        exitBtn.setOnClickListener(v -> {
            triggerFeedback();
            showGameHubHomeScreen();
        });
        topBar.addView(exitBtn);

        gameView.addView(topBar);

        // Photo wale custom icons (Steering, Sprint, Shoot, Aim, Joystick)
        gameView.addView(createMovableJoystick(dpToPx(35), dpToPx(25)));

        // Right side GTA Action buttons
        gameView.addView(createCustomActionButton("🎯", "AIM", dpToPx(25), dpToPx(100)));
        gameView.addView(createCustomActionButton("💥", "FIRE", dpToPx(80), dpToPx(80)));
        gameView.addView(createCustomActionButton("🏃", "SPRINT", dpToPx(25), dpToPx(35)));
        gameView.addView(createCustomActionButton("🚗", "ENTER CAR", dpToPx(80), dpToPx(20)));
        gameView.addView(createCustomActionButton("👊", "MELEE", dpToPx(135), dpToPx(35)));
        gameView.addView(createCustomActionButton("🔄", "RELOAD", dpToPx(135), dpToPx(95)));

        rootContainer.addView(gameView);
    }

    private Button createCustomActionButton(String icon, String label, int rightMargin, int bottomMargin) {
        Button b = new Button(this);
        b.setText(icon + "\n" + label);
        b.setTextSize(10);
        b.setTextColor(Color.WHITE);
        b.setBackground(createShape(Color.argb(80, 40, 40, 46), 22, Color.argb(140, 255, 255, 255), 1));
        setAbsoluteAlignBottomRight(b, rightMargin, bottomMargin, dpToPx(48), dpToPx(48));

        b.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                triggerFeedback();
                b.getBackground().setAlpha(220);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                b.getBackground().setAlpha(80);
            }
            return false;
        });

        return b;
    }

    private FrameLayout createMovableJoystick(int marginX, int marginY) {
        FrameLayout base = new FrameLayout(this);
        base.setBackground(createShape(Color.argb(30, 255, 255, 255), 55, Color.argb(80, 255, 255, 255), 1));

        View thumb = new View(this);
        thumb.setBackground(createShape(Color.argb(80, 255, 255, 255), 26, Color.WHITE, 1));
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
                activeExeName = name;
                String gameName = name.replace(".exe", "").replace(".EXE", "");
                if (!installedGames.contains(gameName)) {
                    installedGames.add(gameName);
                    saveGames();
                }
                Toast.makeText(this, "Imported: " + name, Toast.LENGTH_SHORT).show();
                showGameHubHomeScreen();
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
                
