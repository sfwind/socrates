package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


/**
 * Created by 三十文 on 2017/10/11
 */
@Component
public class NotifyRiseMemberExpireJob {

    @Autowired
    CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SEVEN_EXPIRE_DATE = 7;
    private static final int THREE_EXPIRE_DATE = 3;
    private static final int ONE_EXPIRE_DATE = 1;

    @Scheduled(cron = "0/20 * * * * ?")
    public void work() {
        logger.info("会员即将过期的模板消息提醒任务开始");
        riseMemberExpireCheck();
        logger.info("会员即将过期的模板消息提醒任务结束");
    }

    private void riseMemberExpireCheck() {
        // 会员过期前7、3、1天发送模板消息提醒
        Date sevenExpireDate = DateUtils.afterDays(new Date(), SEVEN_EXPIRE_DATE);
        List<RiseMember> sevenRiseMembers = customerService.loadRiseMembersByExpireDate(sevenExpireDate);
        logger.info("7天人数：{}", sevenRiseMembers.size());
        customerService.sendWillExpireMessage(sevenRiseMembers, SEVEN_EXPIRE_DATE);

        Date threeExpireDate = DateUtils.afterDays(new Date(), THREE_EXPIRE_DATE);
        List<RiseMember> threeRiseMembers = customerService.loadRiseMembersByExpireDate(threeExpireDate);
        logger.info("3天人数：{}", sevenRiseMembers.size());
        customerService.sendWillExpireMessage(threeRiseMembers, THREE_EXPIRE_DATE);

        Date oneExpireDate = DateUtils.afterDays(new Date(), ONE_EXPIRE_DATE);
        List<RiseMember> oneRiseMembers = customerService.loadRiseMembersByExpireDate(oneExpireDate);
        logger.info("1天人数：{}", sevenRiseMembers.size());
        customerService.sendWillExpireMessage(oneRiseMembers, ONE_EXPIRE_DATE);

        // 会员过期前3天发送短信消息提醒
        customerService.sendWillExpireShortMessage(threeRiseMembers, THREE_EXPIRE_DATE);
    }

}
