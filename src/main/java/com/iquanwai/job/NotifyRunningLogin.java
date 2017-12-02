package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.message.CustomerMessageService;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by nethunder on 2017/9/5.
 */
@Component
public class NotifyRunningLogin {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INDEX_URL = "/rise/static/rise";
    private static final String PERSONAL_URL = "/rise/static/customer/personal";

    @Autowired
    private PlanService planService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerMessageService customerMessageService;

    @Scheduled(cron = "0 30 21 ? * MON-FRI")
    public void notifyHasRunningPlansLogin() {
        logger.info("开始未登录提醒job");
        List<ImprovementPlan> runningUnlogin = planService.loadRunningUnlogin();
        logger.info("待提醒人数：{}", runningUnlogin.size());
        runningUnlogin.forEach(this::sendNotifyMsg);
        logger.info("未登录提醒job结束");
    }

    private void sendNotifyMsg(ImprovementPlan plan) {
        try {
            Profile profile = customerService.getProfile(plan.getProfileId());
            if (profile == null) {
                logger.error("用户:{} 未找到", plan.getProfileId());
                return;
            }
            if (!profile.getLearningNotify()) {
                logger.info("用户:{} 关闭学习提醒", plan.getProfileId());
                return;
            }
            logger.info("用户:{} 发送未登录提醒", plan.getProfileId());

            String openId = profile.getOpenid();
            String content = profile.getNickname() + "同学，晚上好！\n\n"
                    + "快来继续学习你的课程<a href='" + ConfigUtils.getAppDomain() + INDEX_URL + "'>《" + plan.getProblemName() + "》</a>，拿下一个职场新技能！\n\n"
                    + "如不需要学习提醒，点击<a href='" + ConfigUtils.getAppDomain() + PERSONAL_URL + "'>这里</a>关闭";
            customerMessageService.sendCustomerMessage(openId, content, Constants.WEIXIN_MESSAGE_TYPE.TEXT);

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
