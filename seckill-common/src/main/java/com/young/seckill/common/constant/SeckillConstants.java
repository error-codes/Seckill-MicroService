package com.young.seckill.common.constant;

public class SeckillConstants {

    // region:           订单处理方式常量
    /**
     * 数据库方式
     */
    public static final String PLACE_ORDER_TYPE_DB = "db";

    /**
     * 分布式锁方法
     */
    public static final String PLACE_ORDER_TYPE_LOCK = "lock";

    /**
     * lua脚本方式
     */
    public static final String PLACE_ORDER_TYPE_LUA = "lua";

    // endregion


    // region:           过期时间相关常量
    /**
     * Token 有效时长
     */
    public static final Long TOKEN_VALID_DURATION = 100L;

    /**
     * 事务日志 有效时长
     */
    public static final Long TX_LOG_VALID_DURATION = 7 * 24 * 3600L;

    /**
     * 时间常量 5 分钟
     */
    public static final Long FIVE_MINUTES = 5 * 60L;

    /**
     * 订单任务 有效时长
     */
    public static final Long ORDER_TASK_VALID_DURATION = 24 * 3600L;

    // endregion


    // region:           Redis 相关常量
    /**
     * Lua 脚本后缀
     **/
    public static final String LUA_SUFFIX = "_lua";

    // endregion


    // region:            活动相关 Redis
    /**
     * 活动 KEY
     */
    public static final String ACTIVITY_KEY = "activity:";

    /**
     * 分布式缓存锁活动操作 KEY 后缀
     */
    public static final String DISTRIBUTED_ACTIVITY_LOCK_SUFFIX = "|DISTRIBUTED_ACTIVITY_LOCK";

    /**
     * 秒杀活动缓存 KEY 前缀
     */
    public static final String SECKILL_ACTIVITY_CACHE_KEY = "SECKILL_ACTIVITY_CACHE|";

    /**
     * 秒杀活动列表缓存 KEY 前缀
     */
    public static final String SECKILL_ACTIVITY_LIST_CACHE_KEY = "SECKILL_ACTIVITY_LIST_CACHE|";

    // endregion


    // region:            商品相关 Redis
    /**
     * 商品 KEY
     */
    public static final String GOODS_KEY = "goods:";

    /**
     * 商品事务列表 KEY
     */
    public static final String GOODS_TX_KEY = "goods_tx:";

    /**
     * 商品库存 KEY
     */
    public static final String GOODS_ITEM_STOCK_KEY_PREFIX = "goods:stock:";

    /**
     * 商品限购 KEY
     */
    public static final String GOODS_ITEM_LIMIT_KEY_PREFIX = "goods:limit:";

    /**
     * 商品上架标识 KEY
     */
    public static final String GOODS_ITEM_SHELVES_KEY_PREFIX = "goods:shelves:";

    /**
     * 分布式缓存锁商品操作 KEY 后缀
     */
    public static final String DISTRIBUTED_GOODS_LOCK_SUFFIX = "|DISTRIBUTED_GOODS_LOCK";

    /**
     * 秒杀商品缓存 KEY 前缀
     */
    public static final String SECKILL_GOODS_CACHE_KEY = "SECKILL_GOODS_CACHE|";

    /**
     * 秒杀商品列表缓存 KEY 前缀
     */
    public static final String SECKILL_GOODS_LIST_CACHE_KEY = "SECKILL_GOODS_LIST_CACHE|";

    // endregion


    // region:            库存相关 Redis
    /**
     * 秒杀库存缓存 KEY 前缀
     */
    public static final String SECKILL_STOCK_CACHE_KEY = "SECKILL_STOCK_CACHE|";

    /**
     * 分布式缓存锁库存操作 KEY 后缀
     */
    public static final String DISTRIBUTED_STOCK_LOCK_SUFFIX = "|DISTRIBUTED_STOCK_LOCK";
    // endregion


    // region:            订单相关 Redis
    /**
     * 订单 Redis KEY
     */
    public static final String ORDER_KEY = "order:";

    /**
     * 订单任务 ID KEY
     */
    public static final String ORDER_TASK_KEY = "order:task:";

    /**
     * 订单任务ID 存储 订单ID KEY
     */
    public static final String ORDER_TASK_ORDER_KEY = "order:task:order:";

    /**
     * 订单任务 Token KEY
     */
    public static final String ORDER_TASK_TOKENS_KEY = "order:task:token:";

    /**
     * 加锁获取最新的下单许可
     */
    public static final String ORDER_TASK_LOCK_REFRESH_TOKENS_KEY = "order:task:token:refresh:";

    /**
     * 订单事务列表 KEY
     */
    public static final String ORDER_TX_KEY = "order_tx:";

    /**
     * 分布式缓存锁订单操作 KEY 后缀
     */
    public static final String DISTRIBUTED_ORDER_LOCK_SUFFIX = "|DISTRIBUTED_ORDER_LOCK";

