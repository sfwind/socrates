package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.UserRole;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class UserRoleDao  extends DBUtil{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<UserRole> loadValidAssists(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from UserRole where RoleId in (3,4,5,6,11,12,13,14,15) and del = 0";
        ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class);

        try {
            return runner.query(sql,h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public UserRole getAssist(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<UserRole> h = new BeanHandler<>(UserRole.class);
        String sql = "SELECT * FROM UserRole where ProfileId=? and RoleId in (3,4,5,6,11,12,13,14,15) and Del=0";
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
