package com.iquanwai.domain.po;

import lombok.Data;

import java.util.Date;

@Data
public class AuditionClassMember {

    private Integer id;
    private Integer profileId;
    private String openId;
    private String className;
    private Date startDate;
    private Boolean active; // 学习记录是否生效
    private Boolean committee; // 是否是学委
    private Boolean winningGroup; // 是否是优胜小组成员
    private Boolean checked; // 是否已经校验完成
    private Boolean del;

}
