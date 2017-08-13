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

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/7/19.
 */
@Component
public class NotifyFreeUserJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private CustomerService customerService;

    private static final String INDEX_URL = "/rise/static/plan/main";

    @Scheduled(cron = "0 30 21 * * ?")
    public void work() {
        logger.info("提醒限免用户任务开始");
        notifyInactiveUser();
        logger.info("提醒限免用户任务结束");
    }

    private void notifyInactiveUser() {
        List<ImprovementPlan> improvementPlans = planService.loadFreeInactiveUserPlan();
        improvementPlans.stream().forEach(improvementPlan -> {
            try {
                Profile profile = customerService.getProfile(improvementPlan.getProfileId());
                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(improvementPlan.getOpenid());
                templateMessage.setTemplate_id(ConfigUtils.getLearningNotifyMsg());

                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                templateMessage.setUrl(ConfigUtils.getAppDomain() + INDEX_URL);

                int closeTime = DateUtils.interval(new Date(), improvementPlan.getCloseDate()) + 1;
                data.put("first", new TemplateMessage.Keyword(profile.getNickname() + "同学，晚上好！\n\n" +
                        "快来完成今天的学习任务吧\n", "#000000"));
                data.put("keyword1", new TemplateMessage.Keyword("找到本质问题，减少无效努力", "#000000"));
                data.put("keyword2", new TemplateMessage.Keyword("距到期还有" + closeTime + "天", "#000000"));
                data.put("remark", new TemplateMessage.Keyword("\n点此卡片开始学习！", "#f57f16"));

                templateMessageService.sendMessage(templateMessage);
            } catch (Exception e) {
                logger.error("发送" + improvementPlan.getOpenid() + "失败", e);
            }

        });
    }
}
