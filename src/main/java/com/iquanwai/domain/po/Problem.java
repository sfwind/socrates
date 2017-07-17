package com.iquanwai.domain.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Problem {
    private int id;
    private String problem; // 工作生活中遇到的问题
    private String pic; //头图
    private Integer length; //训练天数
    private Integer warmupCount; //巩固练习次数
    private Integer applicationCount; //应用练习次数
    private Integer challengeCount; //小目标次数
    private String description; //富文本描述
    private Integer catalogId; // 分类
    private String subjectDesc; //小课论坛介绍
    private String descPic; // 描述图片
    private String audio; //语音
    private Boolean del; //是否删除(0-否,1-是)

    private Boolean done; // 非db字段 用户是否做过
    private Integer status; // 非db字段 用户选过小课（0-未选过,1-正在进行,2-已完成）
    private String activityUrl; //非db字段 吊打活动页面
    private String password; //非db字段 吊打密码
}
