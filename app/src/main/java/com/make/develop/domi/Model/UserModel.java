package com.make.develop.domi.Model;

public class UserModel {
    private String uid, name, address, phone;

    public UserModel(){

    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserModel(String uid, String name, String address, String phone){
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.phone = phone;

    }
}
