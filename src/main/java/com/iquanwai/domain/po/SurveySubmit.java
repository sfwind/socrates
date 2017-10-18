package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/17.
 */
@Data
public class SurveySubmit {
    // 问卷id+用户id+提交id, 用户可以做多个问卷，但是同时只能做一个
    private Integer id; // 问卷提交表的id
    private Integer activity; // 问卷星的id
    private String openId; // 用户openId
    private Integer sequence; // 提交问卷的顺序id
    private Integer status; // 是否已提交，1：已提交，0：未提交
    private Integer timeTaken; // 填写问卷花费的时间
    private Date submitTime; //
    private Integer totalValue; // 总分
}
