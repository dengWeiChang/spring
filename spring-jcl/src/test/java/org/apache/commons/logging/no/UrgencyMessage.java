package org.apache.commons.logging.no;

/**
 * 加急消息
 * @author dengweichang
 */
public interface UrgencyMessage extends Message {

	/**
	 * 监控消息处理过程
	 * @param messageId 消息编号
	 * @return 处理状态
	 */
	Object watch(String messageId);
}
