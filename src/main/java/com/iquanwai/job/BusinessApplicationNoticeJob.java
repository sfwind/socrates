package com.iquanwai.job;

import com.iquanwai.domain.BusinessSchoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by nethunder on 2017/10/4.
 */
@Component
public class BusinessApplicationNoticeJob {
    @Autowired
    BusinessSchoolService businessSchoolService;

    private Logger logger = LoggerFactory.getLogger(getClass());

//    @Scheduled(cron = "0 0 20 * * ?")
//    public void work() {
//        logger.info("商学院申请通知任务开始");
//        businessSchoolService.noticeApplication(new Date());
//        logger.info("商学院申请通知任务开始");
//    }


}
