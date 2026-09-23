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
import java.io.IOException;
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
private File baseDir;
@Override
public boolean onCreate() {
baseDir = getContext().getExternalFilesDir(null);
if (baseDir == null) {
baseDir = getContext().getFilesDir();
}
File gameDir = new File(baseDir, "dev_hdd0/game");
if (!gameDir.exists()) {
gameDir.mkdirs();
}
return true;
}
@Override
public Cursor queryRoots(String[] projection) {
MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
MatrixCursor.RowBuilder row = result.newRow();
row.add(Root.COLUMN_ROOT_ID, "ps3_root");
row.add(Root.COLUMN_DOCUMENT_ID, "root");
row.add(Root.COLUMN_TITLE, "PS3 Emulator");
row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_IS_CHILD);
row.add(Root.COLUMN_ICON, android.R.mipmap.sym_def_app_icon);
return result;
}
@Override
public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
File file = getFileForDocId(documentId);
includeFile(result, file, documentId);
return result;
}
@Override
public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
File parent = getFileForDocId(parentDocumentId);
File[] files = parent.listFiles();
if (files != null) {
for (File file : files) {
includeFile(result, file, null);
}
}
return result;
}
@Override
public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
File parent = getFileForDocId(parentDocumentId);
File file = new File(parent, displayName);
try {
if (Document.MIME_TYPE_DIR.equals(mimeType)) {
if (!file.mkdirs()) {
throw new FileNotFoundException("Failed to create directory: " + file);
}
} else {
if (!file.createNewFile()) {
throw new FileNotFoundException("Failed to create file: " + file);
}
}
} catch (IOException e) {
throw new FileNotFoundException("Error creating document: " + e.getMessage());
}
return getDocIdForFile(file);
}
@Override
public void deleteDocument(String documentId) throws FileNotFoundException {
File file = getFileForDocId(documentId);
deleteRecursive(file);
}
private boolean deleteRecursive(File fileOrDirectory) {
if (fileOrDirectory.isDirectory()) {
File[] children = fileOrDirectory.listFiles();
if (children != null) {
for (File child : children) {
deleteRecursive(child);
}
}
}
return fileOrDirectory.delete();
}
@Override
public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
File file = getFileForDocId(documentId);
int accessMode = ParcelFileDescriptor.parseMode(mode);
return ParcelFileDescriptor.open(file, accessMode);
}
private File getFileForDocId(String docId) {
if ("root".equals(docId)) {
return baseDir;
}
return new File(baseDir, docId);
}
private String getDocIdForFile(File file) {
String base = baseDir.getAbsolutePath();
String path = file.getAbsolutePath();
if (path.startsWith(base)) {
if (path.length() == base.length()) {
return "root";
}
return path.substring(base.length() + 1);
}
return file.getAbsolutePath();
}
private void includeFile(MatrixCursor result, File file, String documentId) {
if (documentId == null) {
documentId = getDocIdForFile(file);
}
int flags = 0;
if (file.isDirectory()) {
flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
} else {
flags |= Document.FLAG_SUPPORTS_WRITE;
flags |= Document.FLAG_SUPPORTS_DELETE;
}
String displayName = "root".equals(documentId) ? "PS3 Storage" : file.getName();
String mimeType = file.isDirectory() ? Document.MIME_TYPE_DIR : "application/octet-stream";
MatrixCursor.RowBuilder row = result.newRow();
row.add(Document.COLUMN_DOCUMENT_ID, documentId);
row.add(Document.COLUMN_DISPLAY_NAME, displayName);
row.add(Document.COLUMN_SIZE, file.length());
row.add(Document.COLUMN_MIME_TYPE, mimeType);
row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
row.add(Document.COLUMN_FLAGS, flags);
}
}
