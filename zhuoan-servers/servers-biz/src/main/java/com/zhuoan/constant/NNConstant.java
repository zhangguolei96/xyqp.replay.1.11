package com.zhuoan.constant;

/**
 * @author wqm
 * @DESCRIPTION
 * @Date Created in 9:11 2018/4/17
 * @Modified By:
 **/
public class NNConstant {

    /**
     * 抢庄类型-房主坐庄
     */
    public static final int NN_BANKER_TYPE_FZ = 0;
    /**
     * 抢庄类型-轮庄
     */
    public static final int NN_BANKER_TYPE_LZ = 1;
    /**
     * 抢庄类型-抢庄
     */
    public static final int NN_BANKER_TYPE_QZ = 2;
    /**
     * 抢庄类型-明牌抢庄
     */
    public static final int NN_BANKER_TYPE_MP = 3;
    /**
     * 抢庄类型-牛牛坐庄
     */
    public static final int NN_BANKER_TYPE_NN = 4;
    /**
     * 抢庄类型-通比
     */
    public static final int NN_BANKER_TYPE_TB = 5;

    /**
     * 无人抢庄-随机庄家
     */
    public static final int NN_QZ_NO_BANKER_SJ = 1;
    /**
     * 无人抢庄-解散房间
     */
    public static final int NN_QZ_NO_BANKER_JS = -1;
    /**
     * 无人抢庄-重开局
     */
    public static final int NN_QZ_NO_BANKER_CK = 2;

    /**
     * 牛牛游戏状态-初始
     */
    public static final int NN_GAME_STATUS_INIT=0;
    /**
     * 牛牛游戏状态-准备
     */
    public static final int NN_GAME_STATUS_READY=1;
    /**
     * 牛牛游戏状态-抢庄
     */
    public static final int NN_GAME_STATUS_QZ=2;
    /**
     * 牛牛游戏状态-定庄
     */
    public static final int NN_GAME_STATUS_DZ=3;
    /**
     * 牛牛游戏状态-下注
     */
    public static final int NN_GAME_STATUS_XZ=4;
    /**
     * 牛牛游戏状态-亮牌
     */
    public static final int NN_GAME_STATUS_LP=5;
    /**
     * 牛牛游戏状态-结算
     */
    public static final int NN_GAME_STATUS_JS=6;
    /**
     * 牛牛游戏状态-总结算
     */
    public static final int NN_GAME_STATUS_ZJS=7;

    /**
     * 玩家状态-初始
     */
    public static final int NN_USER_STATUS_INIT=0;
    /**
     * 玩家状态-准备
     */
    public static final int NN_USER_STATUS_READY=1;
    /**
     * 玩家状态-抢庄
     */
    public static final int NN_USER_STATUS_QZ=2;
    /**
     * 玩家状态-定庄
     */
    public static final int NN_USER_STATUS_DZ=3;
    /**
     * 玩家状态-下注
     */
    public static final int NN_USER_STATUS_XZ=4;
    /**
     * 玩家状态-亮牌
     */
    public static final int NN_USER_STATUS_LP=5;
    /**
     * 玩家状态-结算
     */
    public static final int NN_USER_STATUS_JS=6;
    /**
     * 玩家状态-总结算
     */
    public static final int NN_USER_STATUS_ZJS=7;

    /**
     * 牛牛最少开始人数
     */
    public static final int NN_MIN_START_COUNT = 2;
    /**
     * 牛牛游戏倒计时-初始
     */
    public static final int NN_TIMER_INIT = 0;
    /**
     * 牛牛游戏倒计时-准备
     */
    public static final int NN_TIMER_READY = 10;
    /**
     * 牛牛游戏倒计时-抢庄
     */
    public static final int NN_TIMER_QZ = 15;
    /**
     * 牛牛游戏倒计时-下注
     */
    public static final int NN_TIMER_XZ = 10;
    /**
     * 牛牛游戏倒计时-亮牌
     */
    public static final int NN_TIMER_SHOW = 15;

    public static final String DATA_KEY_VALUE = "value";
    public static final String DATA_KEY_MONEY = "money";


}