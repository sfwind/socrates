package com.iquanwai.job.notify;

import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.util.DateUtils;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by 三十文 on 2017/10/13
 */
@Component
public class NotifyBusinessApplicationExpireJob {
    @Autowired
    private BusinessSchoolService businessSchoolService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    //@Scheduled(cron = "0 0 13,20 * * ?")
    //todo:测试
    @Scheduled(cron = "0 */1 * * * ?")
    @CatInspect(name = "notifyBusinessApplyPassedButNoPay")
    public void work() {
        logger.info("商学院申请未报名用户提醒任务开始");
        sendBSApplicationExpireMessage();
        logger.info("商学院申请未报名用户提醒任务结束");
    }

    private void sendBSApplicationExpireMessage() {
        // 优惠券的过期展示日期要比数据库中减少一天
        //TODO:0修改为1
        Date oneDay = DateUtils.beforeDays(new Date(), 0);
        businessSchoolService.sendRiseMemberApplyMessageByDealTime(oneDay, 0);
    }

}
