package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.ClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文
 */
@Repository
public class ClassMemberDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public int changeActiveStatus(int id, boolean activeStatus) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE ClassMember SET Active = ? WHERE Id = ?";
        try {
            return runner.update(sql, activeStatus, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<ClassMember> loadActiveByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Active = 1 AND Del = 0";
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public ClassMember loadLatestByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ClassMember WHERE ProfileId = ? AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
