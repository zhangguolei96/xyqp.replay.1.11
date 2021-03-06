package com.zhuoan.biz.event.bdx;

import com.corundumstudio.socketio.SocketIOClient;
import com.zhuoan.biz.event.FundEventDeal;
import com.zhuoan.biz.game.biz.RoomBiz;
import com.zhuoan.biz.game.biz.UserBiz;
import com.zhuoan.biz.model.Playerinfo;
import com.zhuoan.biz.model.RoomManage;
import com.zhuoan.biz.model.bdx.BDXGameRoomNew;
import com.zhuoan.biz.model.dao.PumpDao;
import com.zhuoan.constant.BDXConstant;
import com.zhuoan.constant.CommonConstant;
import com.zhuoan.constant.DaoTypeConstant;
import com.zhuoan.service.cache.RedisService;
import com.zhuoan.service.jms.ProducerService;
import com.zhuoan.util.Dto;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Destination;
import java.util.*;

/**
 * @author wqm
 * @DESCRIPTION
 * @Date Created in 14:46 2018/4/26
 * @Modified By:
 **/
@Component
public class BDXGameEventDealNew {

    @Resource
    private RoomBiz roomBiz;

    @Resource
    private UserBiz userBiz;

    @Resource
    private Destination daoQueueDestination;

    @Resource
    private ProducerService producerService;

    @Resource
    private FundEventDeal fundEventDeal;

    @Resource
    private RedisService redisService;

    /**
     * 创建房间通知自己
     * @param client
     */
    public void createRoom(SocketIOClient client, Object data) {
        JSONObject postData = JSONObject.fromObject(data);
        String account = postData.getString(CommonConstant.DATA_KEY_ACCOUNT);
        String roomNo = postData.getString(CommonConstant.DATA_KEY_ROOM_NO);
        BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(roomNo);
        JSONObject roomData = obtainRoomData(roomNo, account);
        // 数据不为空
        if (!Dto.isObjNull(roomData)) {
            JSONObject result = new JSONObject();
            result.put(CommonConstant.RESULT_KEY_CODE, CommonConstant.GLOBAL_YES);
            result.put("data", roomData);
            // 通知自己
            CommonConstant.sendMsgEventToSingle(client, result.toString(), "enterRoomPush_BDX");
        }
        if (room.getUserPacketMap().size()==2) {
            startGame(room);
        }
    }

    /**
     * 加入房间通知
     * @param client
     * @param data
     */
    public void joinRoom(SocketIOClient client, Object data) {
        // 进入房间通知自己
        createRoom(client, data);
        JSONObject joinData = JSONObject.fromObject(data);
        // 非重连通知其他玩家
        if (joinData.containsKey("isReconnect") && joinData.getInt("isReconnect") == 0) {
            String account = joinData.getString(CommonConstant.DATA_KEY_ACCOUNT);
            BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(joinData.getString(CommonConstant.DATA_KEY_ROOM_NO));
            Playerinfo player = room.getPlayerMap().get(account);
            JSONObject obj = new JSONObject();
            obj.put("account", player.getAccount());
            obj.put("name", player.getName());
            obj.put("headimg", player.getRealHeadimg());
            obj.put("sex", player.getSex());
            obj.put("ip", player.getIp());
            obj.put("vip", player.getVip());
            obj.put("location", player.getLocation());
            obj.put("area", player.getArea());
            obj.put("score", player.getScore());
            obj.put("index", player.getMyIndex());
            obj.put("userOnlineStatus", player.getStatus());
            obj.put("ghName", player.getGhName());
            obj.put("introduction", player.getSignature());
            obj.put("userStatus", room.getUserPacketMap().get(account).getStatus());
            obj.put("value", room.getUserPacketMap().get(account).getValue());
            obj.put("sum", room.getUserPacketMap().get(account).getScore());
            // 通知玩家
            CommonConstant.sendMsgEventToAll(room.getAllUUIDList(account), obj.toString(), "playerEnterPush_BDX");
        }
    }

    /**
     * 开始游戏
     * @param room
     */
    public void startGame(BDXGameRoomNew room){
        room.initGame();
        room.setGameStatus(BDXConstant.BDX_GAME_STATUS_GAME_EVENT);
    }

