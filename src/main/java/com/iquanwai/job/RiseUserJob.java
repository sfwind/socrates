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

    @Scheduled(cron = "0 15 18 * * ?")
    public void work() {
        logger.info("start rise user login job");
        customerService.userLoginLog(2);
        customerService.userLoginLog(1);
        logger.info("end  rise user login job");
    }
}
