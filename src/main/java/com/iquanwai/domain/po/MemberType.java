package com.iquanwai.domain.po;

import lombok.Data;

@Data
public class MemberType {
    private Integer id;
    /**
     * 描述
     */
    private String description;

    private Integer del;
}
