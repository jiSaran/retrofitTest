package com.saran.test.retroapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by core I5 on 12/7/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABSE_VERSION = 1;

    private static final String DATABASE_NAME = "spinnerDB";

    private static final String TABLE_NAME = "spinnerTable";

    private static final String KEY_ID = "id";
    private static final String PROBLEM_NAME = "problem_name";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_SPINNER_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROBLEM_NAME + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_SPINNER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(sqLiteDatabase);
    }

    public static String getDBName()
    {
        return DATABASE_NAME;
    }


    public void addProblem(int id,String problemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID,id);
        values.put(PROBLEM_NAME,problemName);
        db.insert(TABLE_NAME,null,values);

        db.close();
    }

    public List<String> getProblem(){
        List<String> problems = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);

        if(cursor!=null) {
            cursor.moveToFirst();
            while(cursor.isAfterLast()==false){
                String problem = cursor.getString(cursor.getColumnIndex(PROBLEM_NAME));
                problems.add(problem);
                cursor.moveToNext();
            }
        }

        return problems;
    }
}
