package com.iquanwai.domain.accesstoken;


import com.iquanwai.domain.dao.AccessTokenDao;
import com.iquanwai.domain.dao.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    private static String accessToken;
    protected static Logger logger = LoggerFactory.getLogger(AccessTokenService.class);
    @Autowired
    private WeiXinAccessTokenRepo weiXinAccessTokenRepo;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AccessTokenDao accessTokenDao;

    @Override
    public String getAccessToken() {
        if(accessToken!=null){
            return accessToken;
        }

        String token = redisUtil.get("accessToken");
        if(token==null){
            logger.info("insert access token");
            String accessToken = _getAccessToken();
            redisUtil.set("accessToken", accessToken);
            accessTokenDao.insertOrUpdate(accessToken);
        }else {
            accessToken = token;
        }

        return accessToken;
    }

    private String _getAccessToken() {
        logger.info("refreshing access token");
        String strAccessToken = weiXinAccessTokenRepo.getAccessToken();
        if(strAccessToken!=null){
            accessToken = strAccessToken;
        }
        return accessToken;
    }

    @Override
    public String refreshAccessToken(boolean force) {
        if(force) {
            forceUpdateAccessToken();
        }else{
            String token = redisUtil.get("accessToken");
            if(token==null){
                logger.info("insert access token");
                String accessToken = _getAccessToken();
                redisUtil.set("accessToken", accessToken);
                accessTokenDao.insertOrUpdate(accessToken);
            }else{
                //如果数据库的accessToken未刷新,则强制刷新
                if(token.equals(accessToken)){
                    forceUpdateAccessToken();
                }else{
                    //如果数据库的accessToken已刷新,返回数据库的token
                    logger.info("reload access token");
                    accessToken = token;
                }
            }
        }

        return accessToken;
    }

    private void forceUpdateAccessToken(){
        String accessToken = _getAccessToken();
        redisUtil.set("accessToken", accessToken);
        accessTokenDao.insertOrUpdate(accessToken);
    }
}