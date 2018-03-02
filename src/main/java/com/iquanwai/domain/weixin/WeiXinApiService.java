package com.iquanwai.domain.weixin;

/**
 * Created by 三十文
 */
public interface WeiXinApiService {
    WeiXinResult.UserInfoObject getWeiXinUserInfo(String openId);
}
