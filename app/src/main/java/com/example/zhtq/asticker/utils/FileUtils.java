package com.example.zhtq.asticker.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class FileUtils {
    public static final String TAG = FileUtils.class.getSimpleName();
    public static final String EXT_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String THUMBNAIL_SAVED_PATH = "DCIM/.thumbnails/";
    public static final String ROOT_DIR = "/ASticker";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * combine several parts into one standard path string.
     * @param parts must be in order.
     * @return
     */
    public static String combine(String... parts) {
        String combinedStr;
        if (parts != null && parts.length > 0) {
            combinedStr = "";
            for (String str : parts) {
                if (!TextUtils.isEmpty(combinedStr) && !str.startsWith("/")) {
                    // must start with "/", if not the first one. eg."/whut"
                    str = String.format(Locale.CHINA, "/%s", str);
                }
                if (str.endsWith("/")) {
                    str = str.substring(0, str.length() - 1);
                }
                combinedStr = String.format(Locale.CHINA, "%s%s", combinedStr, str);
            }
        } else {
            combinedStr = null;
        }
        return combinedStr;
    }

    /**
     *  format the size to a string with unit.
     * @param size file size use Byte as the unit.
     * @return a string with an unit, eg.12.59M
     */
    public static String getFormattedSize(long size) {
        float localSize = (float)(size / Math.pow(2, 30));
        Formatter formatter = new Formatter();
        if (localSize > 1) {
            return formatter.format("%.2fG", Math.round(localSize * 100) / 100f).toString();
        } else if ((localSize = (float)(size / Math.pow(2, 20))) > 1) {
            return formatter.format("%.2fM", Math.round(localSize * 100) / 100f).toString();
        } else if ((localSize = (float)(size / Math.pow(2, 10))) > 1) {
            return formatter.format("%.2fK", Math.round(localSize * 100) / 100f).toString();
        } else {
            return formatter.format("%dB", size).toString();
        }
    }

    public static String getFileNameByTime(long timeStamp, boolean toMS) {
        if (timeStamp == -1) {
            timeStamp = Calendar.getInstance().getTimeInMillis();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String name = simpleDateFormat.format(new Date(timeStamp));
        if (toMS) {
            name = name.concat(String.format(Locale.CHINA, "-%d", timeStamp % 1000));
        }
        return name;
    }

    /**
     *
     * @param bitmap
     * @param dir  for example:DCIM/
     * @return
     */
    public static String saveJpeg2ExternalStorage(Bitmap bitmap, String dir, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = getFileNameByTime(-1, false) + ".jpg";
        }
        String path = combine(EXT_DIR_PATH, dir, fileName);
        if (!checkAndCreateFile(path)) {
            LogUtil.w(TAG, "file check failed:%s.", path);
            return null;
        }
        try (FileOutputStream fOut =  new FileOutputStream(path)){
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)) {
                fOut.flush();
            } else {
                LogUtil.i(TAG, "saveJpeg2ExternalStorage, compress failed.");
                path = null;
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "saveJpeg2ExternalStorage, save picture failed.", e);
            path = null;
        }
        return path;
    }

    public static int getFileTotalLines(String fileName) {
        FileReader fr = null;
        LineNumberReader lnr = null;
        int lines = 0;

        try {
            fr = new FileReader(fileName);
            lnr = new LineNumberReader(fr);
            if (lnr != null) {
                lnr.skip(new File(fileName).length());
                lines = lnr.getLineNumber();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Logger.f(TAG, e.getMessage());
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                    fr = null;
                }
                if (lnr != null) {
                    lnr.close();
                    lnr = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lines;
    }

    public static String listFile(String dir, final String prefix, final String suffix) {
        String target = null;

        File file = new File(dir);

        if(file.exists()) {
            File[] files = file.listFiles(
                    new FilenameFilter(){
                        public boolean accept(File dir, String name) {
                            return ((name.startsWith(prefix))&&(name.endsWith(suffix)));
                        }
                    }
            );

            if(files.length > 0) {
                target = files[0].getPath();
            }else{
                //无此文件
            }
        }else{
            //此目录不存在
        }
        return target;
    }

    public static boolean createFile(String fileName) {
        boolean result = false;

        File file = new File(fileName);
        try {
            if(!file.exists()){
                result = file.createNewFile();
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean createDir(String dir) {
        boolean result = false;

        File file = new File(dir);
        if (!file.exists()) {
            result = file.mkdirs();
        } else {
            result = true;
        }

        return result;
    }

    public static Boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     *
     * @param filePath path if the file you wanna delete.
     * @param recursive if true, this method would delete directory.
     * @return
     */
    public static Boolean deleteFile(String filePath, boolean recursive) {
        File file = new File(filePath);
        if (file.isDirectory() && file.list().length > 0) {
            deleteFile(file, recursive);
        }
        return deleteFile(file, false);
    }

    /**
     * delete a file or directory
     * @param file
     * @param recursive if true, all the files in the directory would be deleted.
     * @return false if there is even one failure.
     */
    public static boolean deleteFile(File file, boolean recursive) {
        if (file.isFile() && file.exists() || file.isDirectory() && file.list().length == 0) {
            return file.delete();
        } else if (file.isDirectory() && recursive) {
            boolean result = true;
            String[] subDirNames = file.list();
            for (String name: subDirNames) {
                boolean localrt = deleteFile(file.getAbsolutePath(), name, true);
                if (!localrt) {
                    result = false;
                }
            }
            return result;
        }
        return false;
    }

    /**
     * delete a file denoted by parent and fileName
     * @param parent the file dir path, not the file path
     * @param fileName the file name, not the file path
     * @param recursive
     * @return
     */
    public static Boolean deleteFile(String parent, String fileName, boolean recursive) {
        File file = new File(parent, fileName);
        if (file.isDirectory() && file.list().length > 0) {
            deleteFile(file, recursive);
        }
        return deleteFile(file, recursive);
    }

    public static long getFileLength(String fileName){
        File file = new File(fileName);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    public static boolean writeFromInputStream(String path, String fileName, final InputStream input) {
        boolean result = false;

        File file = null;
        OutputStream output = null;

        if (createDir(path) && createFile(path + fileName)) {
            file = new File(path + fileName);

            try {
                output = new FileOutputStream(file);

                byte[] buffer = new byte[4*1024];
                int temp;
                while ((temp = input.read(buffer)) != -1) {
                    output.write(buffer, 0, temp);
                }

                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                //Logger.e(TAG, e.toString());
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    output = null;
                }
            }
        }

        return result;
    }

    public static File writeFile(String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;

        if (createDir(path) && createFile(path + fileName)) {
            file = new File(path + fileName);

            try {
                output = new FileOutputStream(file);
                byte[] buffer = new byte[4*1024];
                int temp;
                while ((temp = input.read(buffer)) != -1) {
                    output.write(buffer, 0, temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    output = null;
                }
            }
        }
        return file;
    }

    private static boolean checkAndCreateDir(File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static boolean checkAndCreateFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return true;
        }
        if (!checkAndCreateDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public static void writeFileViaWriter(String filePath, String logString, boolean append){
        if (logString == null) {
            LogUtil.w(TAG, "logString is null");
            return;
        }
        if (!checkAndCreateFile(filePath)) {
            LogUtil.w(TAG, "writeFileViaWriter, cannot create file:%s.", filePath);
            return;
        }

        try (FileWriter writer = new FileWriter(filePath, append)){
            writer.write(logString);
        } catch (IOException e) {
            LogUtil.e(TAG, "try to write log file, failed.", e);
        }
    }

}
