package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.AnswerApproval;
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
 * Created by justin on 17/6/19.
 */
@Repository
public class AnswerApprovalDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<AnswerApproval> getLastPeriodApprovals(Date start, Date end) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<AnswerApproval>> h = new BeanListHandler<>(AnswerApproval.class);
        String sql = "SELECT * FROM AnswerApproval where AddTime>=? and AddTime<? and Del=0";

        try {
            return run.query(sql, h, start, end);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
