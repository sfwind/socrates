package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private CustomerService customerService;
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

    /**
     * 修改plan的状态
     * @param planId id
     * @param status 状态
     */
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


    /**
     * 获取将要关闭的订单
     * @return 订单
     */
    public List<ImprovementPlan> loadUnderClosePlan() {
        List<ImprovementPlan> improvementPlans = loadAllRunningPlan();
        Date date = new Date();
        //获取3天后的日期
        Date closeDate = DateUtils.afterDays(DateUtils.startOfDay(date), NOTIFY_CLOSE_DAYS);

        return improvementPlans.stream().filter(improvementPlan -> improvementPlan.getCloseDate().equals(closeDate))
                .collect(Collectors.toList());
    }

    /**
     * 查询Problem
     * @param problemId id
     * @return Problem
     */
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
        Map<Integer, String> problemMap = Maps.newHashMap();
        problemList.forEach(problem -> problemMap.put(problem.getId(), problem.getProblem()));

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
            Profile profile = customerService.getProfile(profileId);
            //过滤没报名的用户
            if (profile != null && (profile.getRiseMember() != 0)) {
                //用户去重
                if (!openids.contains(profile.getOpenid())) {
                    improvementPlans.add(improvementPlan);
                    openids.add(profile.getOpenid());
                }
            }
        });

        return improvementPlans;
    }

    public List<ImprovementPlan> loadRunningUnlogin() {
        String todayDateString = DateUtils.parseDateToString(new Date());
        List<ImprovementPlan> runningPlans = loadAllRunningPlan();
        List<Problem> problemList = problemDao.loadAll(Problem.class);
        Map<Integer, Problem> problemMap = Maps.newHashMap();
        problemList.forEach(problem -> problemMap.put(problem.getId(), problem));
        runningPlans = runningPlans.stream().filter(plan -> {
            Integer profileId = plan.getProfileId();
            String lastLoginTime = redisUtil.get(LOGIN_REDIS_KEY + profileId.toString());
            // redis里没有 或者登录时间不是今天，都要提醒
            return lastLoginTime == null ||
                    (lastLoginTime.length() >= 10 &&
                            !lastLoginTime.substring(0, 10).equalsIgnoreCase(todayDateString));
        }).collect(Collectors.toList());
        Map<Integer, ImprovementPlan> planMap = Maps.newHashMap();
        runningPlans.forEach(plan -> {
            if (planMap.containsKey(plan.getProfileId())) {
                ImprovementPlan oldPlan = planMap.get(plan.getProfileId());
                if (plan.getCloseDate().before(oldPlan.getCloseDate())) {
                    // 新的plan比老的plan更早关闭
                    planMap.put(plan.getProfileId(), plan);
                }
            } else {
                planMap.put(plan.getProfileId(), plan);
            }
            Problem problem = problemMap.get(plan.getProblemId());
            plan.setProblemName(problem != null ? problem.getProblem() : null);
        });

        return Lists.newArrayList(planMap.values());
    }

}
