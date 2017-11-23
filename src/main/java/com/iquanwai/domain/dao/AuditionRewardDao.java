package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.AuditionReward;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class AuditionRewardDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<AuditionReward> loadByAuditionId(Integer auditionId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM AuditionReward WHERE AuditionId = ? AND Del = 0";
        ResultSetHandler<List<AuditionReward>> h = new BeanListHandler<>(AuditionReward.class);
        try {
            return runner.query(sql, h, auditionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<AuditionReward> loadByAuditionIds(List<Integer> auditionIds) {
        if (auditionIds.size() == 0) {
            return Lists.newArrayList();
        }

        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM AuditionReward WHERE AuditionId IN (" + produceQuestionMark(auditionIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<AuditionReward>> h = new BeanListHandler<>(AuditionReward.class);
        try {
            return runner.query(sql, h, auditionIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
