package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by nethunder on 2017/4/13.
 */
@Component
public class RiseMemberJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CustomerService customerService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void work() {
        logger.info("会员过期任务开始");
        customerService.checkMemberExpired();
        logger.info("会员过期任务结束");
    }
}
