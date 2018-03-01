package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by nethunder on 2017/4/21.
 */
@Component
public class RiseUserJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerService customerService;

    @Scheduled(cron = "0 30 11 * * ?")
    public void work() {
        logger.info("记录用户昨日登录任务开始");
        customerService.userLoginLog();
        logger.info("记录用户昨日登录任务开始");
    }
}
