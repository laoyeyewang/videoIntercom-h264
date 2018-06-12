package com.xair.h264demo.entity;

public class UDPSend {
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
    private String Version;

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


    @Override
    public String toString() {
        return "UDPSend [DeviceType=" + DeviceType + ",ID=" + ID + ",IP" + IP + ",MASK=" + MASK + ",GATE="+ GATE + ",DNS=" + DNS + ",Louhao=" + Louhao + ",Danyuan=" + Danyuan + ",Fangjian=" + Fangjian + ",Name=" + Name + ",Version=" + Version + "]";
    }
}
