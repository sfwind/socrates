package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.BusinessSchoolApplication;
import com.iquanwai.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/27.
 */
@Repository
public class BusinessSchoolApplicationDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public List<BusinessSchoolApplication> loadCheckApplicationsForNotice(Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessSchoolApplication where Valid = 1 AND DATE(CheckTime) <= ? AND Status in (1,2) AND Deal = 0 AND Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class), DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<BusinessSchoolApplication> loadDealApplicationsForNotice(Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessSchoolApplication where Valid = 1 AND DATE(DealTime) = ? AND Status = 1 AND Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class), DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public Integer updateNoticeAction(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Deal = 1,DealTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public BusinessSchoolApplication loadLastApproveApplication(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE Valid = 1 AND ProfileId = ? AND Del = 0 AND Status = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<BusinessSchoolApplication> loadNoExpiredDealApplication() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from BusinessSchoolApplication where Valid = 1 AND Expired = 0 AND Del = 0 AND Deal = 1";
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer expiredApply(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Expired = 1 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
