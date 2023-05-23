package com.xxf.i18n.plugin.bean;

/**
 * 字符串实体类
 * Created by xyw on 2023/5/21.
 */
public class StringEntity {
    private String id;
    private String value;

    public StringEntity() {
    }

    public StringEntity(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
