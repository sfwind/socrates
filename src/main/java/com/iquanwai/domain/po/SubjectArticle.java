package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/3/8.
 */
@Data
public class SubjectArticle {
    private Integer id;
    private String openid; //提交用户openid
    private Integer problemId; //难题id
    private Integer authorType; //发布者类型
    private Integer sequence; // 排序字段,非0时是精彩评论
    private String title; // 标题
    private String content; //提交内容
    private Integer pointStatus; //是否已加分（0-否，1-是）
    private Date updateTime; //最后更新时间

}
