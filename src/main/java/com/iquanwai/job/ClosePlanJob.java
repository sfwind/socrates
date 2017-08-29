package com.iquanwai.job;

import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
@Component
public class ClosePlanJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void work() {
        logger.info("关闭小课任务开始");
        closePlan();
        logger.info("关闭小课任务结束");
    }

    private void closePlan() {
        List<ImprovementPlan> improvementPlanList = planService.loadAllRunningPlan();
        improvementPlanList.stream().forEach(improvementPlan -> {
            //过期自动结束训练
            if (DateUtils.afterDays(improvementPlan.getCloseDate(), 1).before(new Date())) {
                Integer status = ImprovementPlan.CLOSE;
                Integer freeProblemId = ConfigUtils.getFreeProblem();
                //限免试用
                if (improvementPlan.getProblemId().equals(freeProblemId)) {
                    if (!improvementPlan.getRiseMember()) {
                        status = ImprovementPlan.CLOSE_FREE;
                    }
                }
                planService.completePlan(improvementPlan.getId(), status);
            }
        });


    }

}
