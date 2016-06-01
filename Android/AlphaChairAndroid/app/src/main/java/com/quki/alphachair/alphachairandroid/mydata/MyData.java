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
    private float frontRight;
    private float frontLeft;
    private float backRight;
    private float backLeft;
    private Date now;

    public void setName(String name) {
        this.name = name;
    }

    public void setFSRData(float frontRight, float frontLeft, float backRight, float backLeft) {
        this.frontRight = frontRight;
        this.frontLeft = frontLeft;
        this.backRight = backRight;
        this.backLeft = backLeft;
    }

    public void setNow(Date now) {
        this.now = now;
    }

}
