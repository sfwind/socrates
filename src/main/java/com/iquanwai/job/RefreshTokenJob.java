package com.iquanwai.job;

import com.iquanwai.domain.accessToken.AccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 17/7/20.
 */
@Component
public class RefreshTokenJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AccessTokenService accessTokenService;

    @Scheduled(cron="0 0 0/2 * * ?")
    public void work(){
        logger.info("AccessTokenJob start");
        accessTokenService.refreshAccessToken(true);
        logger.info("AccessTokenJob end");
    }
}
