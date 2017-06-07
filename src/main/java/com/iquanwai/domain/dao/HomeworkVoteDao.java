package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.HomeworkVote;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
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
 * Created by nethunder on 2017/1/2.
 */
@Repository
public class HomeworkVoteDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<HomeworkVote> loadVoteByDate(Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkVote>> h = new BeanListHandler<>(HomeworkVote.class);
        String sql = "select * from HomeworkVote where AddTime>=? and Del=0";
        try {
            return runner.query(sql, h, date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