    /**
     * 下注
     * @param client
     * @param data
     */
    public void xiaZhu(SocketIOClient client,Object data){
        JSONObject postData = JSONObject.fromObject(data);
        if (!CommonConstant.checkEvent(postData,BDXConstant.BDX_GAME_STATUS_GAME_EVENT,client)&&
            !CommonConstant.checkEvent(postData,BDXConstant.BDX_GAME_STATUS_SUMMARY,client)){
            return;
        }
        // 房间号
        final String roomNo = postData.getString(CommonConstant.DATA_KEY_ROOM_NO);
        BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(roomNo);
        // 玩家账号
        String account = postData.getString(CommonConstant.DATA_KEY_ACCOUNT);
        if (postData.containsKey("value")) {
            double value = postData.getDouble("value");
            JSONObject result = new JSONObject();
            if (room.getUserPacketMap().get(account).getStatus()!=BDXConstant.BDX_USER_STATUS_INIT) {
                result.put(CommonConstant.RESULT_KEY_CODE,CommonConstant.GLOBAL_NO);
                result.put(CommonConstant.RESULT_KEY_MSG,"当前无法下注");
                CommonConstant.sendMsgEventToSingle(client,result.toString(),"gameXiazhuPush_BDX");
                return;
            }
            JSONObject userInfo = userBiz.getUserByAccount(account);
            if (!Dto.isObjNull(userInfo) && userInfo.containsKey(room.getCurrencyType())) {
                if (value < 0 || userInfo.getDouble(room.getCurrencyType()) < value) {
                    result.put(CommonConstant.RESULT_KEY_CODE,CommonConstant.GLOBAL_NO);
                    result.put(CommonConstant.RESULT_KEY_MSG,"元宝不足");
                    CommonConstant.sendMsgEventToSingle(client,result.toString(),"gameXiazhuPush_BDX");
                    return;
                }
                redisService.insertKey("summaryTimes_bdx"+room.getRoomNo(),"0",null);
                room.getUserPacketMap().get(account).setStatus(BDXConstant.BDX_USER_STATUS_GAME_EVENT);
                room.getUserPacketMap().get(account).setValue(value);
                result.put(CommonConstant.RESULT_KEY_CODE,CommonConstant.GLOBAL_YES);
                result.put("index",room.getPlayerMap().get(account).getMyIndex());
                result.put("value",value);
                CommonConstant.sendMsgEventToAll(room.getAllUUIDList(),result.toString(),"gameXiazhuPush_BDX");
            }
        }
    }

