package com.iquanwai.job.expire;

import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设置BusinessSchoolApplication过期
 */
@Component
public class ExpireMemberApplyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BusinessSchoolService businessSchoolService;

    @Scheduled(cron = "0 0 * * * ?")
    @CatInspect(name = "closePlan")
    public void work() {
        logger.info("过期会员申请任务开始");
        businessSchoolService.expiredApply();
        logger.info("过期会员申请任务结束");
    }

}
