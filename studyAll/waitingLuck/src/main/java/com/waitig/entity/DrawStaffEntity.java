package com.waitig.entity;

public class DrawStaffEntity {
    String id;
    String level;
    String drawName;
    String staffId;
    String staffName;
    String staffMobile;
    String drawTime;

    public String getDrawName() {
        return drawName;
    }

    public void setDrawName(String drawName) {
        this.drawName = drawName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffMobile() {
        return staffMobile;
    }

    public void setStaffMobile(String staffMobile) {
        this.staffMobile = staffMobile;
    }

    public String getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(String drawTime) {
        this.drawTime = drawTime;
    }

    @Override
    public String toString() {
        return "DrawStaffEntity{" +
                "id='" + id + '\'' +
                ", level='" + level + '\'' +
                ", drawName='" + drawName + '\'' +
                ", staffId='" + staffId + '\'' +
                ", staffName='" + staffName + '\'' +
                ", staffMobile='" + staffMobile + '\'' +
                ", drawTime='" + drawTime + '\'' +
                '}';
    }
}
