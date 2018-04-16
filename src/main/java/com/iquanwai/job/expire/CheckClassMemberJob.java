package com.iquanwai.job.expire;

import com.iquanwai.domain.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by 三十文
 */
@Component
public class CheckClassMemberJob {

    @Autowired
    private CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    public void work() {
        logger.info("开始校验学员身份数据");
        customerService.checkClassMemberExpire();
        logger.info("学员身份数据校验结束");
    }

}
