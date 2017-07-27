package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class PracticePlan {
    private int id;
    private Integer planId; //训练id
    private Integer type; //题目类型（1-巩固练习，2-巩固练习，11-应用练习，12-综合练习，21-小目标，31-知识理解，32-知识回顾）
    private String practiceId; //练习id,多个时用逗号隔开
    private Boolean unlocked; // 是否解锁
    private Integer series; // 节号
    private Integer sequence; //节内顺序
    private Integer knowledgeId; //知识点id
    private Integer status; //题目状态（0-未完成，1-已完成）

    private Date updateTime;
}
