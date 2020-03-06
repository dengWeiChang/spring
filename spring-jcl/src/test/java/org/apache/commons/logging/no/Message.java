package org.apache.commons.logging.no;

/**
 * 消息抽象接口
 * @author dengweichang
 */
public interface Message {

	/**
	 * 发送消息行为
	 * @param message 消息内容
	 * @param toUser target
	 */
	void send(String message, String toUser);
}
