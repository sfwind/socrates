package com.iquanwai.domain.po;

import lombok.Data;

@Data
public class RiseClassMember {

    private Integer id;
    /**
     * 仅用来做每月所有人报名前缀，无任何实际业务含义
     */
    @Deprecated
    private String classId; // classId 2017 年份
    private String className; // 班级名称
    private String groupId; // 班级 01
    private String memberId;
    private Integer profileId; // 用户 id
    private Integer year;
    private Integer month; // 数据月份
    private Integer active; // 是否参与本次学习
    private Integer del; // 数据是否删除，0-未删除 1-已删除

    /**
     * 商学院会员
     */
    public static final int BUSINESS_MEMBERSHIP = 1;
    /**
     * 专项课
     */
    public static final int MONTHLY_CAMP = 2;

}
