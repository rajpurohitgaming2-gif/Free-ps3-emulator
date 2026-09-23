package com.freeps3emulator;

import android.content.Context;
import java.io.File;

public class HddManager {
    private final File hdd0Dir;
    private final File gameDir;

    public HddManager(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }
        hdd0Dir = new File(baseDir, "dev_hdd0");
        gameDir = new File(hdd0Dir, "game");
        
        if (!gameDir.exists()) {
            gameDir.mkdirs();
        }
    }

    public File getGameDir() {
        return gameDir;
    }

    public boolean isGameInstalled(String gameFolder) {
        File target = new File(gameDir, gameFolder);
        return target.exists() && target.isDirectory();
    }

    public String getGamePath(String gameFolder) {
        return new File(gameDir, gameFolder).getAbsolutePath();
    }
}

