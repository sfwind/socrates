package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/4.
 */
@Service
public class PlanService {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private RiseUserLoginDao riseUserLoginDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private RedisUtil redisUtil;

    private static final String LOGIN_REDIS_KEY = "login:";

    //提前3天通知用户,小课即将关闭
    private static final int NOTIFY_CLOSE_DAYS = 3;
    //3天未登录
    private static final int NOLOGIN_DAYS = 3;

    private Logger logger = LoggerFactory.getLogger(getClass());


    public List<ImprovementPlan> loadAllRunningPlan() {
        return improvementPlanDao.loadAllRunningPlan();
    }

    public void completePlan(Integer planId, Integer status) {
        //训练计划结束
        logger.info("{} is terminated", planId);
        //更新训练计划状态
        improvementPlanDao.updateStatus(planId, status);
        if (status == ImprovementPlan.CLOSE) {
            // 更新关闭时间
            improvementPlanDao.updateCloseTime(planId);
        }
    }


    public List<ImprovementPlan> loadUnderClosePlan() {
        List<ImprovementPlan> improvementPlans = loadAllRunningPlan();
        Date date = new Date();
        //获取3天后的日期
        Date closeDate = DateUtils.afterDays(DateUtils.startOfDay(date), NOTIFY_CLOSE_DAYS);

        return improvementPlans.stream().filter(improvementPlan -> improvementPlan.getCloseDate().equals(closeDate))
                .collect(Collectors.toList());
    }

    public Problem getProblem(Integer problemId) {
        return problemDao.load(Problem.class, problemId);
    }

    /**
     * 获取已经三天内没有登陆过 RISE 且中间没有提醒过的用户的 OpenId 和最后一次学习的 Problem 名称
     */
    public List<ImprovementPlan> loadPreviouslyLogin() {
        List<ImprovementPlan> improvementPlanList = Lists.newArrayList();

        // 一次性获取所有的 Problem 信息
        List<Problem> problemList = problemDao.loadAll(Problem.class);
        Map<Integer, String> problemMap = new HashMap<>();
        problemList.stream().forEach(problem -> problemMap.put(problem.getId(), problem.getProblem()));

        // 获取三天前学员 OpenId 和最近一次的 LoginDate
        String previousLoginDate = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), NOLOGIN_DAYS));
        List<RiseUserLogin> riseUserLoginList = riseUserLoginDao.loadUnLoginUser(previousLoginDate);

        List<RiseMember> riseMemberList = riseMemberDao.validRiseMember();
        List<String> riseMemberOpenids = riseMemberList.stream().map(RiseMember::getOpenId).collect(Collectors.toList());

        //只提醒会员用户
        if (riseUserLoginList != null) {
            improvementPlanList = riseUserLoginList.stream().filter(riseUserLogin ->
                    riseMemberOpenids.contains(riseUserLogin.getOpenid()))
                    .map(riseUserLogin -> {
                        ImprovementPlan improvementPlan = improvementPlanDao.loadLatestProblemByOpenId(riseUserLogin.getOpenid());
                        improvementPlan.setProblemName(problemMap.get(improvementPlan.getProblemId()));
                        return improvementPlan;
                    }).collect(Collectors.toList());
        }
        return improvementPlanList;
    }

    public List<ImprovementPlan> getLearningPlan(Integer problemId) {
        List<ImprovementPlan> improvementPlans = Lists.newArrayList();
        List<String> openids = Lists.newArrayList();
        improvementPlanDao.loadByProblemId(problemId).forEach(improvementPlan -> {
            Integer profileId = improvementPlan.getProfileId();
            Profile profile = profileDao.load(Profile.class, profileId);
            //过滤没报名的用户
            if (profile != null && profile.getRiseMember()) {
                //用户去重
                if (!openids.contains(profile.getOpenid())) {
                    improvementPlans.add(improvementPlan);
                    openids.add(profile.getOpenid());
                }
            }
        });

        return improvementPlans;
    }

    /**
     * 获取进行中的限免用户小课
     */
    public List<ImprovementPlan> loadFreeInactiveUserPlan() {
        List<ImprovementPlan> improvementPlanList = improvementPlanDao.loadRunningPlanByProblemId(
                ConfigUtils.getFreeProblem());
        return improvementPlanList.stream().filter(improvementPlan -> {
            //非限免用户不通知
            if (improvementPlan.getRiseMember()) {
                return false;
            }
            // 第一天学习不通知
//            Date startDate = DateUtils.startOfDay(new Date());
//            if (startDate.equals(improvementPlan.getStartDate())) {
//                return false;
//            }
            // 3天后即将关闭不提醒,有额外提醒消息
            Date closeDate = DateUtils.afterDays(DateUtils.startOfDay(new Date()), 3);
            if (closeDate.equals(improvementPlan.getCloseDate())) {
                return false;
            }

            int planId = improvementPlan.getId();
            List<PracticePlan> practicePlanList = practicePlanDao.loadPracticePlan(planId);

            for (PracticePlan practicePlan : practicePlanList) {
                // 判断今天是否完成过练习 已做过练习的不提醒
                if (practicePlan.getStatus() == 1) {
                    if (DateUtils.isToday(practicePlan.getUpdateTime())) {
                        return false;
                    }
                }
            }
            return true;
        }).collect(Collectors.toList());

    }

    /**
     * 昨日新关注人员，但是今日未登录
     */
    public List<Profile> loadNewUnLogin() {
        Date beforeDate = DateUtils.beforeDays(new Date(), 1);
        String todayDateString = DateUtils.parseDateToString(new Date());
        List<Profile> profiles = profileDao.loadProfiles(beforeDate, new Date());

        profiles = profiles.stream().filter(Profile::getRiseMember).collect(Collectors.toList());
        List<Profile> unLoginProfiles = Lists.newArrayList();
        for (Profile profile : profiles) {
            Integer profileId = profile.getId();
            String lastLoginTime = redisUtil.get(LOGIN_REDIS_KEY + profileId.toString());
            if (lastLoginTime != null && lastLoginTime.length() >= 10
                    && !lastLoginTime.substring(0, 10).equalsIgnoreCase(todayDateString)) {
                // 今日未登录
                unLoginProfiles.add(profile);
            }
        }
        return unLoginProfiles;
    }

}
