package com.iquanwai.job.notify;

import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.util.cat.CatInspect;
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
public class NotifyBusinessApplicationJob {
    @Autowired
    BusinessSchoolService businessSchoolService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 30 7,21 * * ?")
    @CatInspect(name = "notifyBusinessApply")
    public void work() {
        logger.info("商学院申请通知任务开始");
        businessSchoolService.noticeApplication(new Date());
        logger.info("商学院申请通知任务开始");
    }


}
