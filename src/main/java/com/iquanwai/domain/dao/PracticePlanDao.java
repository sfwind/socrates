package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.PracticePlan;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class PracticePlanDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<PracticePlan> loadPracticePlan(Integer planId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<PracticePlan>> h = new BeanListHandler<>(PracticePlan.class);
        String sql = "SELECT * FROM PracticePlan where PlanId=?";
        try {
            List<PracticePlan> practicePlans = run.query(sql, h,
                    planId);
            return practicePlans;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
