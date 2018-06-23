package com.xair.h264demo.Tool.SQL;

public class RoomInfo {
    private int userId;
    private String DeviceType;
    private String ID;
    private String IP;
    private String MASK;
    private String GATE;
    private String DNS;
    private String Louhao;
    private String Danyuan;
    private String Fangjian ;
    private String Name;
    private String Password;
    private String Version;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDeviceType() {
        return DeviceType;
    }
    public void setDeviceType(String deviceType) {
        DeviceType = deviceType;
    }


    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }


    public String getIP() {
        return IP;
    }
    public void setIP(String IP) {
        this.IP = IP;
    }


    public String getMASK() {
        return MASK;
    }
    public void setMASK(String MASK) {
        this.MASK = MASK;
    }


    public String getGATE() {
        return GATE;
    }
    public void setGATE(String GATE) {
        this.GATE = GATE;
    }


    public String getDNS() {
        return DNS;
    }
    public void setDNS(String DNS) {
        this.DNS = DNS;
    }


    public String getLouhao() {
        return Louhao;
    }
    public void setLouhao(String louhao) {
        Louhao = louhao;
    }


    public String getDanyuan() {
        return Danyuan;
    }
    public void setDanyuan(String danyuan) {
        Danyuan = danyuan;
    }


    public String getFangjian() {
        return Fangjian;
    }
    public void setFangjian(String fangjian) {
        Fangjian = fangjian;
    }


    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }


    public String getVersion() {
        return Version;
    }
    public void setVersion(String version) {
        Version = version;
    }


    public String getPassword() { return Password; }
    public void setPassword(String password) { Password = password; }

    @Override
    public String toString() {
        return "RoomInfo{" +
                "userId=" + userId +
                "DeviceType=" + DeviceType + '\'' +
                ", ID='" + ID + '\'' +
                ", IP='" + IP + '\'' +
                ", MASK='" + MASK + '\'' +
                ", GATE='" + GATE + '\'' +
                ", GATE='" + DNS + '\'' +
                ", GATE='" + Louhao + '\'' +
                ", GATE='" + Danyuan + '\'' +
                ", GATE='" + Fangjian + '\'' +
                ", GATE='" + Name + '\'' +
                ", GATE='" + Password + '\'' +
                ", GATE='" + Version + '\'' +
                '}';
    }
}
