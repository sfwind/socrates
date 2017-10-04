package com.iquanwai.job;

import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by nethunder on 2017/10/4.
 */
@Component
public class BusinessSchoolJob {
    @Autowired
    BusinessSchoolService businessSchoolService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 0 3 * * ?")
    public void work() {
        logger.info("商学院申请收集任务开始");
        businessSchoolService.searchApplications(DateUtils.beforeDays(new Date(), 1));
        logger.info("商学院申请收集任务开始");
    }
}
