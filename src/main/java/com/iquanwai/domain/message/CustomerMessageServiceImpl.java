package com.iquanwai.domain.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iquanwai.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/7/8.
 */
@Service
public class CustomerMessageServiceImpl implements CustomerMessageService {
    @Autowired
    private RestfulHelper restfulHelper;

    @Override
    public void sendCustomerMessage(String openid, String message, Integer type) {
        if (Constants.WEIXIN_MESSAGE_TYPE.TEXT == type) {
            TextCustomerMessage customerMessage = new TextCustomerMessage(openid, message);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String json = gson.toJson(customerMessage);
            restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        } else if (Constants.WEIXIN_MESSAGE_TYPE.IMAGE == type) {
            ImageCustomerMessage customerMessage = new ImageCustomerMessage(openid, message);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String json = gson.toJson(customerMessage);
            restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        } else if (Constants.WEIXIN_MESSAGE_TYPE.VOICE == type) {
            VoiceCustomerMessage customerMessage = new VoiceCustomerMessage(openid, message);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String json = gson.toJson(customerMessage);
            restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        }
    }
}
