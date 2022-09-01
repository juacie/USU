package com.unitech.scanner.utility.utils;


import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.unitech.scanner.utility.config.AllUITag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unitech.scanner.utility.config.App.mUriArray;

public class IOUtil {
    private static final String mTAG = "Util_IO";

    //----------------------------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean fileWriteFromURI(Context context, String filePath, String writeDown) throws Exception {
        Uri mUri;
        DocumentFileUtil documentFileUtil = new DocumentFileUtil(context);
        File file = new File(filePath);
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume;

        volume = storageManager.getStorageVolume(file);

        if (volume == null) {
            String path = file.getPath();
            List <StorageVolume> vols = storageManager.getStorageVolumes();
            if (vols.size() > 0) {
                for (StorageVolume sv : vols) {
                    Logger.tag(AllUITag.savePreference).debug(String.format("sv %s : %s, removable: %b, primary: %b, isEmulated: %b",
                            sv.getUuid(), sv.getState(), sv.isRemovable(), sv.isPrimary(), sv.isEmulated()));

                    if (sv.getUuid() != null && sv.isRemovable() && path.contains(sv.getUuid())) {
                        Logger.tag(AllUITag.savePreference).debug(String.format("Found target volume %s", sv.getUuid()));
                        volume = sv;
                    }
                }
            }
        }
        if (volume != null) {
            mUri = Uri.fromFile(file);
        } else {
            Logger.tag(AllUITag.savePreference).error("volume = null");
            throw new Exception("USU no external filepath.");
        }
        if (mUriArray.size() < 1) {
            Logger.tag(AllUITag.savePreference).error("mUriArray.size() < 1");
            throw new Exception("USU no external filepath.");
        }
        Logger.tag(AllUITag.savePreference).debug("mUriArray = " + mUriArray);
        for (Uri uri : mUriArray) {
            if (uri.getPath() == null || mUri.getPath() == null) {
                continue;
            }
            String[] tmpUriArray = uri.getPath().replace(":", "").split("/");
            String[] tmpmUriArray = mUri.getPath().split("/");
            Logger.tag(AllUITag.savePreference).debug("tmpUriArray = " + Arrays.toString(tmpUriArray));
            Logger.tag(AllUITag.savePreference).debug("tmpmUriArray = " + Arrays.toString(tmpmUriArray));
            if (!tmpUriArray[2].equals(tmpmUriArray[2])) {
                continue;
            }
            DocumentFile treeUri = DocumentFile.fromTreeUri(context, uri);
            if (treeUri == null) {
                continue;
            }
            Logger.tag(AllUITag.savePreference).debug("onActivityResult mUri = " + mUri);
            String realPath = mUri.getPath();
            String storage = volume.getUuid();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(realPath);
            if (storage == null) {
                Logger.error("savePreferenceToFile storage == null");
                throw new Exception("The external filepath UUID is null.");
            }
            int idx = stringBuilder.indexOf(storage);
            stringBuilder.replace(0, idx + storage.length() + 1, "");    //plus 1 to replace '/' char
            Logger.tag(AllUITag.savePreference).debug("onActivityResult stringBuilder = " + stringBuilder.toString());
            String fileName = stringBuilder.toString();
            if (fileName.equals("")) {
                Logger.tag(AllUITag.savePreference).error("onActivityResult stringBuilder = null");
                throw new Exception("There is no file name.");
            }
            DocumentFile documentFile = treeUri;

            if (fileName.contains("/")) {
                String[] spiltD = fileName.split("/");
                Logger.tag(AllUITag.savePreference).debug("onActivityResult spiltD = " + Arrays.toString(spiltD));
                if (spiltD.length < 2) {
                    Logger.tag(AllUITag.savePreference).error("onActivityResult filepath spilt < 2");
                    throw new Exception("The file path is not correct.");
                }
                for (int i = 0; i < spiltD.length - 1; i++) {
                    if (documentFile == null) {
                        break;
                    }
                    DocumentFile tmpFile = documentFile.findFile(spiltD[i]);
                    if (tmpFile == null) {
                        tmpFile = documentFile;
                    }
                    if (tmpFile.getUri() == documentFile.getUri()) {
                        if (documentFileUtil.createDirectory(documentFile, spiltD[i])) {
                            documentFile = documentFile.findFile(spiltD[i]);
                        } else {
                            Logger.tag(AllUITag.savePreference).error("onActivityResult createDirectory error");
                            throw new Exception("Create directory is getting error.");
                        }
                    } else {
                        documentFile = tmpFile;
                    }
                }
                fileName = spiltD[spiltD.length - 1];
            }
            Logger.tag(AllUITag.savePreference).debug("onActivityResult fileName= " + fileName);
            if (documentFile == null) {
                Logger.tag(AllUITag.savePreference).error("onActivityResult documentFile error");
                throw new Exception("Document file is null.");
            }
            DocumentFile fileLast = documentFile.findFile(fileName);
            if (fileLast == null) {
                fileLast = documentFile;
            }
            Logger.tag(AllUITag.savePreference).debug("onActivityResult fileLast= " + fileLast.getUri());
            Logger.tag(AllUITag.savePreference).debug("onActivityResult documentFile = " + documentFile.getUri());
            if (fileLast.getUri() == documentFile.getUri()) {
                if (documentFileUtil.createFile(documentFile, "text/*", fileName)) {
                    fileLast = documentFile.findFile(fileName);
                } else {
                    Logger.tag(AllUITag.savePreference).error("onActivityResult createFile error");
                    throw new Exception("Create file got error.");
                }
            }
            if (fileLast == null) {
                Logger.tag(AllUITag.savePreference).error("onActivityResult fileLast error");
                throw new Exception("Can't find creation file.");
            }
            Logger.tag(AllUITag.savePreference).debug("onActivityResult Path:" + fileLast.getUri());

            if (documentFileUtil.save(fileLast.getUri(), writeDown)) {
                return true;
            }
        }
        return false;
    }



    public static boolean fileWrite(String filepath, String str, boolean append) {
        try {
            FileWriter fw = new FileWriter(filepath, append);
            fw.write(str);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean fileWrite(String filepath, byte[] str, boolean append) {
        try {
            OutputStream fw = new FileOutputStream(filepath, append);
            fw.write(str);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    @Nullable
    public static String readFile(String storage) {
        try {
            FileReader fr = new FileReader(storage);
            BufferedReader br = new BufferedReader(fr);
            List <Byte> readData = new ArrayList <>();
            byte temp;
            while ((temp = (byte)br.read()) != -1) {
                readData.add(temp);
            }
            br.close();
            fr.close();
            byte[] mDataBytes = new byte[readData.size()];
            for(int i = 0;i<mDataBytes.length;i++){
                mDataBytes[i] = readData.get(i);
            }
            return new String(mDataBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //----------------------------------------------------------------------------------------------
    public static boolean fileExist(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean mkdir(File file) {
        ArrayList <File> Path = new ArrayList <>();
        while (file.getParentFile() != null && !file.exists()) {
            Path.add(file);
            file = file.getParentFile();
        }
        //Escape the file dir
        if (Path.size() > 1) {
            for (int i = Path.size() - 1; i > 0; i--) {
                if (!Path.get(i).mkdir()) {
                    Logger.debug( Path.get(i).getAbsolutePath() + " mkdir fail.");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
    //----------------------------------------------------------------------------------------------
    public void forceDeleteDir(File dir_target) {
        if (dir_target.exists()) {
            if (dir_target.isDirectory()) {
                String[] fileList = dir_target.list();

                if (fileList != null) {
                    for (String aFileList : fileList) {
                        String sFile = dir_target.getPath() + File.separator + aFileList;
                        File tmp = new File(sFile);
                        if (tmp.isFile()) {
                            if (tmp.delete()) {
                                Logger.debug("Delete " + sFile);
                            }
                        }
                        if (tmp.isDirectory()) {
                            forceDeleteDir(new File(sFile));
                        }
                    }
                }
            }
            if (dir_target.delete()) {
                Logger.debug("Delete " + dir_target);
            }
        }
    }

}
