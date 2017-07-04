package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.EventWall;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/7/4.
 */
@Repository
public class EventWallDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<EventWall> loadDiaoDa(Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<EventWall>> h = new BeanListHandler<>(EventWall.class);
        String sql = "select * from EventWall where date(StartTime)=? and ProblemId is not null and Del=0";
        try {
            return runner.query(sql, h, date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
