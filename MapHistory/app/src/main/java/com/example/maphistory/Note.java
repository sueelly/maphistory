package com.example.maphistory;


public class Note {

    int _id;
    String address;
    String locationX;
    String locationY;
    String titleOfDiary;
    String contents;
    String picture;
    String createDateStr;

    public Note(int _id, String titleOfDiary, String createDateStr, String address, String locationX, String locationY, String picture , String contents
    ) {
        this._id =_id;
        this.address = address;
        this.locationX =locationX;
        this.locationY = locationY;
        this.titleOfDiary = titleOfDiary;
        this.contents = contents;
        this.picture = picture;
        this.createDateStr =createDateStr;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocationX() {
        return locationX;
    }

    public void setLocationX(String locationX) {
        this.locationX = locationX;
    }

    public String getLocationY() {
        return locationY;
    }

    public void setLocationY(String locationY) {
        this.locationY = locationY;
    }

    public String getTitleOfDiary() {
        return titleOfDiary;
    }

    public void setTitleOfDiary(String titleOfDiary) {
        this.titleOfDiary = titleOfDiary;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getCreateDateStr() {
        return createDateStr;
    }

    public void setCreateDateStr(String createDateStr) {
        this.createDateStr = createDateStr;
    }
}
