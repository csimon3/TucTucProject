package com.solucionjava.tuctuc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by csimon on 12/11/13.
 */
public class DBTools extends SQLiteOpenHelper {

    private final static int    DB_VERSION = 3;

    public DBTools(Context context) {
        super(context, "tuctuc.db", null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query  = "create table tuc (tucId Integer primary key autoincrement, "+
                " noTuc text, owner text, saldo text, lastUpdate text)";
        sqLiteDatabase.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        String query  = "create table tucHist (fecha text, tucId Integer, "+
                " saldo text)";
        query  = "create table tucHist (fecha text, tucId Integer, "+
                " saldo text)";
        sqLiteDatabase.execSQL(query);
    }

    public void recreateDb() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "drop table tuc;";
        sqLiteDatabase.execSQL(query);
        query = "drop table tucHist;";
        sqLiteDatabase.execSQL(query);
        query = "create table tuc (tucId Integer primary key autoincrement, "+
                " noTuc text, owner text, saldo text, lastUpdate text)";
        sqLiteDatabase.execSQL(query);
        query  = "create table tucHist (fecha text, tucId Integer, "+
                " saldo text)";
        sqLiteDatabase.execSQL(query);

    }
    public void insertTuc (HashMap<String,String> queryValues){

        //Log.d(this.getClass().getName(), "Inserting tuc "+ queryValues.get("noTuc"));
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("noTuc", queryValues.get("noTuc"));
        values.put("owner", queryValues.get("owner"));
        values.put("saldo", queryValues.get("saldo"));
        values.put("lastUpdate", queryValues.get("lastUpdate"));
        database.insert("tuc", null, values);
        database.close();
    }

    public int updateTuc (HashMap<String,String> queryValues){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("noTuc", queryValues.get("noTuc"));
        values.put("owner", queryValues.get("owner"));
        values.put("saldo", queryValues.get("saldo"));
        values.put("lastUpdate", queryValues.get("lastUpdate"));
        return database.update("tuc", values, "tucId = ?", new String[] {queryValues.get("tucId")});
    }

    public void updateSaldoTuc (HashMap<String,String> queryValues){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("saldo", queryValues.get("saldo"));
        values.put("lastUpdate", queryValues.get("lastUpdate"));
        //System.out.println("updateSaldoTuc saldo = "+queryValues.get("saldo")+" - date = "+queryValues.get("lastUpdate")+" ** tuc="+queryValues.get("noTuc"));
        database.update("tuc", values, "noTuc = ?", new String[] {queryValues.get("noTuc")});

        values = new ContentValues();
        values.put("tucId", getTucId(queryValues.get("noTuc")));
        values.put("saldo", queryValues.get("saldo"));
        values.put("fecha", queryValues.get("lastUpdate"));
        database.insert("tucHist", null, values);
        database.close();
    }

    public void deleteTuc (int id){
        //System.out.println("Deleting TUC "+id);
        recreateDb();
    }
    public void deleteAllTuc (){
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "delete from tuc";
        database.execSQL(query);
    }

    public ArrayList<HashMap<String, String>> getAllTucs(){
        ArrayList<HashMap<String, String>> tucArrayList = new ArrayList<HashMap<String, String>>();
        String query = " Select * from tuc order by owner";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                HashMap<String, String> tucMap = new HashMap<String, String>();
                tucMap.put("tucId", cursor.getString(0));
                tucMap.put("noTuc", cursor.getString(1));
                tucMap.put("owner", cursor.getString(2));
                tucMap.put("saldo", cursor.getString(3));
                tucMap.put("lastUpdate", cursor.getString(4));
                tucArrayList.add(tucMap);
            } while (cursor.moveToNext());
        }
        return tucArrayList;
    }

    public ArrayList<HashMap<String, String>> getTucHist(String noTuc){
        ArrayList<HashMap<String, String>> tucArrayList = new ArrayList<HashMap<String, String>>();
        String query = "Select a.tucId,a.noTuc, a.owner, b.saldo,b.fecha from tuc a join tucHist b on (a.tucId=b.tucId) where a.noTuc='"+noTuc+"' order by b.fecha";
        System.out.println("++++++++ Getting TUC history => "+noTuc+" -- SQL="+query);
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                System.out.println("++++++++ Adding "+cursor.getString(1)+" - "+cursor.getString(4)+" => "+cursor.getString(3));
                HashMap<String, String> tucMap = new HashMap<String, String>();
                tucMap.put("tucId", cursor.getString(0));
                tucMap.put("noTuc", cursor.getString(1));
                tucMap.put("owner", cursor.getString(2));
                tucMap.put("saldo", cursor.getString(3));
                tucMap.put("fecha", cursor.getString(4));
                tucArrayList.add(tucMap);
            } while (cursor.moveToNext());
        }/*
        String query = "Select * from tucHist b order by b.fecha";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                System.out.println("++++++++ Adding "+cursor.getString(0)+" - "+cursor.getString(1)+" => "+cursor.getString(2));
            } while (cursor.moveToNext());
        }*/
        return tucArrayList;
    }

    public HashMap<String, String> getTucInfo(int tucId){
        HashMap<String, String> tucMap = new HashMap<String, String>();
        String query = " Select * from tuc where tucId="+tucId;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do {
                tucMap.put("tucId", cursor.getString(0));
                tucMap.put("noTuc", cursor.getString(1));
                tucMap.put("owner", cursor.getString(2));
                tucMap.put("saldo", cursor.getString(3));
                tucMap.put("lastUpdate", cursor.getString(4));
            } while (cursor.moveToNext());
        }
        return tucMap;
    }

    public int getTucId(String tucNo){
        int tucId=0;
        String query = " Select * from tuc where noTuc=?";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, new String[]{tucNo});

        if (cursor.moveToFirst()){
            do {
                tucId=  Integer.parseInt(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return tucId;
    }

}
