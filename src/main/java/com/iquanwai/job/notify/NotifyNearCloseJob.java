package com.iquanwai.job.notify;

import com.google.common.collect.Maps;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Problem;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/3/21.
 */
@Component
public class NotifyNearCloseJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private TemplateMessageService templateMessageService;

    private static final String INDEX_URL = "/rise/static/learn";

    @Scheduled(cron = "0 25 21 * * ?")
    @CatInspect(name = "notifyNearClosePlan")
    public void work() {
        logger.info("通知即将关闭课程任务开始");
        //发送点赞数统计
        notifyNearClosePlan();
        logger.info("通知即将关闭课程任务结束");
    }

    private void notifyNearClosePlan() {
        List<ImprovementPlan> underClosedPlans = planService.loadUnderClosePlan();
        underClosedPlans.stream().forEach(improvementPlan -> {
            TemplateMessage templateMessage = new TemplateMessage();
            Profile profile = customerService.getProfile(improvementPlan.getProfileId());
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.getUnderCloseMsg());
            templateMessage.setUrl(ConfigUtils.getAppDomain() + INDEX_URL);

            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);

            Problem problem = planService.getProblem(improvementPlan.getProblemId());

            data.put("first", new TemplateMessage.Keyword("你的以下课程还有3天就到期了：\n"));
            data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(improvementPlan.getCloseDate())));
            data.put("remark", new TemplateMessage.Keyword("\n抓紧在到期前解锁所有练习吧！"));

            templateMessageService.sendMessage(templateMessage);
        });
    }
}
