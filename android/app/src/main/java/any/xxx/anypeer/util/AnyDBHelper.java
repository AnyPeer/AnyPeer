package any.xxx.anypeer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AnyDBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "any_chat.db";
	private static final int DB_VERSION = 1;

	static final String TABLE_FRIENDS = "table_friends";
	static final String USERID = "userid";
	static final String NICKNAME = "nickname";
	static final String EXTRA = "extra";
	static final String TIME = "time";

	private static final String CREATE_AUTOTRANSFER = "create table " +
			TABLE_FRIENDS + "(_id integer primary key autoincrement, " +
			USERID + " varchar, " +
			NICKNAME + " varchar, " +
			EXTRA + " varchar, " +
			TIME + " varchar"
			+ ")";

	AnyDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_AUTOTRANSFER);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
