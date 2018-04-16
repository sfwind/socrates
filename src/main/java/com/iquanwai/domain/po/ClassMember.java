package com.iquanwai.domain.po;

import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class ClassMember {

    private Integer id;
    private Integer profileId;
    // 班级号
    private String className;
    // 小组号
    private String groupId;
    // 身份类型
    private Integer memberTypeId;
    // 是否生效
    private Boolean active;
    // 是否删除
    private Boolean del;

}
