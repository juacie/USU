package com.unitech.scanner.utility.service.mainUsage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.ErrorMessage;
import com.unitech.scanner.utility.config.formatting.Action;
import com.unitech.scanner.utility.config.formatting.Converter;
import com.unitech.scanner.utility.config.formatting.Formatting;
import com.unitech.scanner.utility.config.formatting.FormattingElement;
import com.unitech.scanner.utility.config.formatting.Rule;

import org.json.JSONException;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/3 下午 03:03
 * 修改人:user
 * 修改時間:2021/2/3 下午 03:03
 * 修改備註:
 */

public class FormattingData {
    //==============================================================================================
    private final Context context;
    private final SharedPreferences defaultPref;

    //==============================================================================================
    public FormattingData(Context context) {
        this.context = context;
        this.defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //==============================================================================================
    public String getTerminator() {
        String terminator_input;
        int terminatorListIndex = 2;//0:none 1:CR 2:LF 3:CRLF 4:TAB
        String terminator = defaultPref.getString(context.getString(R.string.setting_Terminator), AllDefaultValue.setting_Terminator);
        if (terminator != null) {
            terminatorListIndex = Integer.parseInt(terminator);
        }
        switch (terminatorListIndex) {
            case 0:
                terminator_input = "";
                break;
            case 1:
                terminator_input = String.valueOf((char) 0x0D);
                break;
            case 3:
                terminator_input = String.valueOf((char) 0x0D + (char) 0x0A);
                break;
            case 4:
                terminator_input = String.valueOf((char) 0x09);
                break;
            default:
                terminator_input = String.valueOf((char) 0x0A);
                break;
        }
        return terminator_input;
    }

    public String getPrefix() {
        String prefix = "";
        String sPrefix = defaultPref.getString(context.getString(R.string.setting_Prefix), AllDefaultValue.setting_Prefix);
        if (sPrefix != null) {
            prefix = sPrefix;
        }
        prefix= prefix
                .replace("<CR>",String.valueOf((char)0X0D))
                .replace("<LF>",String.valueOf((char)0X0A))
                .replace("<TAB>",String.valueOf((char)0x09))
        ;
        return prefix;
    }

    public String getSuffix() {
        String suffix = "";
        String sSuffix = defaultPref.getString(context.getString(R.string.setting_Suffix), AllDefaultValue.setting_Suffix);
        if (sSuffix != null) {
            suffix = sSuffix;
        }
        suffix= suffix
                .replace("<CR>",String.valueOf((char)0X0D))
                .replace("<LF>",String.valueOf((char)0X0A))
                .replace("<TAB>",String.valueOf((char)0x09))
        ;
        return suffix;
    }

    private String getReplace(String data) {
        String replace = defaultPref.getString(context.getString(R.string.setting_Replace), AllDefaultValue.setting_Replace);
        if (replace != null && !replace.equals("") && replace.contains("->")) {
            String[] temp = replace.split("->");
            if(temp.length>0){
                for(int i=0;i<temp.length;i++){
                    temp[i] = temp[i]
                            .replace("<CR>",String.valueOf((char)0X0D))
                            .replace("<LF>",String.valueOf((char)0X0A))
                            .replace("<TAB>",String.valueOf((char)0x09))
                    ;
                }
                if (temp.length == 1) {
                    data = data.replace(temp[0], "");
                } else if (temp.length == 2) {
                    data = data.replace(temp[0], temp[1]);
                }
            }

        }
        return data;
    }

    private String getRemoveNonChar(String data) {
        boolean removeNonPrintableChar = defaultPref.getBoolean(context.getString(R.string.setting_RemoveNonPrintableChar), AllDefaultValue.setting_RemoveNonPrintableChar);
        String tmpData = data;
        if (removeNonPrintableChar) {
            tmpData = tmpData.replaceAll("\\p{C}", "");
        }
        return tmpData;
    }

    public String getLastData(String data, int codeType) throws Exception {
        if (data == null) {
            throw new Exception(ErrorMessage.NO_DATA);
        }
        boolean useFormatting = defaultPref.getBoolean(context.getString(R.string.setting_UseFormatting), AllDefaultValue.setting_UseFormatting);
        return useFormatting ? useFormatting(data, codeType < 0 ? codeType + 256 : codeType) : useDataEditing(data);
    }

    private String useDataEditing(String data) {
        String returnData = data;
        returnData = getRemoveNonChar(returnData);
        returnData = getReplace(returnData);
        returnData = getPrefix() + returnData + getSuffix() + getTerminator();
        return returnData;
    }

    private String useFormatting(String origin_data, int codeType) throws IOException, JSONException {
        String returnData = origin_data;
        returnData = getRemoveNonChar(returnData);
        String jsonFormatting = defaultPref.getString(context.getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
        Formatting formatting = Converter.fromJsonString(jsonFormatting);
        ArrayList <FormattingElement> formattingElements = formatting.getFormatting();
        if (formattingElements == null) {
            return returnData;
        }
        boolean findFormattingElement = false;
        for (FormattingElement formattingElement:formattingElements){
            if(formattingElement.getType()==codeType){
                findFormattingElement = true;
                if(!formattingElement.getEnable()){
                    return returnData;
                }
                break;
            }
        }
        if(!findFormattingElement){
            return returnData;
        }
        ArrayList <Rule> rules = formatting.getRuleList(codeType);
        if (rules == null) {
            return returnData;
        }
        for (Rule rule : rules) {
            boolean enable = rule.getEnable();
            if (!enable) {
                continue;
            }
            boolean filterOnly = rule.getFilterOnly();
            String regEx = rule.getRegex();
//            regEx = escapeExprSpecialWord(regEx);
            Pattern p = Pattern.compile(regEx);
            Matcher matcher = p.matcher(returnData);
            if (!filterOnly) {
                StringBuilder str = new StringBuilder();
                while (matcher.find()) {
                    Logger.debug("TestRegEx find = true");
                    String group = matcher.group();
                    Logger.debug("TestRegEx group = " + group);
                    str.append(group);
                }
                if (!str.toString().equals("")) {
                    returnData = str.toString();
                }
            } else {

                boolean find = matcher.find();
                Logger.debug("regEx = " + regEx + " ,  matcher.find() = " + find);
                if (find) {
                    ArrayList <Action> actions = rule.getActions();
                    if (actions != null && actions.size() > 0) {
                        for (Action action : actions) {
                            boolean enableAction = action.getEnable();
                            if (!enableAction) {
                                continue;
                            }
                            int index = action.getIndex();
                            int range = action.getLength();
                            int switchCase = action.getSymbolCase();
                            String content = action.getContent();
                            String regexReplace = action.getRegexReplace();
                            if (content != null) {
                                content = content.replace("<LF>", String.valueOf(0x0A));
                                content = content.replace("<TAB>", String.valueOf(0x09));
                            }
                            switch (action.getActionDo().toUpperCase()) {
                                case "INPUT":
                                    if (index <= returnData.length() && index >= 0) {
                                        Logger.debug("returnData = " + returnData + "\nDO INPUT INDEX = " + index + " , LENGTH = " + range + " , CASE = " + switchCase + " , CONTENT = " + content);
                                        StringBuilder stringBuilder = new StringBuilder(returnData);
                                        if (index == 0) {
                                            stringBuilder.append(content);
                                        } else {
                                            stringBuilder.insert(index - 1, content);
                                        }
                                        returnData = stringBuilder.toString();
                                    }
                                    break;
                                case "SWITCH":
                                    if (index <= returnData.length() && index >= 1 && range <= returnData.length() && range >= 0) {
                                        Logger.debug("returnData = " + returnData + "\nDO SWITCH INDEX = " + index + " , LENGTH = " + range + " , CASE = " + switchCase + " , CONTENT = " + content);
                                        if (index == 1) {
                                            if (range == 0) {
                                                returnData = setCase(returnData, switchCase);
                                            } else {
                                                String subString1 = returnData.substring(0, range - 1);
                                                String subString2 = returnData.replaceFirst(subString1, "");
                                                returnData = setCase(subString1, switchCase) + subString2;
                                            }
                                        } else {
                                            String subString1 = returnData.substring(0, index - 1);
                                            String remove1 = returnData.replaceFirst(subString1, "");
                                            String subString2, subString3 = "";
                                            if (range == 0) {
                                                subString2 = setCase(remove1, switchCase);
                                            } else {
                                                String temp2 = remove1.substring(0, range);
                                                if (remove1.length() > temp2.length()) {
                                                    subString3 = remove1.substring(temp2.length());
                                                }
                                                subString2 = setCase(temp2, switchCase);
                                            }
                                            returnData = subString1 + subString2 + subString3;
                                        }
                                    }
                                    break;
                                case "REPLACE":
                                    if (index <= returnData.length() && index >= 1 && range <= returnData.length() && range >= 0) {
                                        Logger.debug("returnData = " + returnData + "\nDO REPLACE INDEX = " + index + " , LENGTH = " + range + " , CASE = " + switchCase + " , CONTENT = " + content);
                                        if (index == 1) {
                                            if (range == 0) {
                                                returnData = content;
                                            } else {
                                                String subString1 = returnData.substring(0, range - 1);
                                                String subString2 = returnData.replaceFirst(subString1, "");
                                                returnData = content + subString2;
                                            }
                                        } else {
                                            String subString1 = returnData.substring(0, index - 1);
                                            String remove1 = returnData.replaceFirst(subString1, "");
                                            String subString2, subString3 = "";
                                            if (range != 0) {
                                                subString2 = remove1.substring(0, range);
                                                if (remove1.length() > subString2.length()) {
                                                    subString3 = remove1.substring(subString2.length());
                                                }
                                            }
                                            subString2 = content;
                                            returnData = subString1 + subString2 + subString3;
                                        }
                                    }
                                    break;
                                case "REGEX":
                                    Logger.debug("match regexReplace = "+regexReplace);
                                    for(int i=0;i<=matcher.groupCount();i++){
                                        Logger.debug("match group("+i+")="+matcher.group(i));
                                    }
                                    returnData = returnData.replaceAll(regEx,regexReplace);
                                    break;
                            }
                        }
                    }
                }
            }

        }
        return returnData;
    }

    private String setCase(String data, int caseType) {
        return (caseType == 0) ? data.toLowerCase() : data.toUpperCase();
    }
}
