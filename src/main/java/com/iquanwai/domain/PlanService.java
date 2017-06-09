package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.ProblemDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.RiseUserLoginDao;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Problem;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.RiseUserLogin;
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
        if(riseUserLoginList != null) {
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

}
