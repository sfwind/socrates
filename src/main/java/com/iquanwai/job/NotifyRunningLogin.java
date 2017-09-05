package com.iquanwai.job;

import com.google.common.collect.Maps;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/9/5.
 */
@Component
public class NotifyRunningLogin {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INDEX_URL = "/rise/static/plan/main";


    @Autowired
    private PlanService planService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private TemplateMessageService templateMessageService;

    @Scheduled(cron = "0 18 11 ? * MON-FRI")
    public void notifyHasRunningPlansLogin() {
        logger.info("开始未登录提醒job");
        List<ImprovementPlan> runningUnlogin = planService.loadRunningUnlogin();
        runningUnlogin.forEach(this::sendNotifyMsg);
        logger.info("未登录提醒job结束");
    }

    private void sendNotifyMsg(ImprovementPlan plan) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(plan.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getLearningNotifyMsg());
        templateMessage.setUrl(ConfigUtils.getAppDomain() + INDEX_URL);
        String closeDate = DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(plan.getCloseDate(), 1));
        Profile profile = customerService.getProfile(plan.getProfileId());
        String first = "\n" + profile.getNickname() + "同学，晚上好！快来学习今天的小课，拿下一个职场新技能！";
        data.put("first", new TemplateMessage.Keyword(first));
        data.put("keyword1", new TemplateMessage.Keyword(plan.getProblemName()));
        data.put("keyword2", new TemplateMessage.Keyword("今天——" + closeDate));
        data.put("remark", new TemplateMessage.Keyword("\n可以自觉学习，不需要提醒？点击上课啦，进入“我的”去关闭\n" +
                "\n" +
                "点此卡片开始学习"));
        templateMessageService.sendMessage(templateMessage);
    }
}
