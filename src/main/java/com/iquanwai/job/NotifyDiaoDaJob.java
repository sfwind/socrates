package com.iquanwai.job;

import com.google.common.collect.Maps;
import com.iquanwai.domain.ActivityService;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Problem;
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
 * Created by justin on 17/7/4.
 */
@Component
public class NotifyDiaoDaJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ActivityService activityService;
    @Autowired
    private PlanService planService;
    @Autowired
    private TemplateMessageService templateMessageService;

    @Scheduled(cron = "0 0 20 * * ?")
    public void work() {
        logger.info("吊打通知任务开始");
        notifyDiaoDaUser();
        logger.info("吊打通知任务结束");
    }

    private void notifyDiaoDaUser() {
        List<Problem> problems = activityService.loadDiaoDaProblem();
        problems.forEach(problem -> {
            Integer problemId = problem.getId();
            List<ImprovementPlan> improvementPlans = planService.getLearningPlan(problemId);

            improvementPlans.forEach(improvementPlan -> {
                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(improvementPlan.getOpenid());
                templateMessage.setTemplate_id(ConfigUtils.getActivityStartMsg());

                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                templateMessage.setUrl(problem.getActivityUrl());

                data.put("first", new TemplateMessage.Keyword("你学习的小课“"+problem.getProblem()
                        +"”半小时后有作业吊打哦，快来看看大家怎么完善别人的作业吧！\n\n"+problem.getPassword()));
                data.put("keyword1", new TemplateMessage.Keyword("作业吊打"));
                data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())+" 20:30"));

                templateMessageService.sendMessage(templateMessage);
            });
        });
    }
}
