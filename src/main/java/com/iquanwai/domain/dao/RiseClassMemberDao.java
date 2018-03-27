package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.RiseClassMember;
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
public class RiseClassMemberDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseClassMember riseClassMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseClassMember (ClassName, GroupId, MemberId, ProfileId, Year, Month, Active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseClassMember.getClassName(),
                    riseClassMember.getGroupId(),
                    riseClassMember.getMemberId(),
                    riseClassMember.getProfileId(),
                    riseClassMember.getYear(),
                    riseClassMember.getMonth(),
                    riseClassMember.getActive());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int update(RiseClassMember riseClassMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET ClassName = ?, GroupId = ?, Active = ? WHERE Id = ?";
        try {
            return runner.update(sql,
                    riseClassMember.getClassName(),
                    riseClassMember.getGroupId(),
                    riseClassMember.getActive(),
                    riseClassMember.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseClassMember loadLatestRiseClassMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseClassMember> queryByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public RiseClassMember queryByMemberId(String memberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE MemberId = ? AND Active = 1 AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, memberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public RiseClassMember queryValidClassMemberByMemberId(String memberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE MemberId = ? AND Del = 0 LIMIT 1";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, memberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 单笔查询
     *
     * @param profileId 用户 id
     * @param year      年份
     * @param month     月份
     */
    public RiseClassMember queryByProfileIdAndTime(Integer profileId, Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 获取对应年月下的所有有效数据
     */
    public List<RiseClassMember> loadAllByYearMonth(Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseClassMember> loadActiveRiseClassMembers() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Active = 1 AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public RiseClassMember loadActiveRiseClassMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Active = 1 AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public int del(Integer riseClassMemberId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET Del = 1 WHERE Id = ?";
        try {
            return runner.update(sql, riseClassMemberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int batchUpdateGroupId(List<Integer> ids, String groupId) {
        if (ids.size() == 0) {
            return -1;
        }

        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET GroupId = ? WHERE Id IN (" + produceQuestionMark(ids.size()) + ") AND Del = 0";
        List<Object> objects = Lists.newArrayList();
        objects.add(groupId);
        objects.addAll(ids);
        try {
            return runner.update(sql, objects.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 将对应年月下的有效人员的 Active 字段置为 1
     */
    public int batchUpdateActive(Integer year, Integer month, Integer active) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseClassMember SET Active = ? WHERE Year = ? AND Month = ? AND Del = 0";
        try {
            return runner.update(sql, active, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<RiseClassMember> batchQueryByProfileIds(List<Integer> profileIds) {
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId in (" + produceQuestionMark(profileIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseClassMember> loadActiveByMemberIds(List<String> memberIds) {
        if (memberIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Active = 1 AND Del = 0 AND MemberId In (" + produceQuestionMark(memberIds.size()) + ")";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, memberIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseClassMember> loadByClassName(String className) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ClassName = ? AND (GroupId IS NOT NULL AND GroupId != '') AND Del = 0 ORDER BY ClassName, GroupId";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, className);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public RiseClassMember loadPurchaseRiseClassMember(Integer profileId, Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND YEAR = ? AND Month = ? AND Del = 0";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 获得所有的班级和小组
     */
    public List<RiseClassMember> loadAllClassNameAndGroup() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " select ClassName,GroupId from RiseClassMember where Active = 1 and Del = 0 group by ClassName,GroupId order by ClassName";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);

        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 根据班级和小组获得用户
     */
    /**
     * 根据班级和小组获得用户
     */
    public List<RiseClassMember> getRiseClassMemberByClassNameGroupId(String className, String groupId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseClassMember where ClassName = ? and GroupId = ? and Active = 1 and Del = 0";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);

        try {
            return runner.query(sql, h, className, groupId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer getCountByClassNameGroupId(String className, String groupId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT COUNT(*) FROM RiseClassMember WHERE CLASSNAME = ? AND GROUPID = ? AND DEL = 0 ";
        try {
            return runner.query(sql, new ScalarHandler<Long>(), className, groupId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


    public Integer getCountByClass(String className) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT COUNT(*) FROM RiseClassMember WHERE CLASSNAME=? AND DEL = 0";

        try {
           return runner.query(sql, new ScalarHandler<Long>(), className).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据 MemberId 查询 RiseClassMember 记录，只是为了发证书
     *
     * @param memberIds 学号集合
     */
    public List<RiseClassMember> queryForCertificateMemberIds(List<String> memberIds) {
        if (memberIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE Del = 0 AND MemberId IN (" + produceQuestionMark(memberIds.size()) + ") GROUP BY ProfileId ORDER BY Id DESC";
        ResultSetHandler<List<RiseClassMember>> h = new BeanListHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, memberIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
