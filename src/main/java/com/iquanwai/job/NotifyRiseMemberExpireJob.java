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

    @Scheduled(cron = "0/20 * * * * ?")
    public void work() {
        logger.info("会员即将过期的模板消息提醒任务开始");
        riseMemberExpireCheck();
        logger.info("会员即将过期的模板消息提醒任务结束");
    }

    private void riseMemberExpireCheck() {
        // 会员过期前7、3、1天发送模板消息提醒
        Date sevenExpireDate = DateUtils.afterDays(new Date(), 7);
        List<RiseMember> sevenRiseMembers = customerService.loadRiseMembersByExpireDate(sevenExpireDate);
        logger.info("7天人数：{}", sevenRiseMembers.size());
        customerService.sendWillExpireMessage(sevenRiseMembers, 7);

        // logger.info("3天人数：{}", threeRiseMembers.size());
        // customerService.sendWillExpireMessage(threeRiseMembers, 3);

        Date oneExpireDate = DateUtils.afterDays(new Date(), 1);
        List<RiseMember> oneRiseMembers = customerService.loadRiseMembersByExpireDate(oneExpireDate);
        logger.info("1天人数：{}", oneRiseMembers.size());
        customerService.sendWillExpireMessage(oneRiseMembers, 1);
        customerService.sendWillExpireShortMessage(oneRiseMembers, 1);

        // // 会员过期前3天发送短信消息提醒
        // Date threeExpireDate = DateUtils.afterDays(new Date(), 3);
        // List<RiseMember> threeRiseMembers = customerService.loadRiseMembersByExpireDate(threeExpireDate);
    }

}
