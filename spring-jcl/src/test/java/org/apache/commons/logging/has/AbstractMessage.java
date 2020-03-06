package org.apache.commons.logging.has;

/**
 * 抽象层：消息
 * @author dengweichang
 */
public abstract class AbstractMessage {

	MessageImplementor messageImplementor;

	public AbstractMessage(MessageImplementor messageImplementor) {
		this.messageImplementor = messageImplementor;
	}

	public void sendMessage(String message, String toUser) {
		this.messageImplementor.send(message, toUser);
	}
}
