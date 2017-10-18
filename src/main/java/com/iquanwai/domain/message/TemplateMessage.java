package com.iquanwai.domain.message;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * Created by justin on 16/8/10.
 */
@Data
public class TemplateMessage {

    private String touser;
    private String template_id;
    private String url;

    private Map<String, Keyword> data = Maps.newHashMap();

    public static final String BLACK = "#000000";

    @Data
    public static class Keyword {
        public Keyword(String value) {
            this.value = value;
        }
        public Keyword(String value, String color) {
            this.value = value;
            this.color = color;
        }

        private String value;
        private String color = "#04136d";
    }

    public String getContent() {
        JSONObject dataJson = new JSONObject();
        for (Map.Entry entry : data.entrySet()) {
            dataJson.put(entry.getKey().toString(), entry.getValue());
        }
        return dataJson.toString();
    }

}
