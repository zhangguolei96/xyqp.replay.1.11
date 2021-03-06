package com.zhuoan.queue;


import com.zhuoan.service.jms.ProducerService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Destination;


@Component
public class SqlQueue {

    @Resource
    private ProducerService producerService;

    @Resource
    private Destination sqlQueueDestination;


//	private BlockingQueue<SqlModel> queue = new LinkedBlockingQueue<SqlModel>();
//	private MaJiangBiz mjBiz = new MajiangBizImpl();
//
//	public SqlQueue(int works) {
//		for (int i = 0; i < works; i++) {
////			startQueue();
//		}
////		startQueue1();
//	}
	
	/**
	 * 添加到数据库队列
	 * @param @param sqlModel   
	 * @return void  
	 * @throws
	 * @date 2018年3月24日
	 */
	public void addSqlTask(SqlModel sqlModel){
//		queue.add(sqlModel);
        producerService.sendMessage(sqlQueueDestination,sqlModel);

	}
	
//	public void startQueue(){
//		new Thread(){
//			public void run(){
//				try {
//					while(true){
//						SqlModel sqlModel = queue.take();
//						switch (sqlModel.type) {
//						case SqlModel.EXECUTEUPDATEBYSQL:// 更新
//							DBUtil.executeUpdateBySQL(sqlModel.sql, sqlModel.params);
//							break;
//						case 4:// 抽水
//							mjBiz.pump(sqlModel.userIds, sqlModel.roomNo, sqlModel.gid, sqlModel.fee, sqlModel.type1);
//							break;
//						default:
//							break;
//						}
//					}
//				} catch (Exception e) {
//					LogUtil.print("----------数据库队列处理异常----------");
//					logger.error("",e);
//				}
//			}
//		}.start();
//	}
	
//	public void startQueue1(){
//		new Thread(){
//			public void run(){
//				try {
//					while(true){
//						UserInfoCache.updateCache();
//						Thread.sleep(5000);
//					}
//				} catch (Exception e) {
//					LogUtil.print("----------数据库队列处理异常----------");
//					logger.error("",e);
//				}
//			}
//		}.start();
//	}
}
