package com.quki.alphachair.alphachairandroid.mydata;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by quki on 2016-06-01.
 */
public class MyData extends RealmObject {

    @Required // Name은 null이 될 수 없음
    private String name;
    private Date now;
    private float frontRight,frontLeft,backRight,backLeft;
    public void setName(String name) {
        this.name = name;
    }
    public void setFrontRight(float frontRight){
        this.frontRight = frontRight;
    }
    public void setFrontLeft(float frontLeft){
        this.frontLeft = frontLeft;
    }
    public void setBackRight(float backRight){
        this.backRight = backRight;
    }
    public void setBackLeft(float backLeft){
        this.backLeft = backLeft;
    }
    public void setNow(Date now) {
        this.now = now;
    }

    public String getName(){
        return name;
    }

    public float getFrontRight() {
        return frontRight;
    }

    public float getFrontLeft() {
        return frontLeft;
    }

    public float getBackRight() {
        return backRight;
    }

    public float getBackLeft() {
        return backLeft;
    }

    public Date getNow(){
        return now;
    }

}
