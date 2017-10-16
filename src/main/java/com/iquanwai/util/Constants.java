package com.iquanwai.util;

/**
 * Created by nethunder on 2016/12/21.
 */
public interface Constants {

    interface Status {
        String OK = "1";
        String FAIL = "0";
    }

    interface AccountError {
        Integer TIME_OUT = 100001;
    }

    interface PracticeType {
        int CHALLENGE = 21;
        int APPLICATION_REVIEW = 12;
        int APPLICATION = 11;
        int SUBJECT = 3;
    }

    interface VoteType {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface PictureType {
        int HOMEWORK = 1;
        int CHALLENGE = 2;
        int APPLICATION = 3;
        int SUBJECT = 4;
    }

    interface CommentModule {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface CommentType {
        int STUDENT = 1;
    }

    interface Module {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface ViewInfo {
        interface Module {
            int CHALLENGE = 1;
            int APPLICATION = 2;
            int SUBJECT = 3;
        }

        interface EventType {
            int PC_SUBMIT = 1;
            int MOBILE_SUBMIT = 2;
            int PC_SHOW = 3;
            int MOBILE_SHOW = 4;
        }
    }

    interface Device {
        int PC = 1;
        int MOBILE = 2;
    }

    interface LabelArticleModule {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface WEIXIN_MESSAGE_TYPE {
        int TEXT = 1;
        int IMAGE = 2;
        int VOICE = 3;
        int NEWS = 4;
        int DISTRIBUTE = 5;
    }

    interface RISE_MEMBER {
        int FREE = 0;
        int MEMBERSHIP = 1;
        int COURSE_USER = 2;
        int MONTHLY_CAMP = 3;
    }

    interface COUPON_CATEGORY {
        /**
         * 只能用来购买会员
         */
        String ONLY_MEMBERSHIP = "ELITE_RISE_MEMBER";

        /**
         * 只能用来购买线下工作坊
         */
        String ONLY_WORKSHOP = "OFF_LINE_WORKSHOP";
    }

    interface BUSINESS_GOODS {
        int CAMP = 1;
        int HALF_PROFESSIONAL = 2;
        int PROFESSIONAL = 3;
        int OTHER = 4;
    }

}