    /**
     * 游戏事件
     * @param client
     * @param data
     */
    public void gameEvent(SocketIOClient client,Object data){
        JSONObject postData = JSONObject.fromObject(data);
        if (!CommonConstant.checkEvent(postData,BDXConstant.BDX_GAME_STATUS_GAME_EVENT,client)&&
            !CommonConstant.checkEvent(postData,BDXConstant.BDX_GAME_STATUS_SUMMARY,client)){
            return;
        }
        // 房间号
        final String roomNo = postData.getString(CommonConstant.DATA_KEY_ROOM_NO);
        BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(roomNo);
        // 玩家账号
        String account = postData.getString(CommonConstant.DATA_KEY_ACCOUNT);
        JSONObject userInfo = userBiz.getUserByAccount(account);
        if (Dto.isObjNull(userInfo) || userInfo.getDouble(room.getCurrencyType()) < room.getUserPacketMap().get(account).getScore()) {
            return;
        }
        JSONObject result = new JSONObject();
        if (room.getUserPacketMap().get(account).getStatus()!=BDXConstant.BDX_USER_STATUS_GAME_EVENT) {
            result.put(CommonConstant.RESULT_KEY_CODE,CommonConstant.GLOBAL_NO);
            result.put(CommonConstant.RESULT_KEY_MSG,"当前无法弃牌");
            CommonConstant.sendMsgEventToSingle(client,result.toString(),"gameEventPush_BDX");
            return;
        }
        summary(room,account);
        JSONArray gameData = new JSONArray();
        for (String uuid : room.getUserPacketMap().keySet()) {
            if (room.getUserPacketMap().containsKey(uuid)&&room.getUserPacketMap().get(uuid)!=null) {
                JSONObject userData = new JSONObject();
                userData.put("scoreLeft",room.getPlayerMap().get(uuid).getScore());
                userData.put("index",room.getPlayerMap().get(uuid).getMyIndex());
                userData.put("sum",room.getUserPacketMap().get(uuid).getScore());
                userData.put("pai",room.getUserPacketMap().get(uuid).getPai());
                userData.put("iswin",room.getUserPacketMap().get(uuid).getIsWin());
                gameData.add(userData);
            }
        }
        result.put(CommonConstant.RESULT_KEY_CODE,CommonConstant.GLOBAL_YES);
        result.put("gameData",gameData);
        CommonConstant.sendMsgEventToAll(room.getAllUUIDList(),result.toString(),"gameEventPush_BDX");
        // 资金盘
        if (room.isFund()) {
            Map<String,Double> map = new HashMap<String, Double>();
            JSONArray array = new JSONArray();
            for (String uuid : room.getUserPacketMap().keySet()) {
                if (room.getUserPacketMap().containsKey(uuid)&&room.getUserPacketMap().get(uuid)!=null) {
                    map.put(room.getPlayerMap().get(uuid).getOpenId(),room.getUserPacketMap().get(uuid).getScore());
                    JSONObject obj = new JSONObject();
                    obj.put("sum",room.getUserPacketMap().get(uuid).getScore());
                    obj.put("openId",room.getPlayerMap().get(uuid).getOpenId());
                    array.add(obj);
                }
            }
            fundEventDeal.addGameOrder(array);
        }
    }

    /**
     * 退出房间
     * @param client
     * @param data
     */
    public void exitRoom(SocketIOClient client,Object data){
        JSONObject postData = JSONObject.fromObject(data);
        if (!CommonConstant.checkEvent(postData,CommonConstant.CHECK_GAME_STATUS_NO,client)) {
            return;
        }
        final String roomNo = postData.getString(CommonConstant.DATA_KEY_ROOM_NO);
        BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(roomNo);
        // 资金盘
        if (room.isFund()) {
            for (String account : room.getRobotList()) {
                fundEventDeal.sysUserExit(account);
            }
        }
        List<UUID> list = room.getAllUUIDList();
        redisService.deleteByKey("summaryTimes_bdx"+roomNo);
        RoomManage.gameRoomMap.remove(roomNo);
        JSONObject result = new JSONObject();
        result.put(CommonConstant.RESULT_KEY_CODE,CommonConstant.GLOBAL_YES);
        CommonConstant.sendMsgEventToAll(list,result.toString(),"exitRoomPush_BDX");
        JSONObject roomInfo = new JSONObject();
        roomInfo.put("room_no", room.getRoomNo());
        roomInfo.put("status", -1);
        producerService.sendMessage(daoQueueDestination, new PumpDao(DaoTypeConstant.UPDATE_ROOM_INFO, roomInfo));
    }

    /**
     * 重连
     * @param client
     * @param data
     */
    public void reconnectGame(SocketIOClient client,Object data) {
        JSONObject postData = JSONObject.fromObject(data);
        if (!postData.containsKey(CommonConstant.DATA_KEY_ROOM_NO)||
            !postData.containsKey(CommonConstant.DATA_KEY_ACCOUNT)||
            !postData.containsKey("uuid")) {
            return;
        }
        String roomNo = postData.getString(CommonConstant.DATA_KEY_ROOM_NO);
        String account = postData.getString(CommonConstant.DATA_KEY_ACCOUNT);
        JSONObject userInfo = userBiz.getUserByAccount(account);
        // uuid不匹配
        if (!userInfo.containsKey("uuid")||Dto.stringIsNULL(userInfo.getString("uuid"))||
            !userInfo.getString("uuid").equals(postData.getString("uuid"))) {
            return;
        }
        JSONObject result = new JSONObject();
        if (client == null) {
            return;
        }
        // 房间不存在
        if (!RoomManage.gameRoomMap.containsKey(roomNo) || RoomManage.gameRoomMap.get(roomNo) == null) {
            result.put("type", 0);
            CommonConstant.sendMsgEventToSingle(client, result.toString(), "reconnectGamePush_BDX");
            return;
        }
        BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(roomNo);
        // 不在当前房间内
        if (Dto.stringIsNULL(account) || !room.getPlayerMap().containsKey(account) || room.getPlayerMap().get(account) == null) {
            result.put("type", 0);
            CommonConstant.sendMsgEventToSingle(client, result.toString(), "reconnectGamePush_BDX");
            return;
        }
        // 刷新uuid
        room.getPlayerMap().get(account).setUuid(client.getSessionId());
        // 组织数据，通知玩家
        result.put("type", 1);
        result.put("data", obtainRoomData(roomNo, account));
        // 通知玩家
        CommonConstant.sendMsgEventToSingle(client, result.toString(), "reconnectGamePush_BDX");
    }

