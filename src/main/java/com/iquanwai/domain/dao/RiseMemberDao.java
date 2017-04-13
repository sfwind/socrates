package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public boolean riseMemberExpired(RiseMember riseMember){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update RiseMember set Expired = 1 where Id = ?";

        try{
            runner.update(sql,riseMember.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
            return false;
        }
        return true;
    }
}
