package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.ApplicationSubmit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class ApplicationSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 查询用户提交记录
     * @param applicationId 应用练习id
     * @param planId 计划id
     */
    public ApplicationSubmit load(Integer applicationId, Integer planId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ProfileId=? and ApplicationId=? and PlanId=? and Del=0";
        try {
            return run.query(sql, h, profileId, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public ApplicationSubmit load(Integer applicationId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ProfileId=? and ApplicationId=? and Del=0";
        try {
            return run.query(sql, h, profileId, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean firstAnswer(Integer id, String content, int length) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, PublishTime = CURRENT_TIMESTAMP, LastModifiedTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean answer(Integer id, String content, int length) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, LastModifiedTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updatePointStatus(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set PointStatus=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void asstFeedback(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Feedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> load(Integer applicationId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ApplicationId=? and Length>=15 and Del=0";
        try {
            List<ApplicationSubmit> submits = run.query(sql, h, applicationId);
            return submits;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void requestComment(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set RequestFeedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateContent(Integer id, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> loadBatchApplicationSubmits(Integer problemId, List<Integer> refers) {
        if (CollectionUtils.isEmpty(refers)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(refers.size());
        List<Object> params = Lists.newArrayList();
        params.add(problemId);
        params.addAll(refers);
        String sql = "select * from ApplicationSubmit where  ProblemId = ? and Id in (" + mask + ") and Del=0";

        try {
            return runner.query(sql, new BeanListHandler<>(ApplicationSubmit.class), params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> loadApplicationSubmitsByApplicationIds(List<Integer> applicationIds, Integer planId) {
        if (applicationIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ApplicationSubmit WHERE ApplicationId in (" + produceQuestionMark(applicationIds.size())
                + ") AND PlanId = ? AND Del = 0";
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        List<Object> objects = Lists.newArrayList();
        objects.addAll(applicationIds);
        objects.add(planId);
        try {
            return runner.query(sql, h, objects.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

}
