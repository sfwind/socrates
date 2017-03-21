package com.iquanwai.domain.message;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by justin on 16/8/10.
 */
@Service
public class TemplateMessageServiceImpl implements TemplateMessageService {
    @Autowired
    private RestfulHelper restfulHelper;

    public boolean sendMessage(TemplateMessage templateMessage) {
//        if(ConfigUtils.messageSwitch()) {
            String url = SEND_MESSAGE_URL;
            String json = new Gson().toJson(templateMessage);
            String body = restfulHelper.post(url, json);
            return StringUtils.isNotEmpty(body);
//        }

//        return false;
    }

//    public String getTemplateId(String templateShortId) {
//        Map<String, String> map = Maps.newHashMap();
//        map.put("template_id_short", templateShortId);
//
//        String url = SEND_MESSAGE_URL;
//        String json = new Gson().toJson(map);
//        String body = restfulHelper.post(url, json);
//
//        Map<String, Object> response = CommonUtils.jsonToMap(body);
//        return (String)response.get("template_id");
//    }
}
