package com.iquanwai.domain.weixin;


import com.google.common.collect.Maps;
import com.iquanwai.domain.accesstoken.AccessTokenService;
import com.iquanwai.domain.message.RestfulHelper;
import com.iquanwai.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by 三十文
 * 此接口用来放置所有微信调用相关接口
 */
@Service
public class WeiXinApiServiceImpl implements WeiXinApiService {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private RestfulHelper restfulHelper;

    /** 获取应用级 accessToken url */
    String APP_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    /** 引导用户授权，在回调接口中返回 code 值 */
    String OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appid}&redirect_uri={redirect_url}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect";
    /** 获取当前用户的 accessToken */
    String USER_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code";
    /** 当前用户 accessToken 过期时，通过 refreshToken 获取新 accessToken 和 refreshToken */
    String REFRESH_USER_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={appid}&grant_type=refresh_token&refresh_token={refresh_token}";
    /** 通过 jscode 换取 accessToken，运用在小程序 */
    String WE_MINI_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={jscode}&grant_type=authorization_code";
    /** 获取用户信息，accessToken 为用户 accessToken，openId 为当前平台对应 openId，包含昵称、头像、unionId等信息 */
    String SNS_API_USER_INFO = "https://api.weixin.qq.com/sns/userinfo?access_token={access_token}&openid={openid}&lang=zh_CN";
    /** 获取用户基本信息，accessToken 为应用级调用接口凭证，包含昵称、头像、unionId、省份、城市、性别等信息 */
    String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={access_token}&openid={openid}&lang=zh_CN";
    /** 获取用户列表，单次最大上线10000，从第一个开始拉取 */
    String GET_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}";
    /** 获取用户列表，单次最大上限10000，从 next_openid 开始拉取 */
    String GET_NEXT_USERS_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token={access_token}&next_openid={next_openid}";
    /** 获取黑名单人员 */
    String LIST_BLACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/getblacklist?access_token={access_token}";
    /** 将人员标记为黑名单 */
    String BATCH_BALCKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchblacklist?access_token={access_token}";
    /** 将人员移除黑名单 */
    String UNBATCH_BACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/batchunblacklist?access_token={access_token}";

    private static final String IP_REGEX = "(\\d*\\.){3}\\d*";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public WeiXinResult.UserInfoObject getWeiXinUserInfo(String openId) {
        Map<String, String> params = Maps.newHashMap();
        params.put("openid", openId);
        params.put("access_token", accessTokenService.getAccessToken());
        String requestUrl = CommonUtils.placeholderReplace(USER_INFO_URL, params);
        String body = restfulHelper.getPure(requestUrl);

        WeiXinResult.UserInfoObject userInfoObject = new WeiXinResult.UserInfoObject();
        try {
            if (CommonUtils.isError(body)) {
                logger.error("微信调用用户信息接口失败：{}", body);
                return null;
            }
            Map<String, Object> result = CommonUtils.jsonToMap(body);
            if (result.get("errcode") != null || result.get("errmsg") != null) {
                return null;
            }
            String newOpenId = result.get("openid").toString();
            String nickName = result.get("nickname").toString();
            Integer sex = null;
            try {
                Double tempSex = (Double) result.get("sex");
                if (tempSex != null) {
                    sex = tempSex.intValue();
                }
            } catch (Exception e1) {
                logger.error(e1.getLocalizedMessage(), e1);
            }
            String headImgUrl = result.get("headimgurl").toString();
            String country = result.get("country").toString();
            String province = result.get("province").toString();
            String city = result.get("city").toString();
            String unionId = result.get("unionid").toString();
            userInfoObject.setOpenId(newOpenId);
            userInfoObject.setNickName(nickName);
            userInfoObject.setSex(sex);
            userInfoObject.setHeadImgUrl(headImgUrl);
            userInfoObject.setCountry(country);
            userInfoObject.setProvince(province);
            userInfoObject.setCity(city);
            userInfoObject.setUnionId(unionId);
            return userInfoObject;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

}
