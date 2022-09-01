package com.unitech.scanner.utility.config.formatting;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
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


public class Converter {
    // Date-time helpers

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static OffsetDateTime parseDateTimeString(String str) {
        DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .appendOptional(DateTimeFormatter.ISO_INSTANT)
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SX"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .toFormatter()
                .withZone(ZoneOffset.UTC);
        return ZonedDateTime.from(DATE_TIME_FORMATTER.parse(str)).toOffsetDateTime();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static OffsetTime parseTimeString(String str) {
        DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_TIME)
                .appendOptional(DateTimeFormatter.ISO_OFFSET_TIME)
                .parseDefaulting(ChronoField.YEAR, 2021)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 2)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 22)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
        return ZonedDateTime.from(TIME_FORMATTER.parse(str)).toOffsetDateTime().toOffsetTime();
    }
    // Serialize/deserialize helpers

    public static Formatting fromJsonString(String json) throws IOException, JSONException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getObjectReader().readValue(json);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return getFormatting(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static Formatting getFormatting(JSONObject jsonObject) throws JSONException {
        Formatting formatting = new Formatting();
        ArrayList <FormattingElement> formattingElements = new ArrayList <>();
        if (jsonObject.has("formatting")) {
            JSONArray formattingElementsJson = jsonObject.getJSONArray("formatting");
            if (formattingElementsJson.length() > 0) {
                for (int i = 0; i < formattingElementsJson.length(); i++) {
                    JSONObject element = (JSONObject) formattingElementsJson.get(i);
                    formattingElements.add(getFormattingElement(element));
                }
            }
        }
        formatting.setFormatting(formattingElements);
        return formatting;
    }

    private static FormattingElement getFormattingElement(JSONObject jsonObject) throws JSONException {
        FormattingElement formattingElement = new FormattingElement();
        if (jsonObject.has("enable")) {
            boolean elementEnable = jsonObject.getBoolean("enable");
            formattingElement.setEnable(elementEnable);
        } else {
            formattingElement.setEnable(true);
        }
        if (jsonObject.has("type")) {
            int elementType = jsonObject.getInt("type");
            formattingElement.setType(elementType);
        } else {
            formattingElement.setType(-1);
        }
        ArrayList <Rule> rules = new ArrayList <>();
        if (jsonObject.has("rule")) {
            JSONArray rulesJson = jsonObject.getJSONArray("rule");
            if (rulesJson.length() > 0) {
                for (int i = 0; i < rulesJson.length(); i++) {
                    JSONObject rule = (JSONObject) rulesJson.get(i);
                    rules.add(getRule(rule));
                }
            }
        }
        formattingElement.setRule(rules);
        return formattingElement;
    }

    private static Rule getRule(JSONObject jsonObject) throws JSONException {
        Rule rule = new Rule();

        if (jsonObject.has("enable")) {
            boolean ruleEnable = jsonObject.getBoolean("enable");
            rule.setEnable(ruleEnable);
        } else {
            rule.setEnable(true);
        }
        if (jsonObject.has("filterOnly")) {
            boolean ruleFilter = jsonObject.getBoolean("filterOnly");
            rule.setFilterOnly(ruleFilter);
        } else {
            rule.setFilterOnly(true);
        }
        if (jsonObject.has("name")) {
            String ruleName = jsonObject.getString("name");
            rule.setName(ruleName);
        } else {
            rule.setName("");
        }
        if (jsonObject.has("regex")) {
            String ruleRegEx = jsonObject.getString("regex");
            rule.setRegex(ruleRegEx);
        } else {
            rule.setRegex("");
        }
        List <Action> actions = new ArrayList <>();
        if (jsonObject.has("action")) {
            JSONArray actionJson = jsonObject.getJSONArray("action");
            if (actionJson.length() > 0) {
                for (int i = 0; i < actionJson.length(); i++) {
                    JSONObject action = (JSONObject) actionJson.get(i);
                    actions.add(getAction(action));
                }
            }
        }
        rule.setAction(actions);
        return rule;
    }

    private static Action getAction(JSONObject jsonObject) throws JSONException {
        Action action = new Action();

        if (jsonObject.has("enable")) {
            boolean actionEnable = jsonObject.getBoolean("enable");
            action.setEnable(actionEnable);
        } else {
            action.setEnable(false);
        }
        if (jsonObject.has("do")) {
            String actionDo = jsonObject.getString("do");
            action.setActionDo(actionDo);
        } else {
            action.setActionDo(null);
        }
        if (jsonObject.has("content")) {
            String actionContent = jsonObject.getString("content");
            action.setContent(actionContent);
        } else {
            action.setContent(null);
        }
        if (jsonObject.has("regexReplace")) {
            String actionRegexReplace = jsonObject.getString("regexReplace");
            action.setRegexReplace(actionRegexReplace);
        } else {
            action.setContent(null);
        }
        if (jsonObject.has("index")) {
            int actionIndex = jsonObject.getInt("index");
            action.setIndex(actionIndex);
        } else {
            action.setIndex(-1);
        }
        if (jsonObject.has("length")) {
            int actionLength = jsonObject.getInt("length");
            action.setLength(actionLength);
        } else {
            action.setLength(-1);
        }
        if (jsonObject.has("symbolCase")) {
            int actionSymbolCase = jsonObject.getInt("symbolCase");
            action.setSymbolCase(actionSymbolCase);
        } else {
            action.setSymbolCase(-1);
        }
        return action;
    }

    public static String toJsonString(Formatting obj) throws JsonProcessingException, JSONException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getObjectWriter().writeValueAsString(obj);
        } else {
            return formatting2Json(obj).toString();
        }
    }

    private static JSONObject formatting2Json(Formatting obj) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        ArrayList <FormattingElement> formattingElements = obj.getFormatting();
        JSONArray jsonArray = new JSONArray();
        if (formattingElements != null && formattingElements.size() > 0) {
            for (FormattingElement formattingElement : formattingElements) {
                jsonArray.put(formattingElement2Json(formattingElement));
            }
        }
        jsonObject.put("formatting", jsonArray);
        return jsonObject;
    }

    private static JSONObject formattingElement2Json(FormattingElement obj) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable", obj.getEnable());
        jsonObject.put("type", obj.getType());
        ArrayList <Rule> rules = obj.getRules();
        JSONArray jsonArray = new JSONArray();
        if (rules != null && rules.size() > 0) {
            for (Rule rule : rules) {
                jsonArray.put(rule2Json(rule));
            }
        }
        jsonObject.put("rule", jsonArray);
        return jsonObject;
    }

    private static JSONObject rule2Json(Rule obj) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable", obj.getEnable());
        jsonObject.put("filterOnly", obj.getFilterOnly());
        jsonObject.put("name", obj.getName());
        jsonObject.put("regex", obj.getRegex());
        ArrayList <Action> actions = obj.getActions();
        JSONArray jsonArray = new JSONArray();
        if (actions != null && actions.size() > 0) {
            for (Action action : actions) {
                jsonArray.put(action2Json(action));
            }
        }
        jsonObject.put("action", jsonArray);
        return jsonObject;
    }

    private static JSONObject action2Json(Action obj) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enable", obj.getEnable());
        jsonObject.put("do", obj.getActionDo());
        jsonObject.put("index", obj.getIndex());
        jsonObject.put("length", obj.getLength());
        jsonObject.put("symbolCase", obj.getSymbolCase());
        jsonObject.put("content", obj.getContent());
        jsonObject.put("regexReplace", obj.getRegexReplace());
        return jsonObject;
    }


    private static ObjectReader reader;
    private static ObjectWriter writer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void instantiateMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new JsonDeserializer <OffsetDateTime>() {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                String value = jsonParser.getText();
                return Converter.parseDateTimeString(value);
            }
        });
        mapper.registerModule(module);
        reader = mapper.readerFor(Formatting.class);
        writer = mapper.writerFor(Formatting.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static ObjectReader getObjectReader() {
        if (reader == null) instantiateMapper();
        return reader;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static ObjectWriter getObjectWriter() {
        if (writer == null) instantiateMapper();
        return writer;
    }
}