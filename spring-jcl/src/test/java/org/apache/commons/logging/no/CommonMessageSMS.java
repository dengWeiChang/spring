package org.apache.commons.logging.no;

/**
 * SMS短消息
 * @author dengweichang
 */
public class CommonMessageSMS implements Message {

	/**
	 * 发送消息行为
	 *
	 * @param message 消息内容
	 * @param toUser  target
	 */
	public void send(String message, String toUser) {
		System.out.println("短消息发送成功");
	}
}
