package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/4/13.
 */
@Data
public class RiseMember {
    private Integer id;
    private String orderId;
    private String openId;
    private Integer profileId;
    private Integer memberTypeId;
    private Date expireDate;
    private Boolean expired;
    private Integer del;
    private Date addTime;

    private String startTime; // 非DB字段，addTime
    private String endTime; // 非DB字段，expireDate

    /**
     * 专业版半年
     */
    public static final int HALF = 1;
    /**
     * 专业版一年
     */
    public static final int ANNUAL = 2;
    /**
     * 精英版一年
     */
    public static final int ELITE = 3;
    /**
     * 精英版半年
     */
    public static final int HALF_ELITE = 4;
    /**
     * 训练营小课
     */
    public static final int CAMP = 5;
}
