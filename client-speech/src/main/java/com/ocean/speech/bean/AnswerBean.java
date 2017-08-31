package com.ocean.speech.bean;

/**
 * Created by zhangyuanyuan on 2017/7/18.
 */

public class AnswerBean {
    private String answer;
    private String url;

    public AnswerBean() {
    }

    public AnswerBean(String answer, String url) {
        this.answer = answer;
        this.url = url;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AnswerBean{" +
                "answer='" + answer + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
