package org.rescueme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by Vedavyas Singareddy on 03-09-2014.
 */
public class RescueMeDBFactory  extends SQLiteOpenHelper{

    private String table_name;

    public RescueMeDBFactory(Context context, String table_name){
        super(context,RescueMeConstants.DB_NAME,null,1);
        this.table_name = table_name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = null;
        if(table_name.equalsIgnoreCase(RescueMeConstants.USER_TABLE)) {
            sql = RescueMeConstants.SQL_USER_TABLE_CREATE_QUERY;
        }
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SQLiteStatement stmt = db.compileStatement(RescueMeConstants.DROP_TABLE);
        stmt.bindString(1, table_name);
        stmt.execute();
        onCreate(db);
    }

    public long RegisterUser(RescueMeUserModel user){
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(RescueMeConstants.COLUMN_NAME, user.getName());
        contentValues.put(RescueMeConstants.COLUMN_EMAIL,user.getEmail());
        contentValues.put(RescueMeConstants.COLUMN_PASSWORD,user.getHashPassword());
        contentValues.put(RescueMeConstants.COLUMN_NUMBER,user.getNumber());

        result = db.insert(table_name,null,contentValues);
        db.close();
        return result;
    }

    public boolean loginUser(RescueMeUserModel user){
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlQuery  = RescueMeConstants.SQL_LOGIN_QUERY + user.getEmail();
        Cursor cursor = db.rawQuery(sqlQuery,null);

        if(cursor.moveToFirst()){
            if(user.getHashPassword().equalsIgnoreCase(cursor.getString(0))){
                db.close();
                return true;
            }
        }
        db.close();
        return false;
    }
}

