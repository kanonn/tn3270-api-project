package com.example.tn3270api.dto;

/**
 * 简单字符串请求（不指定位置）
 */
public class StringRequest {
    private String text;

    public StringRequest() {
    }

    public StringRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
