package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.SurveySubmit;
import com.iquanwai.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
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
 * Created by nethunder on 2017/1/17.
 */
@Repository
public class SurveySubmitDao extends DBUtil {
    public Logger logger = LoggerFactory.getLogger(this.getClass());


    public int insert(String openId, Integer activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SurveySubmit(Activity, OpenId) " +
                "values(?, ?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    activity, openId);
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Boolean submit(SurveySubmit surveySubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SurveySubmit SET Sequence = ?,Status = 1,TimeTaken = ?,SubmitTime = ?,TotalValue= ? where Id = ?";
        try {
            runner.update(sql, surveySubmit.getSequence(), surveySubmit.getTimeTaken(), surveySubmit.getSubmitTime(), surveySubmit.getTotalValue(), surveySubmit.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public Integer submitCount(Integer activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT COUNT(*) FROM SurveySubmit WHERE Activity = ? AND Del = 0";
        try {
            return runner.query(sql, new ScalarHandler<Long>(), activity).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public SurveySubmit loadSubmit(Integer activity, String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM SurveySubmit WHERE Status = 1 and Del = 0 and Activity = ? OpenId = ? order by Id desc limit 1";
        try {
            return runner.query(sql, new BeanHandler<SurveySubmit>(SurveySubmit.class), activity, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<SurveySubmit> loadSubmitGroup(Integer activity, Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from SurveySubmit where Activity = ? AND Status = 1 AND Del = 0 AND DATE(SubmitTime) = ?";
        try {
            return runner.query(sql, new BeanListHandler<SurveySubmit>(SurveySubmit.class), activity, DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();

    }

}
