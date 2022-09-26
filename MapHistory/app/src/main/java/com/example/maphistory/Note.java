package com.example.maphistory;
// just test for merge

public class Note {

    public int _id;
    public String titleOfDiary;
    public String createDateStr;
    public String address;
    public String locationX;
    public String locationY;
    public String picture;
    public String contents;


    public Note(int _id, String titleOfDiary, String createDateStr, String address, String locationX, String locationY, String picture , String contents
    ) {
        this._id =_id;
        this.titleOfDiary = titleOfDiary;
        this.createDateStr =createDateStr;
        this.address = address;
        this.locationX =locationX;
        this.locationY = locationY;
        this.picture = picture;
        this.contents = contents;

    }

    public Note() {
        int _id;
        String titleOfDiary;
        String createDateStr;
        String address;
        String locationX;
        String locationY;
        String picture;
        String contents;
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
