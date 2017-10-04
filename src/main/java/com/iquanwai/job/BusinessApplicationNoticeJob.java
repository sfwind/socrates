package com.iquanwai.job;

import com.iquanwai.domain.BusinessSchoolService;
import lombok.extern.apachecommons.CommonsLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by nethunder on 2017/10/4.
 */
@CommonsLog
public class BusinessApplicationNoticeJob {
    @Autowired
    BusinessSchoolService businessSchoolService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 0 20 * * ?")
    public void work() {
        logger.info("商学院申请通知任务开始");

        logger.info("商学院申请通知任务开始");
    }
}
