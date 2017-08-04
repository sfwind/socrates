package com.iquanwai.job;

import com.google.common.collect.Maps;
import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.ProblemDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Problem;
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
 * Created by xfduan on 2017/8/1.
 */
@Component
public class NotifyNewUnlogin {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    private static final String INDEX_URL = "/rise/static/plan/main";

    @Scheduled(cron = "0 40 17 * * ?")
    public void work() {
        logger.info("提醒一天未登录的新用户学习开始");
        notifyNewUnLogin();
        logger.info("提醒一天未登录的新用户学习结束");
    }

    private void notifyNewUnLogin() {
        List<Profile> newUnLoginProfiles = planService.loadNewUnLogin();
        newUnLoginProfiles.clear();
        newUnLoginProfiles.add(profileDao.load(Profile.class, 30));
        logger.info("清除之后，{}", newUnLoginProfiles.size());
        for (Profile profile : newUnLoginProfiles) {
            Integer profileId = profile.getId();
            List<ImprovementPlan> improvementPlans = improvementPlanDao.loadImprovementPlansByProfileId(profileId);
            sendMessage(profile, improvementPlans);
        }
    }

    private void sendMessage(Profile profile, List<ImprovementPlan> improvementPlans) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(profile.getOpenid());
        templateMessage.setTemplate_id(ConfigUtils.getLearningNotifyMsg());

        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(ConfigUtils.getAppDomain() + INDEX_URL);

        data.put("first", new TemplateMessage.Keyword(profile.getNickname() + "童鞋，晚上好！\n" +
                "趁今天还没结束，快点击下方“上课啦”，来一节能力提升练习吧！\n"));

        if (improvementPlans.size() == 0) {
            data.put("keyword1", new TemplateMessage.Keyword("还未选课哦"));
            data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        } else {
            ImprovementPlan improvementPlan = improvementPlans.stream().findAny().get();
            Problem problem = problemDao.load(Problem.class, improvementPlan.getProblemId());
            if (problem != null) {
                data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
            }
            int closeTime = DateUtils.interval(new Date(), improvementPlan.getCloseDate()) + 1;
            data.put("keyword2", new TemplateMessage.Keyword("距到期还有" + closeTime + "天"));
        }

        data.put("remark", new TemplateMessage.Keyword("\n想念刷题的爽快感受？点击“详情”，立刻开始提升自己！"));
        templateMessageService.sendMessage(templateMessage);
    }


}
