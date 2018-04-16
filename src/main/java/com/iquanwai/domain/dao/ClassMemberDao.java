package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

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

}
