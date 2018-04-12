package com.zhuoan.biz.event.bdx;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.apache.activemq.memory.buffer.MessageQueue;

/**
 * 比大小游戏监听事件
 */
public class BDXGameEvent {
	
	public static SocketIOServer server=null;
	public static MessageQueue queue = null;
	
	public static void listenerBDXGameEvent(SocketIOServer servers, MessageQueue messageQueue){
		
		server=servers;
		queue = messageQueue;
		
		final BDXGameEventDeal bdxService=new BDXGameEventDeal();
		
		/**
		 * 加入房间，或创建房间事件
		 */
		server.addEventListener("enterRoom_BDX", Object.class,new DataListener<Object>() {
			
			@Override
			public void onData(SocketIOClient client, Object data, AckRequest ackSender){
				
//				try {
//					queue.addQueue(new Messages(client, data, 10, 1));
//					//bdxService.enterRoom(client, data);
//				} catch (Exception e) {
//					Logger.getLogger(BDXGameEvent.class).error(e.getMessage(), e);
//					e.printStackTrace();
//				}
				
				
				//========================================准备定时器倒计时================================================
				/*try {
					JSONObject json = new JSONObject();
					json.element("type", 1);
					MutliThreadSSS m = new MutliThreadSSS(client, data , sssService,json);
					m.start();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} */

			}
		});
		
		
	
		
		/**
		 * 游戏事件
		 */
		server.addEventListener("gameEvent_BDX", Object.class,new DataListener<Object>() {
			
			@Override
			public void onData(SocketIOClient client, Object data, AckRequest ackSender){

//				try {
//					queue.addQueue(new Messages(client, data, 10, 2));
//					//bdxService.gameEvent(client, data);
//				} catch (Exception e) {
//					Logger.getLogger(BDXGameEvent.class).error(e.getMessage(), e);
//					e.printStackTrace();
//				}
			}
		});
		
		/**
		 * 游戏结算事件
		 */
		server.addEventListener("gameSummary_BDX", Object.class,new DataListener<Object>() {
			
			@Override
			public void onData(SocketIOClient client, Object data, AckRequest ackSender){
				
//				try {
//					queue.addQueue(new Messages(client, data, 10, 3));
//					//bdxService.gameSummary(client, data);
//				} catch (Exception e) {
//					Logger.getLogger(BDXGameEvent.class).error(e.getMessage(), e);
//					e.printStackTrace();
//				}
			}
		});
		
		
		
		
		
		/**
		 * 退出房间事件
		 */
		server.addEventListener("exitRoom_BDX", Object.class,new DataListener<Object>() {

			@Override
			public void onData(SocketIOClient client, Object data, AckRequest ackSender){

//				try {
//					queue.addQueue(new Messages(client, data, 10, 4));
//					//bdxService.exitRoom(client, data);
//				} catch (Exception e) {
//					Logger.getLogger(BDXGameEvent.class).error(e.getMessage(), e);
//					e.printStackTrace();
//				}
			}
		});

		
	
		/**
		 * 断线重连事件
		 */
		server.addEventListener("reconnectGame_BDX", Object.class,new DataListener<Object>() {

			@Override
			public void onData(SocketIOClient client, Object data, AckRequest ackSender){

//				try {
//					queue.addQueue(new Messages(client, data, 10, 5));
//					//bdxService.reconnectGame(client, data);
//				} catch (Exception e) {
//					Logger.getLogger(BDXGameEvent.class).error(e.getMessage(), e);
//					e.printStackTrace();
//				}
			}
		});
		
	
	
	}
}