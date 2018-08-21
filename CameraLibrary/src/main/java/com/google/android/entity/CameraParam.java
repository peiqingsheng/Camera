package com.google.android.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/15.
 */
public class CameraParam implements Serializable {

    private int maxMember; //最多拍几张返回
    private float picHight;//接近于这个图片高度的尺寸
    private int picQuality;//图片质量
    private String picPath;//图片路径
    private String picPre;//前缀名
    private String watermark;// 水印内容 默认时间

    public enum Quantity {Low, Medium, High, VeryHigh}

    public CameraParam() {
        this(Quantity.Medium);//默认中画质
    }

    public CameraParam(Quantity quantity) {
        maxMember = 1;
        picPre = "";
        switch (quantity) {
            case Low:
                picHight = 800;
                picQuality = 40;
                break;
            case Medium:
                picHight = 1280;
                picQuality = 60;
                break;
            case High:
                picHight = 1920;
                picQuality = 80;
                break;
            case VeryHigh:
                picHight = 3840;
                picQuality = 100;
                break;
        }
    }

    public int getMaxMember() {
        return maxMember;
    }

    public void setMaxMember(int maxMember) {
        this.maxMember = maxMember;
    }

    public float getPicHight() {
        return picHight;
    }

    public void setPicHight(float picHight) {
        this.picHight = picHight;
    }

    public int getPicQuality() {
        return picQuality;
    }

    public void setPicQuality(int picQuality) {
        this.picQuality = picQuality;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getPicPre() {
        return picPre;
    }

    public void setPicPre(String picPre) {
        this.picPre = picPre;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }

}
