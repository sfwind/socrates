package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.Profile;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Profile queryByOpenId(String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler(Profile.class);

        try {
            return run.query("SELECT * FROM Profile where Openid=?", h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean submitPersonalCenterProfile(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingLife=?, City=?, Province=? where Openid=?";
        try {
            run.update(updateSql,
                    profile.getIndustry(),
                    profile.getFunction(),
                    profile.getWorkingLife(),
                    profile.getCity(),
                    profile.getProvince(),
                    profile.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


    public void updatePoint(String openId, int point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET Point = ? where Openid = ?";
        try {
            runner.update(sql, point, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void completeProfile(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET IsFull = 1 where Openid = ?";
        try {
            runner.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Boolean submitPersonalProfile(Profile account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set MobileNo=?, Email=?, Industry=?, Function=?, WorkingLife=?, " +
                "RealName=?, City=?, Province=? where Openid=?";
        try {
            run.update(updateSql,
                    account.getMobileNo(), account.getEmail(),
                    account.getIndustry(), account.getFunction(),
                    account.getWorkingLife(), account.getRealName(),
                    account.getCity(), account.getProvince(),
                    account.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public Boolean riseMemberExpired(String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Profile set RiseMember = 0 where OpenId = ?";
        try{
            runner.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


}
