package org.apache.commons.logging.has;

/**
 * 实现发送消息的统一接口
 * @author dengweichang
 */
public interface MessageImplementor {

	void send(String message, String toUser);
}
