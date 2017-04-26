package com.iquanwai.job;

import com.iquanwai.domain.PlanService;
import com.iquanwai.domain.po.ImprovementPlan;
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
public class DailyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;

    @Scheduled(cron = "0 0 6 * * ?")
    public void work() {
        logger.info("DailyJob start");
        dailyJob();
        logger.info("DailyJob end");
    }

    private void dailyJob() {
        List<ImprovementPlan> improvementPlanList = planService.loadAllRunningPlan();
        improvementPlanList.stream().forEach(improvementPlan -> {
            //过期自动结束训练
            if (improvementPlan.getCloseDate().before(new Date())) {
                planService.completePlan(improvementPlan.getId(), ImprovementPlan.CLOSE);
            } else {
//                Integer key = improvementPlan.getKeycnt();
//                if (new Date().before(improvementPlan.getCloseDate())) {
//                    planService.updateKey(improvementPlan.getId(), key + 1);
//                }
            }
        });


    }

}
