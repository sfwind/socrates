package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.UserInfo;
import org.apache.commons.collections.CollectionUtils;
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

    public List<UserInfo> loadByProfileIds(List<Integer> profileIds){
        if(CollectionUtils.isEmpty(profileIds)){
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String questionMarks = produceQuestionMark(profileIds.size());
        String sql = "SELECT * FROM UserInfo WHERE ProfileId in (" + questionMarks + ") AND Del = 0";
        ResultSetHandler<List<UserInfo>> h = new BeanListHandler<UserInfo>(UserInfo.class);

        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }



}
