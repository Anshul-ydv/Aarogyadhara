package com.example.Aarogyadhara.models;

public class UserObject {
    private String  uid,
            name,
            phone,
            notificationKey;
    private Boolean selected = false;
    public UserObject(String uid){
        this.uid = uid;
    }
    public UserObject(String uid, String name, String phone){
        this.uid = uid;
        this.name = name;
        this.phone = phone;
    }
    public UserObject() {
        // Default constructor required for calls to DataSnapshot.getValue(UserObject.class)
    }

    public String getUid() {
        return uid;
    }
    public String getPhone() {
        return phone;
    }
    public String getName() {
        return name;
    }
    public String getNotificationKey() {
        return notificationKey;
    }
    public Boolean getSelected() {
        return selected;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

