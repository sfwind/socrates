package com.iquanwai.domain.message;

import com.google.gson.Gson;
import com.iquanwai.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 三十文 on 2017/10/11
 */
@Service
public class ShortMessageService {

    @Autowired
    private RestfulHelper restfulHelper;

    public void sendShortMessage(SMSDto smsDto) {
        String shortMessageUrl = ConfigUtils.sendShortMessageUrl();
        Gson gson = new Gson();
        restfulHelper.post(shortMessageUrl, gson.toJson(smsDto));
    }

}
