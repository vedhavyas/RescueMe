package org.rescueme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Authored by Vedhavyas Singareddi on 03-09-2014.
 */
public class RescueMeDBFactory extends SQLiteOpenHelper {

    private static RescueMeDBFactory dbFactory;
    private String table_name;

    private RescueMeDBFactory(Context context) {
        super(context, RescueMeConstants.DB_NAME, null, 1);
    }

    public static RescueMeDBFactory getInstance(Context context) {
        if (dbFactory == null) {
            dbFactory = new RescueMeDBFactory(context);
        }

        return dbFactory;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RescueMeConstants.SQL_USER_TABLE_CREATE_QUERY);
        db.execSQL(RescueMeConstants.SQL_CONTACT_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RescueMeConstants.DROP_TABLE + RescueMeConstants.CONTACTS_TABLE);
        db.execSQL(RescueMeConstants.DROP_TABLE + RescueMeConstants.USER_TABLE);
        onCreate(db);
    }

    public long registerUser(RescueMeUserModel user) {
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(RescueMeConstants.COLUMN_NAME, user.getName());
        contentValues.put(RescueMeConstants.COLUMN_EMAIL, user.getEmail());
        contentValues.put(RescueMeConstants.COLUMN_PASSWORD, user.getHashPassword());
        contentValues.put(RescueMeConstants.COLUMN_NUMBER, user.getNumber());

        result = db.insert(table_name, null, contentValues);
        return result;
    }

    public boolean loginUser(RescueMeUserModel user) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlQuery = RescueMeConstants.SQL_LOGIN_QUERY + "\"" + user.getEmail() + "\"";
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            if (user.getHashPassword().equalsIgnoreCase(cursor.getString(0))) {
                return true;
            }
        }
        return false;
    }

    public long addEmergencyContact(RescueMeUserModel contact) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(RescueMeConstants.COLUMN_NAME, contact.getName());
        contentValues.put(RescueMeConstants.COLUMN_EMAIL, contact.getEmail());
        contentValues.put(RescueMeConstants.COLUMN_NUMBER, contact.getNumber());

        return db.insert(table_name, null, contentValues);
    }

    public long updateEmergencyContact(RescueMeUserModel contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = -1;

        RescueMeUserModel oldContactData = getContact(contact.getId());

        ContentValues contentValues = new ContentValues();
        if (!oldContactData.getName().equals(contact.getName())) {
            contentValues.put(RescueMeConstants.COLUMN_NAME, contact.getName());
        }

        if (!oldContactData.getEmail().equals(contact.getEmail())) {
            contentValues.put(RescueMeConstants.COLUMN_EMAIL, contact.getEmail());
        }

        if (!oldContactData.getNumber().equals(contact.getNumber())) {
            contentValues.put(RescueMeConstants.COLUMN_NUMBER, contact.getNumber());
        }

        if (contentValues.size() > 0) {
            try {
                result = db.update(table_name, contentValues, RescueMeConstants.COLUMN_ID + " = " + contact.getId(), null);
            } catch (SQLiteConstraintException e) {
                return result;
            }

        }

        return result;
    }

    public RescueMeUserModel getContact(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + table_name + " WHERE " + RescueMeConstants.COLUMN_ID
                + " = " + id;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            RescueMeUserModel contact = new RescueMeUserModel();
            do {
                contact.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(RescueMeConstants.COLUMN_ID))));
                contact.setName(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NAME)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_EMAIL)));
                contact.setNumber(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NUMBER)));
            } while (cursor.moveToNext());

            return contact;
        }

        return null;
    }

    public List<RescueMeUserModel> getAllContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<RescueMeUserModel> contacts = new ArrayList<RescueMeUserModel>();

        String sqlQuery = RescueMeConstants.SQL_SELECT_ALL_QUERY + table_name;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RescueMeUserModel contact = new RescueMeUserModel();
                contact.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(RescueMeConstants.COLUMN_ID))));
                contact.setName(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NAME)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_EMAIL)));
                contact.setNumber(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NUMBER)));
                contacts.add(contact);
            } while (cursor.moveToNext());

            return contacts;
        }

        return null;
    }

    public int deleteContact(String id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(table_name, RescueMeConstants.COLUMN_ID + " = " + id, null);
    }
}

