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

public class FormattingElement {
    private int type = -1;
    private boolean enable = true;
    private Rule[] rule=null;

    @JsonProperty("type")
    public int getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(int value) {
        this.type = value;
    }

    @JsonProperty("enable")
    public boolean getEnable() {
        return enable;
    }
    @JsonProperty("enable")
    public void setEnable(boolean value) {
        this.enable = value;
    }

    @JsonProperty("rule")
    public ArrayList <Rule> getRules() {
        if (rule == null || rule.length == 0) {
            return null;
        }
        List <Rule> rules = Arrays.asList(rule);
        return new ArrayList <>(rules);
    }

    @JsonProperty("rule")
    public void setRule(ArrayList<Rule> value) {
        if(value!=null && value.size()>0){
            Rule[] rules = new Rule[value.size()];
            for(int i = 0;i<value.size();i++){
                rules[i] = value.get(i);
            }
            this.rule = rules;
        }else{
            this.rule = null;
        }
    }
}
