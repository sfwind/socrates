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
    private Boolean active;
    private Integer identity;
    private Boolean checked;
    private Boolean del;

    public interface Identity {
        int NORMAL = 0;
    }

}
