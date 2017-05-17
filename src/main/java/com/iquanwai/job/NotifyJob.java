package com.iquanwai.job;

import com.google.common.collect.Maps;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.dao.RedisUtil;
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

import javax.annotation.PostConstruct;
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
    @Autowired
    private RedisUtil redisUtil;
    @Scheduled(cron = "0 20 10 * * ?")
    public void work() {
        logger.info("NotifyJob start");
        //发送点赞数统计
        notifyUser();
        logger.info("NotifyJob end");
    }


    @PostConstruct()
    public void init(){
        // 测试上线zk是否成功
        logger.info("appid------:{}",ConfigUtils.getAppid());
        logger.info("act------:{}",redisUtil.get("accessToken"));
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

            data.put("first",new TemplateMessage.Keyword("这个小课还有3天就到期了：\n"));
            data.put("keyword1",new TemplateMessage.Keyword(problem.getProblem()));
            data.put("keyword2",new TemplateMessage.Keyword(DateUtils.parseDateToString(improvementPlan.getCloseDate())));
            data.put("remark",new TemplateMessage.Keyword("\n至少做完所有知识理解和巩固练习，才能完成小课，加油加油\n\n" +
                    "点击下方RISE按钮，快去完成练习吧↓↓↓"));

            templateMessageService.sendMessage(templateMessage);
        });
    }
}
