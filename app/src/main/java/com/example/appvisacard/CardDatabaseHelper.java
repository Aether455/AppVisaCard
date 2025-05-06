package com.example.appvisacard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.appvisacard.model.CardModel;

import java.util.ArrayList;
import java.util.List;

public class CardDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "users.db"; // Phải trùng DB_NAME với UserDatabaseHelper
    private static final int DB_VERSION = 2;

    private UserDatabaseHelper userDbHelper;

    public CardDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        userDbHelper = new UserDatabaseHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Không cần làm gì vì đã tạo bảng ở UserDatabaseHelper
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public boolean insertCard(String cardNumber, String expireDate, String cardHolder, int userId) {
        if (userId == -1) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("card_number", cardNumber);
        values.put("expire_date", expireDate);
        values.put("card_holder", cardHolder);
        values.put("user_id", userId);

        long result = db.insertWithOnConflict("cards", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public List<CardModel> getCardsByUserId(int userId) {
        List<CardModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cards WHERE user_id=?", new String[]{String.valueOf(userId)});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String number = cursor.getString(1);
                    String date = cursor.getString(2);
                    String holder = cursor.getString(3);
                    list.add(new CardModel(id, number, date, holder));
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    public void deleteCard(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("cards", "id=?", new String[]{String.valueOf(id)});
    }
    public boolean isCardExists(String cardNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM cards WHERE card_number = ?", new String[]{cardNumber});
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }
}
