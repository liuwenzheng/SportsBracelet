package com.blestep.sportsbracelet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.blestep.sportsbracelet.entity.Step;

public class DBTools {
	private DBOpenHelper myDBOpenHelper;
	private SQLiteDatabase db;
	private static DBTools dbTools;

	public static DBTools getInstance(Context context) {
		if (dbTools == null) {
			dbTools = new DBTools(context);
			return dbTools;
		}
		return dbTools;
	}

	public DBTools(Context context) {
		myDBOpenHelper = new DBOpenHelper(context);
		db = myDBOpenHelper.getWritableDatabase();
	}

	public long insertStep(Step step) {
		ContentValues cv = new ContentValues();
		cv.put(DBConstants.STEP_FIELD_DATE, step.date);
		cv.put(DBConstants.STEP_FIELD_COUNT, step.count);
		cv.put(DBConstants.STEP_FIELD_DISTANCE, step.distance);
		cv.put(DBConstants.STEP_FIELD_CALORIES, step.calories);
		long row = db.insert(DBConstants.TABLE_NAME_STEP, null, cv);
		return row;
	}

	public void deleteStep(int id) {
		String where = DBConstants.STEP_FIELD_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(DBConstants.TABLE_NAME_STEP, where, whereValue);
	}

	public boolean isStepExist(String date) {
		String[] args = new String[] { date };
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_NAME_STEP + " WHERE " + DBConstants.STEP_FIELD_DATE + " = ?",
				args);
		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		} else {
			cursor.close();
			return true;
		}
	}

	// drop table;
	public void droptable(String tablename) {
		db.execSQL("DROP TABLE IF EXISTS " + tablename);
	}

	// close database;
	public void close(String databasename) {
		db.close();
	}

}
