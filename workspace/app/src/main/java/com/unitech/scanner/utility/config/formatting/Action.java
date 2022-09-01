package com.unitech.scanner.utility.config.formatting;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/27 下午 12:00
 * 修改人:user
 * 修改時間:2021/1/27 下午 12:00
 * 修改備註:
 */

public class Action {
    private String actionDo = null;
    private int index = -1;
    private String content = null;
    private boolean enable=false;
    private int length = -1;
    private int symbolCase = -1;
    private String regexReplace = null;

    @JsonProperty("do")
    public String getActionDo() {
        return actionDo;
    }

    @JsonProperty("do")
    public void setActionDo(String value) {
        this.actionDo = value;
    }

    @JsonProperty("index")
    public int getIndex() {
        return index;
    }

    @JsonProperty("index")
    public void setIndex(int value) {
        this.index = value;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String value) {
        this.content = value;
    }

    @JsonProperty("enable")
    public boolean getEnable() {
        return enable;
    }

    @JsonProperty("enable")
    public void setEnable(boolean value) {
        this.enable = value;
    }

    @JsonProperty("length")
    public int getLength() {
        return length;
    }

    @JsonProperty("length")
    public void setLength(int value) {
        this.length = value;
    }

    @JsonProperty("symbolCase")
    public int getSymbolCase() {
        return symbolCase;
    }

    @JsonProperty("symbolCase")
    public void setSymbolCase(int value) {
        this.symbolCase = value;
    }

    @JsonProperty("regexReplace")
    public String getRegexReplace() {
        return regexReplace;
    }

    @JsonProperty("regexReplace")
    public void setRegexReplace(String value) {
        this.regexReplace = value;
    }
}