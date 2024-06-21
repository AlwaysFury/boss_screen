package com.boss.common.constant;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/18
 */
public class LogisticsStatusConst {

    public static final String LOGISTICS_NOT_STARTED = "初始状态，订单未准备好履行";

    public static final String LOGISTICS_REQUEST_CREATED = "订单安排发货";

    public static final String LOGISTICS_PICKUP_DONE = "订单移交给第三方物流";

    public static final String LOGISTICS_PICKUP_RETRY = "订购待处理的 3PL 重试取件";

    public static final String LOGISTICS_PICKUP_FAILED = "由于取货失败或取货但无法继续送货而被 3PL 取消订单";

    public static final String LOGISTICS_DELIVERY_DONE = "订单成功交付";

    public static final String LOGISTICS_DELIVERY_FAILED = "由于3PL交付失败，订单被取消";

    public static final String LOGISTICS_REQUEST_CANCELED = "在LOGISTICS_REQUEST_CREATED点下单时取消订单";

    public static final String LOGISTICS_COD_REJECTED = "综合物流货到付款：货到付款订单被拒绝。";

    public static final String LOGISTICS_READY = "订单准备履行 从付款角度看：非货到付款：订单已付货到付款：订单通过货到付款筛选";

    public static final String LOGISTICS_INVALID = "在LOGISTICS_READY点下单时取消订单";

    public static final String LOGISTICS_LOST = "由于3PL丢失订单而取消订单";

    public static final String LOGISTICS_PENDING_ARRANGE = "订单物流待定安排";
}
