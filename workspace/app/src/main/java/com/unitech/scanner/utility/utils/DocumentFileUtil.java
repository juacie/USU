package com.unitech.scanner.utility.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.unitech.scanner.utility.config.AllUITag;

import org.tinylog.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by USER
 */
public class DocumentFileUtil {
    private static final String mTag = DocumentFileUtil.class.getName();
    public static final String typePlain = "text/plain";

    private final Context mContext;

    public DocumentFileUtil(Context context) {
        mContext = context;
    }

    public DocumentFile getDirectory(Uri treeUri) {
        return DocumentFile.fromTreeUri(mContext, treeUri);
    }

    @SuppressLint("NewApi")
    public DocumentFile getDocumentFileByPath(String path) {
        DocumentFile file = null;

        Uri uri = Uri.fromFile(new File(path));

        mContext.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        file = DocumentFile.fromFile(new File(path));

        return file;
    }

    public OutputStream getOutputStreamFromDocumentFile(DocumentFile file) {
        OutputStream out = null;

        if (file != null) {// && file.isFile()) {
            try {
                out = mContext.getContentResolver().openOutputStream(file.getUri());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return out;
    }

    public OutputStream getOutputStreamFromUri(Uri uri) {
        OutputStream out = null;

        if (uri != null) {// && file.isFile()) {
            try {
                out = mContext.getContentResolver().openOutputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return out;
    }

    public InputStream getInputStreamFromDocumentFile(DocumentFile file) {
        InputStream in = null;

        if (file != null && file.isFile()) {
            try {
                in = mContext.getContentResolver().openInputStream(file.getUri());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return in;
    }

    public InputStream getInputStreamFromUri(Uri uri) {
        InputStream in = null;

        if (uri != null) {
            try {
                in = mContext.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return in;
    }

    public boolean createFile(DocumentFile folder, String filetype, String filename) {
        DocumentFile newFile = folder.createFile(filetype, filename);
        return newFile != null && newFile.exists() && newFile.isFile();
    }

    public boolean createDirectory(DocumentFile folder, String filename) {
        DocumentFile newFile = folder.createDirectory(filename);
        return newFile != null && newFile.exists() && newFile.isDirectory();
    }

//    public boolean deleteFile(DocumentFile file){
//        return file.delete();
//    }



    public static boolean isKitkat() {
        return false;
    }

    public static boolean isAndroid5() {
        return true;
    }

    public static String getSdCardPath() {
        String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        try {
            sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
        } catch (IOException ioe) {
            Log.e(mTag, "Could not get SD directory", ioe);
        }
        return sdCardDirectory;
    }

    @SuppressLint("NewApi")
    public static ArrayList <String> getExtSdCardPaths(Context con) {
        ArrayList <String> paths = new ArrayList <>();
        File[] files = con.getExternalFilesDirs("external");
        File firstFile = files[0];
        for (File file : files) {
            if (file != null && !file.equals(firstFile)) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    public static String getFullPathFromTreeUri(@Nullable final Uri treeUri, Context con) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri), con);
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }

    public static String getFullPathFromUri2(final Uri uri, Context con) {
        String path = null;

        try {
            String auth = uri.getAuthority();
            path = uri.getPath();
            if (path == null) {
                return null;
            }
            if (auth != null && path.contains(auth)) {
                path = path.substring(path.indexOf(auth) + auth.length());
            }

            if (path.contains("/fmrootfile")) {
                path = path.replaceAll("/fmrootfile", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    public static String getFullPathFromUri(@Nullable final Uri uri, Context con) {
        if (uri == null) {
            return null;
        }
        String volumeId = getVolumeIdFromUri(uri);
        String volumePath = getVolumePath(volumeId, con);
        if (volumePath == null) {
//            return File.separator;
            volumePath = "";
//            volumePath = String.format("/storage/%s", volumeId);
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromUri(uri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }

    private static String getVolumePath(final String volumeId, Context con) {

        try {
            StorageManager mStorageManager =
                    (StorageManager) con.getSystemService(Context.STORAGE_SERVICE);

            Class <?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);
            if (result == null) return null;
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary != null && primary && volumeId.equals("primary")) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        } else {
            return null;
        }
    }

    private static String getVolumeIdFromUri(final Uri uri) {
        try {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");

            if (split.length > 0) {
                return split[0];
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        } else {
            return File.separator;
        }
    }

    private static String getDocumentPathFromUri(final Uri uri) {
        try {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            if ((split.length >= 2) && (split[1] != null)) {
                return split[1];
            } else {
                return File.separator;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return File.separator;
        }
    }

    public boolean save(Uri uri, String storageData) {
        try {
            OutputStream oStream;
            if (uri.getPath() == null) {
                throw new Exception("URI path is null,uri=" + uri);
            }
            File file = new File(uri.getPath());
            if (file.exists()) {
                oStream = new BufferedOutputStream(new FileOutputStream(file));
            } else {
                DocumentFile documentFile = DocumentFile.fromSingleUri(mContext, uri);
                if (documentFile == null) {
                    throw new Exception("DocumentFile is null,uri=" + uri);
                }
                Logger.tag(AllUITag.documentUtil).debug(String.format("doc file canWrite=%b , canRead=%b", documentFile.canWrite(), documentFile.canRead()));
                mContext.grantUriPermission(mContext.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                ParcelFileDescriptor descriptor = mContext.getContentResolver().openFileDescriptor(uri, "w");
                if (descriptor == null) {
                    throw new Exception("ParcelFileDescriptor is null,uri=" + uri);
                }
                oStream = new FileOutputStream(descriptor.getFileDescriptor());
            }
            OutputStreamWriter outputWriter = new OutputStreamWriter(oStream, StandardCharsets.UTF_8);
            outputWriter.write(storageData);
            outputWriter.flush();
            outputWriter.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logger.error("savePreferenceToFile save fail");
        return false;
    }
}
