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

	public Step selectCurrentStep() {
		Cursor cursor = db.query(DBConstants.TABLE_NAME_STEP, null, null, null, null, null, null);
		Step step = null;
		while (cursor.moveToLast()) {
			step = new Step();
			step.date = cursor.getString(cursor.getColumnIndex(DBConstants.STEP_FIELD_DATE));
			step.count = cursor.getString(cursor.getColumnIndex(DBConstants.STEP_FIELD_COUNT));
			step.duration = cursor.getString(cursor.getColumnIndex(DBConstants.STEP_FIELD_DURATION));
			step.distance = cursor.getString(cursor.getColumnIndex(DBConstants.STEP_FIELD_DISTANCE));
			step.calories = cursor.getString(cursor.getColumnIndex(DBConstants.STEP_FIELD_CALORIES));
			break;
		}
		return step;
	}

	public long insertStep(Step step) {
		ContentValues cv = new ContentValues();
		cv.put(DBConstants.STEP_FIELD_DATE, step.date);
		cv.put(DBConstants.STEP_FIELD_COUNT, step.count);
		cv.put(DBConstants.STEP_FIELD_DURATION, step.duration);
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
		Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstants.TABLE_NAME_STEP + " WHERE "
				+ DBConstants.STEP_FIELD_DATE + " = ?", args);
		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		} else {
			cursor.close();
			return true;
		}
	}

	public void updateStep(Step step) {
		String where = DBConstants.STEP_FIELD_DATE + " = ?";
		String[] whereValue = { step.date };
		ContentValues cv = new ContentValues();
		cv.put(DBConstants.STEP_FIELD_DATE, step.date);
		cv.put(DBConstants.STEP_FIELD_COUNT, step.count);
		cv.put(DBConstants.STEP_FIELD_DURATION, step.duration);
		cv.put(DBConstants.STEP_FIELD_DISTANCE, step.distance);
		cv.put(DBConstants.STEP_FIELD_CALORIES, step.calories);
		db.update(DBConstants.TABLE_NAME_STEP, cv, where, whereValue);
	}

	public void deleteAllData() {
		db.delete(DBConstants.TABLE_NAME_STEP, null, null);
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
