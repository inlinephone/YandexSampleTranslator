package com.example.alexander.yasampltranslator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class TranslateHistoryDB extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "translationDB";
    public static final String TABLE_HISTORY = "tableTranslate";

    public static final String KEY_ID                  = "_id";
    public static final String KEY_ENTERED_TEXT        = "enteredText";
    public static final String KEY_TRANSLATED_TEXT     = "translatedText";
    public static final String KEY_TRANSLATE_DIRECTION = "translateDirection";
    public static final String KEY_FAVOURITE_TEXT      = "favourite";

    public TranslateHistoryDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_HISTORY + "(" + KEY_ID
                + " integer primary key," + KEY_ENTERED_TEXT + " text," + KEY_TRANSLATED_TEXT
                + " text," + KEY_TRANSLATE_DIRECTION + " text," + KEY_FAVOURITE_TEXT + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("drop table if exists " + TABLE_HISTORY);

        onCreate(db);
    }

    public void removeDB()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("drop table if exists " + TABLE_HISTORY);
        db.execSQL("create table " + TABLE_HISTORY + "(" + KEY_ID
                + " integer primary key," + KEY_ENTERED_TEXT + " text," + KEY_TRANSLATED_TEXT
                + " text," + KEY_TRANSLATE_DIRECTION + " text," + KEY_FAVOURITE_TEXT + " text)");

        db.close();
    }
}
