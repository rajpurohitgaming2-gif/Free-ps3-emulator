package com.freeps3emulator;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import java.io.File;
import java.io.FileNotFoundException;

public class PS3StorageProvider extends DocumentsProvider {

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID, Root.COLUMN_FLAGS, Root.COLUMN_TITLE,
            Root.COLUMN_DOCUMENT_ID, Root.COLUMN_ICON
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS, Document.COLUMN_SIZE
    };

    private File getGameDirectory() {
        try {
            if (getContext() == null) return null;
            File base = getContext().getExternalFilesDir(null);
            if (base == null) {
                base = getContext().getFilesDir();
            }
            File gameDir = new File(base, "dev_hdd0/game");
            if (!gameDir.exists()) {
                gameDir.mkdirs();
            }
            return gameDir;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onCreate() {
        try {
            getGameDirectory();
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        try {
            MatrixCursor.RowBuilder row = result.newRow();
            row.add(Root.COLUMN_ROOT_ID, "ps3_root");
            row.add(Root.COLUMN_DOCUMENT_ID, "root");
            row.add(Root.COLUMN_TITLE, "PS3 Emulator");
            row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_LOCAL_ONLY);
            row.add(Root.COLUMN_ICON, android.R.drawable.ic_dialog_info);
        } catch (Exception ignored) {}
        return result;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) {
        MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        try {
            MatrixCursor.RowBuilder row = result.newRow();
            row.add(Document.COLUMN_DOCUMENT_ID, documentId);
            if ("root".equals(documentId)) {
                row.add(Document.COLUMN_DISPLAY_NAME, "PS3 Storage");
                row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
                row.add(Document.COLUMN_FLAGS, Document.FLAG_DIR_SUPPORTS_CREATE);
                row.add(Document.COLUMN_SIZE, 0);
            } else {
                File file = new File(documentId);
                row.add(Document.COLUMN_DISPLAY_NAME, file.getName());
                row.add(Document.COLUMN_MIME_TYPE, file.isDirectory() ? Document.MIME_TYPE_DIR : "application/octet-stream");
                row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_DELETE);
                row.add(Document.COLUMN_SIZE, file.length());
            }
            row.add(Document.COLUMN_LAST_MODIFIED, System.currentTimeMillis());
        } catch (Exception ignored) {}
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) {
        MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        try {
            File dir = "root".equals(parentDocumentId) ? getGameDirectory() : new File(parentDocumentId);
            if (dir != null && dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        MatrixCursor.RowBuilder row = result.newRow();
                        row.add(Document.COLUMN_DOCUMENT_ID, file.getAbsolutePath());
                        row.add(Document.COLUMN_DISPLAY_NAME, file.getName());
                        row.add(Document.COLUMN_MIME_TYPE, file.isDirectory() ? Document.MIME_TYPE_DIR : "application/octet-stream");
                        row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_WRITE | Document.FLAG_SUPPORTS_DELETE);
                        row.add(Document.COLUMN_SIZE, file.length());
                        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
                    }
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        File file = new File(documentId);
        int accessMode = ParcelFileDescriptor.MODE_READ_ONLY;
        if (mode != null && mode.contains("w")) {
            accessMode = ParcelFileDescriptor.MODE_READ_WRITE;
        }
        return ParcelFileDescriptor.open(file, accessMode);
    }
}
