package com.iquanwai.domain.po;

import lombok.Data;

@Data
public class UserRole {
    private Integer id;
    private Integer roleId;
    private Integer profileId;
    private Boolean del;
}