    public void summary(BDXGameRoomNew room, String giveUpAccount){
        String summaryTimesKey = "summaryTimes_bdx"+room.getRoomNo();
        long summaryTimes = redisService.incr(summaryTimesKey,1);
        if (summaryTimes>1) {
            return;
        }
        // 设置房间状态
        room.setGameStatus(BDXConstant.BDX_GAME_STATUS_SUMMARY);
        // 当局输赢
        double sum = room.getUserPacketMap().get(giveUpAccount).getValue();
        List<Integer> pai = obtainPai();
        for (String account : room.getUserPacketMap().keySet()) {
            if (room.getUserPacketMap().containsKey(account)&&room.getUserPacketMap().get(account)!=null) {
                room.getUserPacketMap().get(account).setStatus(BDXConstant.BDX_USER_STATUS_INIT);
                double oldScore = room.getPlayerMap().get(account).getScore();
                if (account.equals(giveUpAccount)) {
                    room.getUserPacketMap().get(account).setScore(-sum);
                    room.getUserPacketMap().get(account).setPai(new int[]{pai.get(1)});
                    room.getUserPacketMap().get(account).setIsWin(CommonConstant.GLOBAL_NO);
                    room.getPlayerMap().get(account).setScore(Dto.sub(oldScore,sum));
                }else {
                    room.getUserPacketMap().get(account).setScore(sum);
                    room.getUserPacketMap().get(account).setPai(new int[]{pai.get(0)});
                    room.getUserPacketMap().get(account).setIsWin(CommonConstant.GLOBAL_YES);
                    room.getPlayerMap().get(account).setScore(Dto.add(oldScore,sum));
                }
            }
        }
        JSONArray array = new JSONArray();
        JSONArray userDeductionData = new JSONArray();
        JSONArray gameLogResults = new JSONArray();
        JSONArray gameResult = new JSONArray();
        // 存放游戏记录
        JSONArray gameProcessJS = new JSONArray();
        for (String account : room.getUserPacketMap().keySet()) {
            if (room.getUserPacketMap().containsKey(account)&&room.getUserPacketMap().get(account)!=null) {
                // 有参与的玩家
                JSONObject userJS = new JSONObject();
                userJS.put("account", account);
                userJS.put("name", room.getPlayerMap().get(account).getName());
                userJS.put("sum", room.getUserPacketMap().get(account).getScore());
                gameProcessJS.add(userJS);
                // 元宝输赢情况
                JSONObject obj = new JSONObject();
                obj.put("total", room.getPlayerMap().get(account).getScore());
                obj.put("fen", room.getUserPacketMap().get(account).getScore());
                obj.put("id", room.getPlayerMap().get(account).getId());
                array.add(obj);
                // 用户游戏记录
                JSONObject object = new JSONObject();
                object.put("id", room.getPlayerMap().get(account).getId());
                object.put("gid", room.getGid());
                object.put("roomNo", room.getRoomNo());
                object.put("type", room.getRoomType());
                object.put("fen", room.getUserPacketMap().get(account).getScore());
                object.put("old", Dto.sub(room.getPlayerMap().get(account).getScore(),room.getUserPacketMap().get(account).getScore()));
                object.put("new", room.getPlayerMap().get(account).getScore());
                userDeductionData.add(object);
                // 战绩记录
                JSONObject gameLogResult = new JSONObject();
                gameLogResult.put("account", account);
                gameLogResult.put("name", room.getPlayerMap().get(account).getName());
                gameLogResult.put("headimg", room.getPlayerMap().get(account).getHeadimg());
                gameLogResult.put("myIndex", room.getPlayerMap().get(account).getMyIndex());
                gameLogResult.put("score", room.getUserPacketMap().get(account).getScore());
                gameLogResult.put("totalScore", room.getPlayerMap().get(account).getScore());
                gameLogResult.put("win", CommonConstant.GLOBAL_YES);
                if (room.getUserPacketMap().get(account).getStatus() < 0) {
                    gameLogResult.put("win", CommonConstant.GLOBAL_NO);
                }
                gameLogResults.add(gameLogResult);
                // 用户战绩
                JSONObject userResult = new JSONObject();
                userResult.put("zhuang", room.getBanker());
                userResult.put("isWinner", CommonConstant.GLOBAL_NO);
                if (room.getUserPacketMap().get(account).getScore() > 0) {
                    userResult.put("isWinner", CommonConstant.GLOBAL_YES);
                }
                userResult.put("score", room.getUserPacketMap().get(account).getScore());
                userResult.put("totalScore", room.getPlayerMap().get(account).getScore());
                userResult.put("player", room.getPlayerMap().get(account).getName());
                gameResult.add(userResult);
            }
        }
        room.getGameProcess().put("JieSuan", gameProcessJS);
        if (room.getId()==0) {
            JSONObject roomInfo = roomBiz.getRoomInfoByRno(room.getRoomNo());
            if (!Dto.isObjNull(roomInfo)) {
                room.setId(roomInfo.getLong("id"));
            }
        }
        // 更新玩家分数
        userBiz.updateUserBalance(array, room.getCurrencyType());
        // 玩家输赢记录
        producerService.sendMessage(daoQueueDestination, new PumpDao(DaoTypeConstant.USER_DEDUCTION, new JSONObject().element("user", userDeductionData)));
        // 战绩信息
        JSONObject gameLogObj = room.obtainGameLog(gameLogResults.toString(), room.getGameProcess().toString());
        producerService.sendMessage(daoQueueDestination, new PumpDao(DaoTypeConstant.INSERT_GAME_LOG, gameLogObj));
        JSONArray userGameLogs = room.obtainUserGameLog(gameLogObj.getLong("id"), array, gameResult.toString());
        for (int i = 0; i < userGameLogs.size(); i++) {
            producerService.sendMessage(daoQueueDestination, new PumpDao(DaoTypeConstant.INSERT_USER_GAME_LOG, userGameLogs.getJSONObject(i)));
        }
    }

