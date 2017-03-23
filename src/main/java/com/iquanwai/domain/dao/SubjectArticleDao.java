package com.iquanwai.domain.dao;

import com.iquanwai.domain.po.SubjectArticle;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/3/8.
 */
@Repository
public class SubjectArticleDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(SubjectArticle subjectArticle){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into SubjectArticle(Openid, ProblemId, AuthorType, Sequence,Title, Content) " +
                "values(?,?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    subjectArticle.getOpenid(), subjectArticle.getProblemId(),
                    subjectArticle.getAuthorType(), subjectArticle.getSequence(), subjectArticle.getTitle(),
                    subjectArticle.getContent());
            return insertRs.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public boolean update(SubjectArticle subjectArticle) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update SubjectArticle set Title = ?,Content = ? where Id = ?";
        try{
            runner.update(sql, subjectArticle.getTitle(), subjectArticle.getContent(), subjectArticle.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public int count(Integer problemId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(1) from SubjectArticle where ProblemId = ?";
        try{
            return runner.query(sql, new ScalarHandler<Long>(), problemId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


}
