package org.apache.commons.logging.no;

/**
 * 加短消息
 * @author dengweichang
 */
public class UrgencyMessageSMS implements UrgencyMessage {

	/**
	 * 监控消息处理过程
	 *
	 * @param messageId 消息编号
	 * @return 处理状态
	 */
	public Object watch(String messageId) {
		return null;
	}

	/**
	 * 发送消息行为
	 *
	 * @param message 消息内容
	 * @param toUser  target
	 */
	public void send(String message, String toUser) {
		System.out.println("加急————系统短消息");
	}
}
