package com.unitech.scanner.utility.config.formatting;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/27 上午 11:59
 * 修改人:user
 * 修改時間:2021/1/27 上午 11:59
 * 修改備註:
 */

public class Rule {
    private String name="";
    private boolean filterOnly = true;
    private String regex = "";
    private boolean enable=true;
    private Action[] action=null;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String value) {
        this.name = value;
    }

    @JsonProperty("filterOnly")
    public boolean getFilterOnly() {
        return filterOnly;
    }

    @JsonProperty("filterOnly")
    public void setFilterOnly(boolean value) {
        this.filterOnly = value;
    }

    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    @JsonProperty("regex")
    public void setRegex(String value) {
        this.regex = value;
    }

    @JsonProperty("enable")
    public boolean getEnable() {
        return enable;
    }

    @JsonProperty("enable")
    public void setEnable(boolean value) {
        this.enable = value;
    }

    @JsonProperty("action")
    public ArrayList <Action> getActions() {
        if (action == null) {
            return null;
        }
        List <Action> actions = Arrays.asList(action);
        return new ArrayList <>(actions);
    }

    @JsonProperty("action")
    public void setAction(List <Action> value) {
        if (value != null && value.size() > 0) {
            Action[] actions = new Action[value.size()];
            for (int i = 0; i < value.size(); i++) {
                actions[i] = value.get(i);
            }
            this.action = actions;
        } else {
            this.action = null;
        }
    }
}