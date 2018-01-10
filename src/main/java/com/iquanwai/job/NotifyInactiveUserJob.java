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
 * Created by xfduan on 2017/6/1.
 */
@Component
public class NotifyInactiveUserJob {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private CustomerService customerService;

    private static final String INDEX_URL = "/rise/static/learn";

    @Scheduled(cron = "0 0 21 ? * THU")
    public void work() {
        logger.info("开始执行三天未登录信息通知任务");
        notifyInactiveUser();
        logger.info("三天未登录信息通知任务结束");
    }

    private void notifyInactiveUser() {
        List<ImprovementPlan> improvementPlanList = planService.loadPreviouslyLogin();
        improvementPlanList.forEach(improvementPlan -> {
            //如果开课时间晚于当前日期,不发消息
            if(improvementPlan.getStartDate().after(new Date())){
                return;
            }
            try{
                Profile profile = customerService.getProfile(improvementPlan.getProfileId());
                if(profile.getLearningNotify()){
                    // 打开了每日提醒，不用发送三日未登录
                    logger.info("用户:{} 打开了每日提醒，不需要发送三日未登录", improvementPlan.getProfileId());
                    return;
                }
                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(improvementPlan.getOpenid());
                templateMessage.setTemplate_id(ConfigUtils.getLearningNotifyMsg());

                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                templateMessage.setUrl(ConfigUtils.getAppDomain()+INDEX_URL);

                data.put("first", new TemplateMessage.Keyword("Hi，圈柚～你已经忽视【圈外同学】超过三天啦！你的课程正在召唤你！\n" +
                        "天朗气清，刷一波圈外课程可好？\n"));
                data.put("keyword1", new TemplateMessage.Keyword(improvementPlan.getProblemName()));
                data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                data.put("remark", new TemplateMessage.Keyword("\n想念刷题的爽快感受？点击“详情”，立刻开始提升自己！"));

                templateMessageService.sendMessage(templateMessage);
            } catch (Exception e){
                logger.error("发送"+improvementPlan.getOpenid()+"失败", e);
            }
        });
    }


}
