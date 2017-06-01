package com.iquanwai.job;

import com.google.common.collect.Maps;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.ImprovementPlan;
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
 * Created by xfduan on 2017/6/1.
 */
@Component
public class NotifyPreviousLoginJob {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private TemplateMessageService templateMessageService;

    @Scheduled(cron = "0 0 20 * * ?")
    public void work() {
        logger.info("开始执行三天未登录信息通知任务");
        notifyPreviousLogin();
        logger.info("三天未登录信息通知任务结束");
    }

    private void notifyPreviousLogin() {
        List<ImprovementPlan> improvementPlanList = planService.loadPreviouslyLogin();
        improvementPlanList.stream().forEach(improvementPlan -> {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(improvementPlan.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.getPreviousLoginMsg());

            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);

            data.put("first", new TemplateMessage.Keyword("第一个文案\n"));
            data.put("keyword1", new TemplateMessage.Keyword(improvementPlan.getProblemName()));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
            data.put("remark", new TemplateMessage.Keyword("\n最后一个文案↓↓↓"));

            templateMessageService.sendMessage(templateMessage);

        });
    }


}
