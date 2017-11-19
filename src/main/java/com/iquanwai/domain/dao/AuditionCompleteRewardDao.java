package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.AuditionCompleteReward;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class AuditionCompleteRewardDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int insert(AuditionCompleteReward auditionCompleteReward) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO AuditionCompleteReward (ProfileId, ClassName, Amount) VALUE (?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    auditionCompleteReward.getProfileId(),
                    auditionCompleteReward.getClassName(),
                    auditionCompleteReward.getAmount());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
