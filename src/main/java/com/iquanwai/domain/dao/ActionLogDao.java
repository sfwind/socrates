package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class ActionLogDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Integer> loadThatDayLoginUser(Integer days){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select DISTINCT ProfileId from ActionLog where DATE(AddTime) = DATE_SUB(CURDATE(),INTERVAL ? DAY) ";

        try{
            ResultSetHandler<List<Integer>> handler = new ColumnListHandler<>();
            return runner.query(sql, handler, days);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
