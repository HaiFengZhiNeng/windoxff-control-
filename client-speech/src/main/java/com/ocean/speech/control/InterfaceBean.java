package com.ocean.speech.control;

/**
 * Created by zhangyuanyuan on 2017/7/10.
 */
public class InterfaceBean {
    private String content;
    private int id;

    public InterfaceBean(String content, int id) {
        this.content = content;
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
