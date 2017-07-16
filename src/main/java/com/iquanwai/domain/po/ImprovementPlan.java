package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ImprovementPlan {
    private int id;
    private String openid; //openid
    private Integer problemId; //问题id
    private Date startDate; //开始日期
    private Date endDate; //结束日期(当日开始复习)
    private Date closeDate; //课程关闭时间（课程关闭日期）
    private Integer status; //执行状态（1-正在进行, 2-已结束, 3-已过期）
    private Integer point; //积分
    private Integer warmupComplete; //巩固练习完成数量
    private Integer applicationComplete; //应用练习完成数量
    private Integer total; //任务总数
    private Integer keycnt; //钥匙数量
    private Integer currentSeries; //已解锁的题组
    private Integer totalSeries; //总题组
    private Integer length; //非db字段 总时长
    private Integer deadline; //非db字段 离截止日期天数
    private Boolean summary; //非db字段 是否显示任务总结
    private Integer series; //非db字段 当前组号
    private Boolean openRise; //非db字段 是否打开过rise
    private Boolean doneAllPractice; //非db字段 是否完成当日练习
    private Boolean newMessage; //非db字段 是否有新消息
    private String problemName; // problemId 对应备注中文
    private Integer profileId; // 用户id
    private Boolean riseMember; // 是否付费


    public final static int RUNNING = 1;
    public final static int COMPLETE = 2;
    public final static int CLOSE = 3;
    public final static int CLOSE_FREE = 4;
}
