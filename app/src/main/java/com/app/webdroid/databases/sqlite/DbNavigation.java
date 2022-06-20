package com.app.webdroid.databases.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.app.webdroid.models.Navigation;

import java.util.ArrayList;
import java.util.List;

public class DbNavigation extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "menu.db";
    public static final String TABLE_MENU = "menu";
    public static final String ID = "id";
    public static final String MENU_NAME = "name";
    public static final String MENU_TYPE = "type";
    public static final String MENU_ICON = "icon";
    public static final String MENU_URL = "url";
    private final SQLiteDatabase db;

    public DbNavigation(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableMenu(db, TABLE_MENU);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        createTableMenu(db, TABLE_MENU);
    }

    public void truncateTableMenu(String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        createTableMenu(db, table);
    }

    private void createTableMenu(SQLiteDatabase db, String table) {
        String CREATE_TABLE = "CREATE TABLE " + table + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MENU_NAME + " TEXT,"
                + MENU_TYPE + " TEXT,"
                + MENU_ICON + " TEXT,"
                + MENU_URL + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    public void addListCategory(List<Navigation> navigations, String table) {
        for (Navigation navigation : navigations) {
            addOneMenu(db, navigation, table);
        }
        getAllMenu(table);
    }

    public void addOneMenu(SQLiteDatabase db, Navigation navigation, String table) {
        ContentValues values = new ContentValues();
        values.put(MENU_NAME, navigation.name);
        values.put(MENU_TYPE, navigation.type);
        values.put(MENU_ICON, navigation.icon);
        values.put(MENU_URL, navigation.url);
        db.insert(table, null, values);
    }

    public List<Navigation> getAllMenu(String table) {
        return getAllMenus(table);
    }

    private List<Navigation> getAllMenus(String table) {
        List<Navigation> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY id ASC", null);
        list = getAllCategoryFormCursor(cursor);
        return list;
    }

    @SuppressLint("Range")
    private List<Navigation> getAllCategoryFormCursor(Cursor cursor) {
        List<Navigation> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Navigation navigation = new Navigation();
                navigation.name = cursor.getString(cursor.getColumnIndex(MENU_NAME));
                navigation.type = cursor.getString(cursor.getColumnIndex(MENU_TYPE));
                navigation.icon = cursor.getString(cursor.getColumnIndex(MENU_ICON));
                navigation.url = cursor.getString(cursor.getColumnIndex(MENU_URL));
                list.add(navigation);
            } while (cursor.moveToNext());
        }
        return list;
    }

}
