package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.BusinessSchoolApplication;
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
 * Created by nethunder on 2017/9/27.
 */
@Repository
public class BusinessSchoolApplicationDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public BusinessSchoolApplication loadByOpenId(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE Openid = ? AND Del = 0";
        try {
            return runner.query(sql, new BeanHandler<BusinessSchoolApplication>(BusinessSchoolApplication.class), openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public BusinessSchoolApplication loadBySubmitId(Integer submitId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Select * from BusinessSchoolApplication where SubmitId = ?";
        try {
            return runner.query(sql, new BeanHandler<BusinessSchoolApplication>(BusinessSchoolApplication.class), submitId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }


    public List<BusinessSchoolApplication> getUserApplications(Integer profileId, Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Select * from BusinessSchoolApplication where ProfileId = ? and AddTime >= ?";
        try {
            return runner.query(sql, new BeanListHandler<BusinessSchoolApplication>(BusinessSchoolApplication.class), profileId, DateUtils.beforeDays(date, 30));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer insert(BusinessSchoolApplication businessSchoolApplication) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO BusinessSchoolApplication(SubmitId, ProfileId, Openid, Status, CheckTime, IsDuplicate, Deal, OriginMemberType,SubmitTime) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), businessSchoolApplication.getSubmitId(), businessSchoolApplication.getProfileId(), businessSchoolApplication.getOpenid(),
                    businessSchoolApplication.getStatus(), businessSchoolApplication.getCheckTime(), businessSchoolApplication.getIsDuplicate(),
                    businessSchoolApplication.getDeal(), businessSchoolApplication.getOriginMemberType(), businessSchoolApplication.getSubmitTime()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
