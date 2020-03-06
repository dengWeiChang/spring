package org.apache.commons.logging.no;

/**
 * 邮件消息
 * @author dengweichang
 */
public class CommonMessageEmail implements Message {

	/**
	 * 发送消息行为
	 *
	 * @param message 消息内容
	 * @param toUser  target
	 */
	public void send(String message, String toUser) {
		System.out.println("发送邮件成功");
	}
}
