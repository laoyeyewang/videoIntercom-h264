package com.xair.h264demo.Tool.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RoomDbHelper extends SQLiteOpenHelper{

    private final String TABLE_NAME     ="info";
    private final String INFO_COLUM_ID  ="_id";

    private final String INFO_COLUM_DeviceType  ="deviceType";
    private final String INFO_COLUM_id  ="id";
    private final String INFO_COLUM_IP  ="ip";
    private final String INFO_COLUM_MASK ="mask";
    private final String INFO_COLUM_GATE ="gate";
    private final String INFO_COLUM_DNS ="dns";
    private final String INFO_COLUM_Louhao ="louhao";
    private final String INFO_COLUM_Danyuan ="danyuan";
    private final String INFO_COLUM_Fangjian ="fangjian";
    private final String INFO_COLUM_NAME="name";
    private final String INFO_COLUM_Password ="pwd";
    private final String INFO_COLUM_Version ="version";

    public RoomDbHelper(Context context){
        super(context,"RoomInfo.db",null,1);
    }
    public RoomDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder sql=new StringBuilder();
        sql.append("Create table if not exists ");
        sql.append(TABLE_NAME+"(");
        sql.append(INFO_COLUM_ID+" integer primary key autoincrement,");
        //autoincrement:id自动增长
        sql.append(INFO_COLUM_DeviceType+" varchar(10),");
        sql.append(INFO_COLUM_id+" varchar(10),");
        sql.append(INFO_COLUM_IP+" varchar(10),");
        sql.append(INFO_COLUM_MASK+" varchar(10),");
        sql.append(INFO_COLUM_GATE+" varchar(10),");
        sql.append(INFO_COLUM_DNS+" varchar(10),");
        sql.append(INFO_COLUM_Louhao+" varchar(10),");
        sql.append(INFO_COLUM_Danyuan+" varchar(10),");
        sql.append(INFO_COLUM_Fangjian+" varchar(10),");
        sql.append(INFO_COLUM_NAME+" varchar(10),");
        sql.append(INFO_COLUM_Password+" varchar(10),");
        sql.append(INFO_COLUM_Version+" varchar(10)");
        sql.append(")");
        sqLiteDatabase.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql="drop table if exists "+TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}

