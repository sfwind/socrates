package com.iquanwai.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.message.RestfulHelper;
import com.iquanwai.domain.weixin.WeiXinApiService;
import com.iquanwai.domain.weixin.WeiXinResult;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by 三十文
 */
@Component
public class HeadImgUrlCheckReceiver {

    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private WeiXinApiService weiXinApiService;

    @Autowired
    private RestfulHelper restfulHelper;

    private static final String TOPIC = "profile_headImgUrl_check";
    private static final String QUEUE = "profile_headImgUrl_queue";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, message -> {
            logger.info("receive message: {}", message);
            JSONObject json = JSON.parseObject(message.getMessage().toString());
            Integer profileId = json.getInteger("profileId");
            String openId = json.getString("openId");
            String preHeadImgUrl = json.getString("headImgUrl");
            checkAndUpdateHeadImgUrl(profileId, openId, preHeadImgUrl);
        });
    }

    private void checkAndUpdateHeadImgUrl(Integer profileId, String openId, String preHeadImgUrl) {
        try {
            Response response = restfulHelper.getResponse(preHeadImgUrl);
            if (response != null) {
                String errorNo = response.header("X-ErrNo");
                if (errorNo != null) {
                    WeiXinResult.UserInfoObject userInfoObject = weiXinApiService.getWeiXinUserInfo(openId);
                    if (userInfoObject != null && userInfoObject.getHeadImgUrl() != null) {
                        customerService.updateHeadImgUrl(profileId, userInfoObject.getHeadImgUrl());
                        logger.info("更新用户 {} 头像成功", openId);
                    }
                }
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
