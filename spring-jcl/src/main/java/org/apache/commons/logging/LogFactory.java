package org.apache.commons.logging;

/**
 * 最轻量的 apache commons logging api
 * 提供了基础的log查询方法
 * @author dengweichang
 */
public class LogFactory {

	/**
	 * 返回指定的日志程序
	 * @param clazz 包含派生的日志类的类
	 */
	public static Log getLog(Class<?> clazz) {
		return getLog(clazz.getName());
	}

	/**
	 * 返回指定的日志程序
	 * @param name log实例的名称
	 */
	public static Log getLog(String name) {
		return LogAdapter.createLog(name);
	}
}
