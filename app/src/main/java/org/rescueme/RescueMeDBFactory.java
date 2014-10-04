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
    private String tableName;

    private RescueMeDBFactory(Context context) {
        super(context, RescueMeConstants.DB_NAME, null, 1);
    }

    public static RescueMeDBFactory getInstance(Context context) {
        if (dbFactory == null) {
            dbFactory = new RescueMeDBFactory(context);
        }

        return dbFactory;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

        if (user.getProfilePic() != null) {
            contentValues.put(RescueMeConstants.COLUMN_PROFILE_PIC, user.getProfilePic());
        }

        contentValues.put(RescueMeConstants.COLUMN_NUMBER, user.getNumber());

        result = db.insert(tableName, null, contentValues);
        return result;
    }

    public long addEmergencyContact(RescueMeUserModel contact) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(RescueMeConstants.COLUMN_NAME, contact.getName());
        contentValues.put(RescueMeConstants.COLUMN_EMAIL, contact.getEmail());
        contentValues.put(RescueMeConstants.COLUMN_NUMBER, contact.getNumber());
        if (contact.getProfilePic() != null) {
            contentValues.put(RescueMeConstants.COLUMN_PROFILE_PIC, contact.getProfilePic());
        }

        return db.insert(tableName, null, contentValues);
    }

    public long updateEmergencyContact(RescueMeUserModel contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(RescueMeConstants.COLUMN_NAME, contact.getName());

        contentValues.put(RescueMeConstants.COLUMN_EMAIL, contact.getEmail());

        contentValues.put(RescueMeConstants.COLUMN_NUMBER, contact.getNumber());
        if (contact.getProfilePic() != null) {
            contentValues.put(RescueMeConstants.COLUMN_PROFILE_PIC, contact.getProfilePic());
        }

        try {
            result = db.update(tableName, contentValues, RescueMeConstants.COLUMN_ID + " = " + contact.getId(), null);
        } catch (SQLiteConstraintException e) {
            return result;
        }

        return result;
    }

    public RescueMeUserModel getContact(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + tableName + " WHERE " + RescueMeConstants.COLUMN_ID
                + " = " + id;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            RescueMeUserModel contact = new RescueMeUserModel();
            do {
                contact.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(RescueMeConstants.COLUMN_ID))));
                contact.setName(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NAME)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_EMAIL)));
                contact.setNumber(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NUMBER)));
                contact.setProfilePic(cursor.getBlob(cursor.getColumnIndex(RescueMeConstants.COLUMN_PROFILE_PIC)));
            } while (cursor.moveToNext());

            return contact;
        }

        return null;
    }

    public List<RescueMeUserModel> getAllContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<RescueMeUserModel> contacts = new ArrayList<RescueMeUserModel>();

        String sqlQuery = RescueMeConstants.SQL_SELECT_ALL_QUERY + tableName;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RescueMeUserModel contact = new RescueMeUserModel();
                contact.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(RescueMeConstants.COLUMN_ID))));
                contact.setName(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NAME)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_EMAIL)));
                contact.setNumber(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NUMBER)));
                contact.setProfilePic(cursor.getBlob(cursor.getColumnIndex(RescueMeConstants.COLUMN_PROFILE_PIC)));
                contacts.add(contact);
            } while (cursor.moveToNext());

            return contacts;
        }

        return null;
    }

    public int deleteContact(String id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(tableName, RescueMeConstants.COLUMN_ID + " = " + id, null);
    }

    public RescueMeUserModel getUserDetails(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlQuery = "SELECT * FROM " + tableName + " WHERE " + RescueMeConstants.COLUMN_ID
                + " = " + id;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            RescueMeUserModel user = new RescueMeUserModel();
            do {
                user.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(RescueMeConstants.COLUMN_ID))));
                user.setName(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_EMAIL)));
                user.setNumber(cursor.getString(cursor.getColumnIndex(RescueMeConstants.COLUMN_NUMBER)));
                user.setProfilePic(cursor.getBlob(cursor.getColumnIndex(RescueMeConstants.COLUMN_PROFILE_PIC)));
            } while (cursor.moveToNext());

            return user;
        }

        return null;
    }

    public int updateUserData(RescueMeUserModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(RescueMeConstants.COLUMN_NAME, user.getName());

        contentValues.put(RescueMeConstants.COLUMN_EMAIL, user.getEmail());

        contentValues.put(RescueMeConstants.COLUMN_NUMBER, user.getNumber());

        contentValues.put(RescueMeConstants.COLUMN_PROFILE_PIC, user.getProfilePic());

        try {
            result = db.update(tableName, contentValues, RescueMeConstants.COLUMN_ID + " = " + user.getId(), null);
        } catch (SQLiteConstraintException e) {
            return result;
        }


        return result;
    }

}

