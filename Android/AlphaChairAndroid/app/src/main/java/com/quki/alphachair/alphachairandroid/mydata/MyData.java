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
    private String posture;

    public void setName(String name) {
        this.name = name;
    }

    public void setPosture(String posture) {
        this.posture = posture;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public String getName() {
        return name;
    }

    public String getPosture() {
        return posture;
    }


    public Date getNow() {
        return now;
    }

}
