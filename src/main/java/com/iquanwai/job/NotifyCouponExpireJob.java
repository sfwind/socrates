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
 * Created by 三十文 on 2017/10/18
 */
@Component
public class NotifyCouponExpireJob {

    @Autowired
    private CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 0 21 * * ?")
    public void work() {
        logger.info("优惠券即将过期提醒任务开始");
        sendCouponExpireNotify();
        logger.info("优惠券即将过期提醒任务开始");
    }

    private void sendCouponExpireNotify() {
        // 优惠券过期前一天晚上提醒
        Date twoDay = DateUtils.afterDays(new Date(), 2);
        customerService.sendWillExpireCouponMessage(twoDay);
    }

}
