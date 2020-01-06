package any.xxx.anypeer.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import any.xxx.anypeer.bean.User;

public class Utils {
    public static final String CHAT_LIST = "chat_list";
    public static final String NAME = "name";
    public static final String USERID = "userid";
    public static final int START_FRIEND = 30000;
    public static final int BACK_CHAT = 30001;
    public static final String ROOTPATH = "anypeer";
    public static final String ADDRESS = "anypeer_Address";
    public static final String QRCODETYPE = "QrCodeType";
    public static final String USER = "user";
    public static final String ADD_FRIEND_CHECK = "add_friend_check";
    private static final String SPECIALVERSION = "1.0.3";

    public enum QrCodeType {
        Carrier_Address,
        Wallet_Address,
        Null_Address
    }

    public static void showLongToast(Context context, String pMsg) {
        Toast.makeText(context, pMsg, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Context context, String pMsg) {
        Toast.makeText(context, pMsg, Toast.LENGTH_SHORT).show();
    }

    public static String getRootPath(Context context) {
        String appPath = context.getFilesDir().getParent();
        //TODO
        return appPath;// + ROOTPATH;
    }

    public static void finish(Activity activity) {
        activity.finish();
    }

    private static final String SPACE = " ";

    //Address + username
    public static String getQrcodeString(Context context) {
        User user = new Gson().fromJson(PrefereneceUtil.getString(context, User.USER), User.class);
        String userName = user.getUserName();
        if (NetUtils.getInstance() != null) {
            return NetUtils.getInstance().getChatID() + SPACE + userName;
        }

        return userName;
    }

    /**
     * RemoveValue SharedPreference
     *
     * @param context
     * @param key
     */
    public static final void RemoveValue(Context context, String key) {
        Editor editor = getSharedPreference(context).edit();
        editor.remove(key);
        boolean result = editor.commit();
        if (!result) {
            Log.e("移除Shared", "save " + key + " failed");
        }
    }

    private static final SharedPreferences getSharedPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 获取SharedPreference 值
     *
     * @param context
     * @param key
     * @return
     */
    public static final String getValue(Context context, String key) {
        return getSharedPreference(context).getString(key, "");
    }

    public static final Boolean getBooleanValue(Context context, String key) {
        return getSharedPreference(context).getBoolean(key, false);
    }

    public static final void putBooleanValue(Context context, String key,
                                             boolean bl) {
        Editor edit = getSharedPreference(context).edit();
        edit.putBoolean(key, bl);
        edit.commit();
    }

    public static final int getIntValue(Context context, String key) {
        return getSharedPreference(context).getInt(key, 0);
    }

    public static final long getLongValue(Context context, String key,
                                          long default_data) {
        return getSharedPreference(context).getLong(key, default_data);
    }

    public static final boolean putLongValue(Context context, String key,
                                             Long value) {
        Editor editor = getSharedPreference(context).edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public static final Boolean hasValue(Context context, String key) {
        return getSharedPreference(context).contains(key);
    }

    /**
     * 设置SharedPreference 值
     *
     * @param context
     * @param key
     * @param value
     */
    public static final boolean putValue(Context context, String key,
                                         String value) {
        value = value == null ? "" : value;
        Editor editor = getSharedPreference(context).edit();
        editor.putString(key, value);
        boolean result = editor.commit();
        if (!result) {
            return false;
        }
        return true;
    }

    /**
     * 设置SharedPreference 值
     *
     * @param context
     * @param key
     * @param value
     */
    public static final boolean putIntValue(Context context, String key,
                                            int value) {
        Editor editor = getSharedPreference(context).edit();
        editor.putInt(key, value);
        boolean result = editor.commit();
        if (!result) {
            return false;
        }
        return true;
    }

    public static Date stringToDate(String str) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date date = null;
        try {
            // Fri Feb 24 00:00:00 CST 2012
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * isEmail
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    /**
     * isMobileNO
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(17[^4,\\D])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * isNumber
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        java.util.regex.Matcher match = pattern.matcher(str);
        if (match.matches() == false) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static float sDensity = 0;

    /**
     * DP转换为像素
     *
     * @param context
     * @param nDip
     * @return
     */
    public static int dipToPixel(Context context, int nDip) {
        if (sDensity == 0) {
            final WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            sDensity = dm.density;
        }
        return (int) (sDensity * nDip);
    }

    public static String getPhotoPathFromContentUri(Context context, Uri uri) {
        String photoPath = "";
        if (context == null || uri == null) {
            return photoPath;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if (isExternalStorageDocument(uri)) {
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        photoPath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                photoPath = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    String type = split[0];
                    Uri contentUris = null;
                    if ("image".equals(type)) {
                        contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[]{split[1]};
                    photoPath = getDataColumn(context, contentUris, selection, selectionArgs);
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            photoPath = uri.getPath();
        } else {
            photoPath = getDataColumn(context, uri, null, null);
        }
        return photoPath;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]);
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    public static boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

	public static boolean isCN(Context context) {
    	try {
		    Locale locale = context.getResources().getConfiguration().locale;
		    String language = locale.getLanguage();
		    if (language.endsWith("zh")) {
			    return true;
		    }
	    }
	    catch (Exception e) {
    		e.printStackTrace();
	    }
	    return false;
	}
}