    // endregion


    // region:            用户相关 Redis
    /**
     * 用户 Redis Key
     */
    public static final String USER_KEY = "user:";

    // endregion


    // region:            状态定义相关常量
    /**
     * 商品库存不存在
     */
    public static final Long LUA_GOODS_STOCK_NOT_EXIST = -1L;

    /**
     * 商品购买量小于等于 0
     */
    public static final Long LUA_GOODS_QUANTITY_LT_ZERO = -2L;

    /**
     * 商品库存小于等于 0
     */
    public static final Long LUA_GOODS_STOCK_GT_ZERO = -3L;

    /**
     * LUA 脚本未执行
     */
    public static final Long LUA_NOT_EXECUTED = -100L;

    /**
     * LUA 脚本执行成功
     */
    public static final Long LUA_EXECUTED_SUCCESS = 1L;

    /**
     * 恢复库存已经执行
     */
    public static final Long CHECK_RECOVER_STOCK_HAS_EXECUTE = 0L;

    /**
     * 恢复库存未执行
     */
    public static final Long CHECK_RECOVER_STOCK_NOT_EXECUTE = 1L;

    /**
     * 下单许可类型
     */
    public static final String TYPE_LICENSE = "license_type";

    /**
     * 订单类型
     */
    public static final String TYPE_ORDER = "order_type";

    /**
     * 事件发布类型
     */
    public static final String TYPE_ROCKETMQ = "rocketmq";

    /**
     * 事件发布类型
     */
    public static final String TYPE_SPRING = "spring";

    // endregion


    // region:            分布式事务常量
    public static final String TCC_ORDER_TRY_KEY     = "order:try:";
    public static final String TCC_ORDER_CONFIRM_KEY = "order:confirm:";
    public static final String TCC_ORDER_CANCEL_KEY  = "order:cancel:";

    // endregion


    // region:          ROCKETMQ 相关常量
    /**
     * 事务消息 TOPIC
     */
    public static final String TX_MESSAGE_TOPIC = "tx_message_topic";

    /**
     * 异常消息 TOPIC
     */
    public static final String ERROR_MESSAGE_TOPIC = "error_message_topic";

    /**
     * 提交订单任务 TOPIC
     */
    public static final String SUBMIT_ORDER_MESSAGE_TOPIC = "submit_order_message_topic";

    /**
     * 活动事件 TOPIC
     */
    public static final String EVENT_TOPIC_ACTIVITY_KEY = "event_topic_activity";

    /**
     * 商品事件 TOPIC
     */
    public static final String EVENT_TOPIC_GOODS_KEY = "event_topic_goods";

    /**
     * 订单事件 TOPIC
     */
    public static final String EVENT_TOPIC_ORDER_KEY = "event_topic_order";

    /**
     * 库存事件 TOPIC
     */
    public static final String EVENT_TOPIC_STOCK_KEY = "event_topic_stock";

    /**
     * 活动事件消费分组
     */
    public static final String EVENT_ACTIVITY_CONSUMER_GROUP = "event_activity_consumer_group";

    /**
     * 商品事件消费分组
     */
    public static final String EVENT_GOODS_CONSUMER_GROUP = "event_goods_consumer_group";

    /**
     * 订单事件消费分组
     */
    public static final String EVENT_ORDER_CONSUMER_GROUP = "event_order_consumer_group";

    /**
     * 库存事件消费分组
     */
    public static final String EVENT_STOCK_CONSUMER_GROUP = "event_stock_consumer_group";

    /**
     * 订单补偿消费分组
     */
    public static final String TX_ORDER_CONSUMER_GROUP = "tx_order_consumer_group";

    /**
     * 提交订单消费分组
     */
    public static final String SUBMIT_ORDER_CONSUMER_GROUP = "submit_order_consumer_group";

    /**
     * 商品补偿消费分组
     */
    public static final String TX_GOODS_CONSUMER_GROUP = "tx_goods_consumer_group";

    // endregion


    // region:           库存分桶  相关常量
    /**
     * 库存编排暂存
     */
    public static final String STOCK_BUCKET_SUSPEND_KEY = "stock:buckets:suspend:";

    /**
     * 库存分桶
     */
    public static final String STOCK_BUCKET_AVAILABLE_KEY = "stock:buckets:available:";

    /**
     * 库存分桶编排
     */
    public static final String STOCK_BUCKET_ARRANGEMENT_KEY = "stock:buckets:arrangement:";

    /**
     * 库存分桶数量
     */
    public static final String STOCK_BUCKET_QUANTITY_KEY = "stocket:buckets:quantity:";

    // endregion


    /**
     * 获取 Redis 中存储的 Key
     */
    public static String getKey(String prefix, Object key) {
        return prefix + key;
    }
}