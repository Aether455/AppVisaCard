package com.example.appvisacard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "users.db";
    private static final int DB_VERSION = 2;

    public UserDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "password TEXT)");

        db.execSQL("CREATE TABLE current_user (" +
                "id INTEGER PRIMARY KEY, " +
                "user_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE cards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "card_number TEXT UNIQUE, " +
                "expire_date TEXT, " +
                "card_holder TEXT, " +
                "user_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cards");
        db.execSQL("DROP TABLE IF EXISTS current_user");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public boolean registerUser(String email, String password) {
        if (isEmailExists(email)) {
            return false; // Email đã tồn tại
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email=? AND password=?", new String[]{email, password});
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();

            // Lưu user đang đăng nhập vào bảng current_user
            setCurrentUser(userId);
            return true;
        }
        if (cursor != null) cursor.close();
        return false;
    }

    private void setCurrentUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("current_user", null, null); // Xóa user cũ
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("user_id", userId);
        db.insert("current_user", null, values);
    }

    public int getCurrentUserId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM current_user WHERE id=1", null);
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(0);
            cursor.close();
        }
        return userId;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM users WHERE email=?", new String[]{email});
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }
    public void clearCurrentUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("current_user", null, null);
    }

}
