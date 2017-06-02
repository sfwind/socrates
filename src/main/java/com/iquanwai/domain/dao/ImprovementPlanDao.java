package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ImprovementPlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<ImprovementPlan> loadAllRunningPlan() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ImprovementPlan WHERE Status in (1,2)";
        ResultSetHandler<List<ImprovementPlan>> h = new BeanListHandler(ImprovementPlan.class);
        try {
            List<ImprovementPlan> improvementPlans = runner.query(sql, h);
            return improvementPlans;
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void updateStatus(Integer planId, Integer status) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET Status =? where Id=?";
        try {
            runner.update(sql, status, planId);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateKey(Integer planId, Integer key) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ImprovementPlan SET Keycnt =? where Id=?";
        try {
            runner.update(sql, key, planId);
        } catch(SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public ImprovementPlan loadLatestProblemByOpenId(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ImprovementPlan where Openid = ? ORDER BY Id DESC";
        ResultSetHandler<ImprovementPlan> h = new BeanHandler<>(ImprovementPlan.class);
        try {
            ImprovementPlan improvementPlan = runner.query(sql, h, openId);
            return improvementPlan;
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
