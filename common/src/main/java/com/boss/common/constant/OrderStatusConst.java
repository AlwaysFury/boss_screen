package com.boss.common.constant;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/22
 */
public class OrderStatusConst {

    public static final String UNPAID = "订单已创建，买家尚未付款。";

    public static final String READY_TO_SHIP = "卖家可以安排发货。";

    public static final String PROCESSED = "卖家已在线安排发货并从 3PL 获得跟踪号。";

    public static final String RETRY_SHIP = "3PL 取件包裹失败。需要重新安排发货。";

    public static final String SHIPPED = "包裹已投递至 3PL 或由 3PL 取件。";

    private static final String TO_CONFIRM_RECEIVE = "买家已收到订单。";

    public static final String IN_CANCEL = "订单的取消正在处理中。";

    public static final String CANCELLED = "订单已取消。";

    public static final String TO_RETURN = "买家要求退货，订单退货正在处理中。";

    public static final String COMPLETED = "订单已完成。";

}
