package com.waitig.entity;

public class StaffEntity {
    String id;
    String staffId;
    String staffName;
    String staffMobile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "StaffEntity{" +
                "id='" + id + '\'' +
                ", staffId='" + staffId + '\'' +
                ", staffName='" + staffName + '\'' +
                ", staffMobile='" + staffMobile + '\'' +
                '}';
    }
}
