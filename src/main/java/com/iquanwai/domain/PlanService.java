package com.iquanwai.domain;

import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.PracticePlanDao;
import com.iquanwai.domain.dao.ProblemDao;
import com.iquanwai.domain.dao.ProblemPlanDao;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Problem;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/4.
 */
@Service
public class PlanService {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemPlanDao problemPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ProblemDao problemDao;

    //提前3天通知用户,小课即将关闭
    private static final int NOTIFY_CLOSE_DAYS = 3;

    private Logger logger = LoggerFactory.getLogger(getClass());


    public List<ImprovementPlan> loadAllRunningPlan() {
        return improvementPlanDao.loadAllRunningPlan();
    }

    public void completePlan(Integer planId, Integer status) {
        //训练计划结束
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        logger.info("{} is terminated", planId);
        //更新训练计划状态
        improvementPlanDao.updateStatus(planId, status);
        //解锁所有应用练习
//        practicePlanDao.unlockApplicationPractice(planId);
        //更新待完成的小课状态
        problemPlanDao.updateStatus(plan.getOpenid(), plan.getProblemId(), 2);
    }

    public void updateKey(Integer planId, Integer key) {
        improvementPlanDao.updateKey(planId, key);
    }


    public List<ImprovementPlan> loadUnderClosePlan(){
        List<ImprovementPlan> improvementPlans = loadAllRunningPlan();
        Date date = new Date();
        //获取3天后的日期
        Date closeDate = DateUtils.afterDays(DateUtils.startOfDay(date), NOTIFY_CLOSE_DAYS);

        return improvementPlans.stream().filter(improvementPlan -> improvementPlan.getCloseDate().equals(closeDate))
                .collect(Collectors.toList());
    }

    public Problem getProblem(Integer problemId){
        return problemDao.load(Problem.class, problemId);
    }
}
