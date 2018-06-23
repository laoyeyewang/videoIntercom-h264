package com.xair.h264demo.Tool.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class RoomDoa {
    private RoomDbHelper roomDbHelper;
    public RoomDoa(Context context){
        roomDbHelper=new RoomDbHelper(context);
    }


    //插入数据
    public void insert(RoomInfo roomInfo){
        SQLiteDatabase db= roomDbHelper.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("deviceType",roomInfo.getDeviceType());
        cv.put("id",roomInfo.getID());
        cv.put("ip",roomInfo.getIP());
        cv.put("mask",roomInfo.getMASK());
        cv.put("gate",roomInfo.getGATE());
        cv.put("dns",roomInfo.getDNS());
        cv.put("louhao",roomInfo.getLouhao());
        cv.put("danyuan",roomInfo.getDanyuan());
        cv.put("fangjian",roomInfo.getFangjian());
        cv.put("name",roomInfo.getName());
        cv.put("pwd",roomInfo.getPassword());
        cv.put("version",roomInfo.getVersion());
        db.insert("Info", null, cv);
        db.close();
    }
    //查询一条数据
    public RoomInfo searchUser(String id){
        SQLiteDatabase db=roomDbHelper.getReadableDatabase();
        Cursor cs=   db.query("info", null, "_id = ? ", new String[]{id}, null, null, null);
        RoomInfo roomInfo=null;
        if (cs.moveToNext()){
            roomInfo = new RoomInfo();

            roomInfo.setUserId(cs.getInt(cs.getColumnIndex("_id")));

            roomInfo.setDeviceType(cs.getString(cs.getColumnIndex("deviceType")));
            roomInfo.setID(cs.getString(cs.getColumnIndex("id")));
            roomInfo.setIP(cs.getString(cs.getColumnIndex("ip")));
            roomInfo.setMASK(cs.getString(cs.getColumnIndex("mask")));
            roomInfo.setGATE(cs.getString(cs.getColumnIndex("gate")));
            roomInfo.setDNS(cs.getString(cs.getColumnIndex("dns")));
            roomInfo.setLouhao(cs.getString(cs.getColumnIndex("louhao")));
            roomInfo.setDanyuan(cs.getString(cs.getColumnIndex("danyuan")));
            roomInfo.setFangjian(cs.getString(cs.getColumnIndex("fangjian")));
            roomInfo.setName(cs.getString(cs.getColumnIndex("name")));
            roomInfo.setPassword(cs.getString(cs.getColumnIndex("pwd")));
            roomInfo.setVersion(cs.getString(cs.getColumnIndex("version")));
        }
        cs.close();
        db.close();
        return roomInfo;
    }

    //查询所有数据
    public List search(){
        SQLiteDatabase db= roomDbHelper.getReadableDatabase();
        Cursor cs= db.query("info", null, null, null, null, null, null);
        RoomInfo roomInfo=null;
        List<RoomInfo> list = new ArrayList<RoomInfo>();
        //返回结果集，相机里用过
        while (cs.moveToNext()){
            roomInfo=new RoomInfo();
            roomInfo.setUserId(cs.getInt(cs.getColumnIndex("_id")));

            roomInfo.setDeviceType(cs.getString(cs.getColumnIndex("deviceType")));
            roomInfo.setID(cs.getString(cs.getColumnIndex("id")));
            roomInfo.setIP(cs.getString(cs.getColumnIndex("ip")));
            roomInfo.setMASK(cs.getString(cs.getColumnIndex("mask")));
            roomInfo.setGATE(cs.getString(cs.getColumnIndex("gate")));
            roomInfo.setDNS(cs.getString(cs.getColumnIndex("dns")));
            roomInfo.setLouhao(cs.getString(cs.getColumnIndex("louhao")));
            roomInfo.setDanyuan(cs.getString(cs.getColumnIndex("danyuan")));
            roomInfo.setFangjian(cs.getString(cs.getColumnIndex("fangjian")));
            roomInfo.setName(cs.getString(cs.getColumnIndex("name")));
            roomInfo.setPassword(cs.getString(cs.getColumnIndex("pwd")));
            roomInfo.setVersion(cs.getString(cs.getColumnIndex("version")));
            list.add(roomInfo);
        }
        cs.close();
        db.close();
        return list;
    }
    //删除所有数据
    public void delete(){
        SQLiteDatabase db= roomDbHelper.getWritableDatabase();
        db.delete("info", null, null);
        db.close();
    }
    //删除一条数据
    public void deleteUser(String id){
        SQLiteDatabase db= roomDbHelper.getWritableDatabase();
        db.delete("info","_id=?",new String[]{id});
        db.close();
    }
    //修改数据
    public void update(RoomInfo roomInfo){
        SQLiteDatabase db= roomDbHelper.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("deviceType",roomInfo.getDeviceType());
        cv.put("id",roomInfo.getID());
        cv.put("ip",roomInfo.getIP());
        cv.put("mask",roomInfo.getMASK());
        cv.put("gate",roomInfo.getGATE());
        cv.put("dns",roomInfo.getDNS());
        cv.put("louhao",roomInfo.getLouhao());
        cv.put("danyuan",roomInfo.getDanyuan());
        cv.put("fangjian",roomInfo.getFangjian());
        cv.put("name",roomInfo.getName());
        cv.put("pwd",roomInfo.getPassword());
        cv.put("version",roomInfo.getVersion());
        String id=String.valueOf(roomInfo.getUserId());
        db.update("info",cv,"_id=?",new String[]{id});
        db.close();
    }
}
