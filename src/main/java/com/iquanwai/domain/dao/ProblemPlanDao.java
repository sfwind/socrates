package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/12/8.
 */
@Repository
public class ProblemPlanDao extends PracticeDBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());


    public void updateStatus(String openid, Integer problemId, Integer status){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ProblemPlan SET STATUS = ? where Openid=? and problemId=?";
        try {
            runner.update(sql, status, openid, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }
}
