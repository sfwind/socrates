package com.iquanwai.job;

import com.google.common.collect.Maps;
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

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/3/21.
 */
@Component
public class NotifyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private TemplateMessageService templateMessageService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void work() {
        logger.info("NotifyJob start");
        //发送点赞数统计
        notifyUser();
        logger.info("NotifyJob end");
    }


    private void notifyUser() {
        List<ImprovementPlan> underClosedPlans = planService.loadUnderClosePlan();
        underClosedPlans.stream().forEach(improvementPlan -> {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(improvementPlan.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.getUnderCloseMsg());

            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);

            Problem problem = planService.getProblem(improvementPlan.getProblemId());

            data.put("first",new TemplateMessage.Keyword("你的以下专题还有3天就到期了"));
            data.put("keyword1",new TemplateMessage.Keyword(problem.getProblem()));
            data.put("keyword2",new TemplateMessage.Keyword(DateUtils.parseDateToString(improvementPlan.getCloseDate())));
            data.put("remark",new TemplateMessage.Keyword("至少做完所有理解训练和巩固训练，才能完成该专题，加油\n" +
                    "查看RISE进度，点击下方按钮↓↓↓"));

            templateMessageService.sendMessage(templateMessage);
        });
    }
}
