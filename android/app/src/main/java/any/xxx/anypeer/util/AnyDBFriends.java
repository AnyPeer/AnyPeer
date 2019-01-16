package any.xxx.anypeer.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.elastos.carrier.FriendInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnyDBFriends {
	private static final String TAG = "AnyDBFriends";
	private SQLiteDatabase mDatabase;
	private static AnyDBFriends sAnyDBFriends = null;

	private AnyDBFriends(Context context) {
		AnyDBHelper anyDBHelper = new AnyDBHelper(context);
		mDatabase = anyDBHelper.getWritableDatabase();
	}

	public static AnyDBFriends getInstance(Context context) {
		if (sAnyDBFriends == null) {
			sAnyDBFriends = new AnyDBFriends(context);
		}

		return sAnyDBFriends;
	}

	public void insertUser(FriendInfo info) {
		String userId = info.getUserId();
		String nickName = info.getName();

		//TODO
		String extra = info.getDescription();
		if (userId == null || userId.isEmpty()) {
			return;
		}

		if (nickName == null || nickName.isEmpty()) {
			nickName = "";
		}

		if (extra == null || extra.isEmpty()) {
			extra = "";
		}

		ContentValues values = new ContentValues();
		values.put(AnyDBHelper.USERID, userId);
		values.put(AnyDBHelper.NICKNAME, nickName);
		values.put(AnyDBHelper.EXTRA, extra);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		Date curDate = new Date(System.currentTimeMillis());
		String time = formatter.format(curDate);
		values.put(AnyDBHelper.TIME, time);
		mDatabase.insert(AnyDBHelper.TABLE_FRIENDS, null, values);
	}

	public void updateUser(String userId, String nickName) {
		updateUser(userId, nickName, null);
	}

	public void deleteUser(String userId) {
		try {
			mDatabase.delete(AnyDBHelper.TABLE_FRIENDS, AnyDBHelper.USERID + " = ?", new String[]{userId});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFriendName(String userId) {
		_FriendInfo info = queryUser(userId);
		if (info != null) {
			return info.mName;
		}

		return null;
	}

	private class _FriendInfo {
		private String mName;
		private String mExtra;
		private String mTime;
	}

	private _FriendInfo queryUser(String userId) {
		_FriendInfo info = null;
		Cursor cursor = null;
		try {
			cursor = mDatabase.query(AnyDBHelper.TABLE_FRIENDS,
					new String[]{AnyDBHelper.NICKNAME, AnyDBHelper.EXTRA, AnyDBHelper.TIME},
					AnyDBHelper.USERID + " = ?",
					new String[]{userId},
					null,
					null,
					null);

			if (cursor != null && cursor.moveToNext()) {
				int nameIndex = cursor.getColumnIndex(AnyDBHelper.NICKNAME);
				int extraIndex = cursor.getColumnIndex(AnyDBHelper.EXTRA);
				int timeIndex = cursor.getColumnIndex(AnyDBHelper.TIME);

				String name = cursor.getString(nameIndex);
				String extra = cursor.getString(extraIndex);
				String time = cursor.getString(timeIndex);
				info = new _FriendInfo();
				info.mName = name;
				info.mExtra = extra;
				info.mTime = time;
				Log.d(TAG, String.format("[queryUser] userId=[%s], name=[%s], extra=[%s], time=[%s]"
						, userId, name, extra, time));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (cursor != null) {
			cursor.close();
		}
		return info;
	}

	private void deleteTable() {
		mDatabase.execSQL("drop table if exists " + AnyDBHelper.TABLE_FRIENDS);
	}

	private void clearTable() {
		mDatabase.execSQL("DELETE FROM " + AnyDBHelper.TABLE_FRIENDS);
	}

	private void updateUser(String userId, String nickName, String extra) {
		try {
			if (nickName == null || nickName.isEmpty()) {
				return;
			}

			ContentValues values = new ContentValues();
			values.put(AnyDBHelper.NICKNAME, nickName);
			if (extra != null) {
				values.put(AnyDBHelper.EXTRA, extra);
			}

			mDatabase.update(AnyDBHelper.TABLE_FRIENDS, values, AnyDBHelper.USERID + " = ?", new String[]{userId});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
