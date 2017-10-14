package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/14.
 */
@Data
public class Coupon {
    private int id;
    private String openid;
    private Integer profileId;
    private Double amount;
    private Integer used; //是否使用（0-否，1-是，2-正在使用）
    private Double cost; //本次订单已消耗的金额，当orderId不为空时有值
    private String orderId; //用于订单的id，当Used=2时有值
    private Date expiredDate; //过期日期
    private String category; //分类
    private String description; // 描述信息


    private String expired; // 过期时间 非DB字段
    //已使用
    public static final int USED = 1;
    //未使用
    public static final int UNUSED = 0;
    //正在使用
    public static final int USING = 2;


}
