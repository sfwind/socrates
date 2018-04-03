package com.iquanwai.domain.po;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
public class Role {
    private Integer id;
    private String name;
    private Integer level;

    //常量值需和数据库持久化数据保持一致
    public static final Integer STRANGE = 0; //陌生人
    public static final Integer STUDENT = 1; //普通用户
    public static final Integer EMPLOYEE = 2; //工作人员
    public static final Integer COACH_LEVEL_1 = 3;//见习
    public static final Integer COACH_LEVEL_2 = 4;//I级教练
    public static final Integer COACH_LEVEL_3 = 12;//II级教练
    public static final Integer COACH_LEVEL_4 = 13;//III级教练
    public static final Integer COACH_LEVEL_5 = 5;//高级教练1
    public static final Integer COACH_LEVEL_6 = 14;//高级教练2
    public static final Integer COACH_LEVEL_7 = 15;//高级教练3
    public static final Integer COACH_LEVEL_8 = 6;//首席教练

    public static final Integer BIG_VIP = 7; //大V
    public static final Integer CONTENT_ADMIN = 8; //内容管理员
    public static final Integer ADMIN = 9; //管理员
    public static final Integer CONTENT_OPERATION = 10; //内容运营

    public static List<Integer> asstRoleLevel = Lists.newArrayList(COACH_LEVEL_1, COACH_LEVEL_2,
            COACH_LEVEL_3, COACH_LEVEL_4, COACH_LEVEL_4,COACH_LEVEL_5,COACH_LEVEL_6,COACH_LEVEL_7,COACH_LEVEL_8,CONTENT_OPERATION, CONTENT_ADMIN);

    public static Role stranger(){
        Role stranger = new Role();
        stranger.setId(STRANGE);
        stranger.setName("陌生人");
        stranger.setLevel(STRANGE);
        return stranger;
    }

    public static Role student(){
        Role stranger = new Role();
        stranger.setId(STUDENT);
        stranger.setName("学生");
        stranger.setLevel(STUDENT);
        return stranger;
    }

    public static boolean isAsst(int roleLevel){
        return asstRoleLevel.contains(roleLevel);
    }

}
