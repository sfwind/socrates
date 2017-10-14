package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.dao.CustomerMessageLogDao;
import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.message.CustomerMessageService;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.CustomerMessageLog;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by nethunder on 2017/9/5.
 */
@Component
public class NotifyRunningLogin {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INDEX_URL = "/rise/static/learn";

    @Autowired
    private PlanService planService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CustomerMessageLogDao customerMessageLogDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    // @Scheduled(cron = "0 30 21 ? * MON-FRI")
    // public void notifyHasRunningPlansLogin() {
    //     logger.info("开始未登录提醒job");
    //     List<ImprovementPlan> runningUnlogin = planService.loadRunningUnlogin();
    //     logger.info("待提醒人数：{}", runningUnlogin.size());
    //     runningUnlogin.forEach(this::sendNotifyMsg);
    //     logger.info("未登录提醒job结束");
    // }

    @Scheduled(cron = "*/20 * * * * ?")
    public void testwork() {
        logger.info("test");
        sendNotifyMsg(improvementPlanDao.load(ImprovementPlan.class, 22317));
        logger.info("test end");
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
            String content = "同学，晚上好！快来学习今天的小课，拿下一个职场新技能！\n" +
                    "课程名称：" + plan.getProblemName() + "\n" +
                    "不需要提醒？可以<a href='" + ConfigUtils.getAppDomain() + INDEX_URL + "'>点此</a>，进入“我的”去关闭";
            customerMessageService.sendCustomerMessage(openId, content, Constants.WEIXIN_MESSAGE_TYPE.TEXT);

            // TemplateMessage templateMessage = new TemplateMessage();
            // templateMessage.setTouser(plan.getOpenid());
            // Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            // templateMessage.setData(data);
            // templateMessage.setTemplate_id(ConfigUtils.getLearningNotifyMsg());
            // templateMessage.setUrl(ConfigUtils.getAppDomain() + INDEX_URL);
            // String first = profile.getNickname() + "同学，晚上好！快来学习今天的小课，拿下一个职场新技能！\n";
            // data.put("first", new TemplateMessage.Keyword(first, "#000000"));
            // data.put("keyword1", new TemplateMessage.Keyword(plan.getProblemName(), "#000000"));
            // data.put("keyword2",
            //         new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date()) + "\n\n不需要提醒？可以点此卡片，进入“我的”去关闭",
            //                 "#000000"));
            // data.put("remark", new TemplateMessage.Keyword("\n点此卡片开始学习", "#f57f16"));
            // templateMessageService.sendMessage(templateMessage);
            CustomerMessageLog log = new CustomerMessageLog();
            log.setComment("进行中小课未登录提醒");
            log.setOpenid(profile.getOpenid());
            log.setPublishTime(new Date());
            customerMessageLogDao.insert(log);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
