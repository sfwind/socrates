package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.UserInfo;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class UserInfoDao extends DBUtil{

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public UserInfo loadByProfileId(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM UserInfo Where ProfileId = ? AND DEL = 0";
        ResultSetHandler<UserInfo> h = new BeanHandler<UserInfo>(UserInfo.class);

        try {
           return runner.query(sql,h,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }



}
