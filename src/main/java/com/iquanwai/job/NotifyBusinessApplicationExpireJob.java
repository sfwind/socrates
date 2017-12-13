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
 * Created by 三十文 on 2017/10/13
 */
@Component
public class NotifyBusinessApplicationExpireJob {
    @Autowired
    private BusinessSchoolService businessSchoolService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 0 20 * * ?")
    public void work() {
        logger.info("商学院申请未报名用户提醒任务开始");
        sendBSApplicationExpireMessage();
        logger.info("商学院申请未报名用户提醒任务结束");
    }

    private void sendBSApplicationExpireMessage() {
        // 优惠券的过期展示日期要比数据库中减少一天
        // 会员申请通过之后，优惠券的有效日期是 2 天，提醒日期为第六天晚上
        Date oneDay = DateUtils.beforeDays(new Date(), 1);
        Date twoDay = DateUtils.beforeDays(new Date(), 2);
        businessSchoolService.sendRiseMemberApplyMessageByAddTime(oneDay, 1);
        businessSchoolService.sendRiseMemberApplyMessageByAddTime(twoDay, 0);
    }

}
