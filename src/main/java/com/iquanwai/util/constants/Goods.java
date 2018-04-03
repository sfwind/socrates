package com.iquanwai.util.constants;


/**
 * @author nethunder
 * <p>
 * 商品
 */

public enum Goods {
    /**
     * 商学院
     */
    ELITE("fragment_member", "3", "elite"),
    /**
     * 精英半年
     */
    HALF_ELITE("fragment_member", "4", "half_elite"),
    /**
     * 专业一年
     */
    ANNUAL("fragment_member", "2", "annual"),
    /**
     * 专业半年
     */
    HALF("fragment_member", "1", "half"),
    /**
     * 训练营
     */
    CAMP("fragment_camp", "5", "camp");

    // 成员变量
    private String goodsType;
    private String goodsId;
    private String roleName;

    public static String ERROR = "error";

    private Goods(String goodsType, String goodsId, String roleName) {
        this.goodsId = goodsId;
        this.goodsType = goodsType;
        this.roleName = roleName;
    }

    public String getGoodsType() {
        return this.goodsType;
    }

    public String getGoodsId() {
        return this.goodsId;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public static Goods find(String goodsType, String goodsId) {
        for (Goods goods : values()) {
            if (goods.goodsType.equals(goodsType) && goods.goodsId.equals(goodsId)) {
                return goods;
            }
        }
        return null;
    }

    public static Goods find(String goodsId) {
        for (Goods goods : values()) {
            if (goods.goodsId.equals(goodsId)) {
                return goods;
            }
        }
        return null;
    }

}