    public static List<Integer> obtainPai(){
        int pai1 = RandomUtils.nextInt(13)+1;
        int pai2 = RandomUtils.nextInt(13)+1;
        int color1 = RandomUtils.nextInt(4);
        int color2 = RandomUtils.nextInt(4);
        List<Integer> list = new ArrayList<Integer>();
        if (pai1==pai2&&color1==color2) {
            return obtainPai();
        }else if (pai1>pai2) {
            list.add(pai1+color1*20);
            list.add(pai2+color2*20);
            return list;
        }else{
            list.add(pai2+color2*20);
            list.add(pai1+color1*20);
            return list;
        }
    }

    /**
     * 获取当前房间数据
     * @param roomNo
     * @param account
     * @return
     */
    public JSONObject obtainRoomData(String roomNo, String account){
        JSONObject roomData = new JSONObject();
        BDXGameRoomNew room = (BDXGameRoomNew) RoomManage.gameRoomMap.get(roomNo);
        roomData.put("gameStatus",room.getGameStatus());
        roomData.put("myIndex",room.getPlayerMap().get(account).getMyIndex());
        roomData.put("room_no",room.getRoomNo());
        roomData.put("roomType",room.getRoomType());
        roomData.put("users",room.getAllPlayer());
        return roomData;
    }
}
