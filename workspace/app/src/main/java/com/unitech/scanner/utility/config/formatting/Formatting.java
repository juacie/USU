package com.unitech.scanner.utility.config.formatting;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/27 上午 11:58
 * 修改人:user
 * 修改時間:2021/1/27 上午 11:58
 * 修改備註:
 */

public class Formatting {
    private FormattingElement[] formatting=null;

    @JsonProperty("formatting")
    public ArrayList <FormattingElement> getFormatting() {
        if (formatting == null || formatting.length == 0) {
            return null;
        }
        List <FormattingElement> formattingList = Arrays.asList(formatting);
        return new ArrayList<>(formattingList);

    }

    @JsonProperty("formatting")
    public void setFormatting(ArrayList <FormattingElement> value) {
        if (value != null && value.size() > 0) {
            FormattingElement[] formattingElements = new FormattingElement[value.size()];
            for (int i = 0; i < value.size(); i++) {
                formattingElements[i] = value.get(i);
            }
            this.formatting = formattingElements;
        } else {
            this.formatting = null;
        }
    }

    public ArrayList <Action> getActionList(int codeType, int index) {
        ArrayList <Rule> rules = getRuleList(codeType);
        if (rules == null) {
            return null;
        }
        int ruleLength = rules.size();
        if (ruleLength < 1) {
            return null;
        }
        if(ruleLength>index){
            return rules.get(index).getActions();
        }
        return null;
    }

    public ArrayList <Rule> getRuleList(int codeType) {
        if (formatting == null) {
            return null;
        }
        int formattingLength = formatting.length;
        if (formattingLength < 1) {
            return null;
        }
        for (FormattingElement formattingElement : formatting) {
            int type = formattingElement.getType();
            if (type == codeType) {
                return formattingElement.getRules();
            }
        }
        return null;
    }


}