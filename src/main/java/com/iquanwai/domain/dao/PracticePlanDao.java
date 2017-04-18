package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class PracticePlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void unlockApplicationPractice(Integer planId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update PracticePlan set UnLocked=1 where PlanId=? and (Type=11 or Type=12)";
        try {
            runner.update(sql, planId);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
