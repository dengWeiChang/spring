package org.apache.commons.logging.has;

/**
 * 普通消息
 * @author dengweichang
 */
public class CommonMessage extends AbstractMessage {

	public CommonMessage(MessageImplementor messageImplementor) {
		super(messageImplementor);
	}

	@Override
	public void sendMessage(String message, String toUser) {
		super.sendMessage(message, toUser);
	}
}
