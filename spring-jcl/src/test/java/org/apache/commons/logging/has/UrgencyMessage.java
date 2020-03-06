package org.apache.commons.logging.has;

/**
 * 加急消息
 * @author dengweichang
 */
public class UrgencyMessage extends AbstractMessage {

	public UrgencyMessage(MessageImplementor messageImplementor) {
		super(messageImplementor);
	}

	@Override
	public void sendMessage(String message, String toUser) {
		System.out.println("加急+++");
		super.sendMessage(message, toUser);
	}

	public Object watch(String messageId) {
		System.out.println("监控加急消息");
		return 1;
	}
}
