package com.iquanwai.job.notify;

import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.util.DateUtils;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by 三十文 on 2017/10/11
 */
@Component
public class NotifyRiseMemberExpireJob {

    @Autowired
    CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    //@Scheduled(cron = "0 20 21 * * ?")
    //TODO:测试
    @Scheduled(cron = "0 1 * * * ?")
    @CatInspect(name = "notifyRiseMemberWillExpired")
    public void work() {
        logger.info("会员即将过期的模板消息提醒任务开始");
        riseMemberExpireCheck();
        logger.info("会员即将过期的模板消息提醒任务结束");
    }

    private void riseMemberExpireCheck() {
        // 会员过期前7、1天发送模板消息提醒
        Date sevenExpireDate = DateUtils.afterDays(new Date(), 8);
        List<RiseMember> sevenRiseMembers = customerService.loadRiseMembersByExpireDate(sevenExpireDate)
                .stream()
                .filter(item -> item.getMemberTypeId().equals(RiseMember.HALF) ||
                        item.getMemberTypeId().equals(RiseMember.ANNUAL) ||
                        item.getMemberTypeId().equals(RiseMember.ELITE) ||
                        item.getMemberTypeId().equals(RiseMember.HALF_ELITE) ||
                        item.getMemberTypeId().equals(RiseMember.BUSINESS_THOUGHT))
                .collect(Collectors.toList());
        logger.info("7天人数：{}", sevenRiseMembers.size());
        customerService.sendWillExpireMessage(sevenRiseMembers, 7);

        Date oneDate = DateUtils.afterDays(new Date(), 2);
        List<RiseMember> oneRiseMembers = customerService.loadRiseMembersByExpireDate(oneDate)
                .stream()
                .filter(item -> item.getMemberTypeId().equals(RiseMember.HALF) ||
                        item.getMemberTypeId().equals(RiseMember.ANNUAL) ||
                        item.getMemberTypeId().equals(RiseMember.ELITE) ||
                        item.getMemberTypeId().equals(RiseMember.HALF_ELITE) ||
                        item.getMemberTypeId().equals(RiseMember.BUSINESS_THOUGHT))
                .collect(Collectors.toList());
        logger.info("1天人数：{}", oneRiseMembers.size());
        customerService.sendWillExpireMessage(oneRiseMembers, 1);
        customerService.sendWillExpireShortMessage(oneRiseMembers, 1);
    }

}
