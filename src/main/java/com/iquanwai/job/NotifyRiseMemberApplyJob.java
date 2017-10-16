package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
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
public class NotifyRiseMemberApplyJob {

    @Autowired
    private CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "*/20 * * * * ?")
    public void work() {
        logger.info("商学院申请未报名用户提醒任务开始");
        sendRiseMemberApplyMessage();
        logger.info("商学院申请未报名用户提醒任务结束");
    }

    private void sendRiseMemberApplyMessage() {
        // 优惠券是数据库中日期的 0 点，所以在查询的时候要减 1，但是描述时正常描述
        Date sixDate = DateUtils.beforeDays(new Date(), 6);
        customerService.sendRiseMemberApplyMessageByAddTime(sixDate, 1);

        customerService.sendRiseMemberApplyShortMessageByAddTime(sixDate);
    }

}
