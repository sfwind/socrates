package com.iquanwai.domain;

import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.PracticePlanDao;
import com.iquanwai.domain.dao.ProblemPlanDao;
import com.iquanwai.domain.po.ImprovementPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        //解锁所有应用训练
        practicePlanDao.unlockApplicationPractice(planId);
        //更新待完成的专题状态
        problemPlanDao.updateStatus(plan.getOpenid(), plan.getProblemId(), 2);
    }

    public void updateKey(Integer planId, Integer key) {
        improvementPlanDao.updateKey(planId, key);
    }

}
