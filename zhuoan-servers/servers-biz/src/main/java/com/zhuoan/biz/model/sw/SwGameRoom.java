package com.zhuoan.biz.model.sw;

import com.zhuoan.biz.model.GameRoom;
import net.sf.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wqm
 * @DESCRIPTION
 * @Date Created in 13:34 2018/6/15
 * @Modified By:
 **/
public class SwGameRoom extends GameRoom{
    /**
     * 筹码
     */
    private JSONArray baseNum = new JSONArray();
    /**
     * 押宝
     */
    private int treasure = 0;
    /**
     * 下注信息
     */
    private JSONArray betArray = new JSONArray();
    /**
     * 输赢信息
     */
    private JSONArray summaryArray = new JSONArray();
    /**
     * 赔率
     */
    private double ratio;
    /**
     * 单子上限
     */
    private int singleMax = 0;
    /**
     * 玩家未下注局数
     */
    private Map<String,Integer> userUnBetTime = new HashMap<>();

    public JSONArray getBaseNum() {
        return baseNum;
    }

    public void setBaseNum(JSONArray baseNum) {
        this.baseNum = baseNum;
    }

    public int getTreasure() {
        return treasure;
    }

    public void setTreasure(int treasure) {
        this.treasure = treasure;
    }

    public JSONArray getBetArray() {
        return betArray;
    }

    public void setBetArray(JSONArray betArray) {
        this.betArray = betArray;
    }

    public JSONArray getSummaryArray() {
        return summaryArray;
    }

    public void setSummaryArray(JSONArray summaryArray) {
        this.summaryArray = summaryArray;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public int getSingleMax() {
        return singleMax;
    }

    public void setSingleMax(int singleMax) {
        this.singleMax = singleMax;
    }

    public Map<String, Integer> getUserUnBetTime() {
        return userUnBetTime;
    }

    public void setUserUnBetTime(Map<String, Integer> userUnBetTime) {
        this.userUnBetTime = userUnBetTime;
    }
}
