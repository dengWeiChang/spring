package org.apache.commons.logging;

/**
 * 日志抽象接口API
 *
 * <p>拥有6个日志级别，依次是
 * <ol>
 *     <li>trace</li>
 *     <li>debug</li>
 *     <li>info</li>
 *     <li>warn</li>
 *     <li>error</li>
 *     <li>fatal</li>
 * </ol>
 *
 * @author dengweichang
 */
public interface Log {

	/**
	 * 当前的日志是否致命（全部信息）
	 * @return	底层日志实现是否超过致命级别
	 */
	boolean isFatalEnabled();

	/**
	 * 当前的日志是否开启error级别
	 * @return	底层日志实现是否启用error级别
	 */
	boolean isErrorEnabled();

	/**
	 * 当前的日志是否开启warn级别
	 * @return	底层日志实现是否启用warn级别
	 */
	boolean isWarnEnabled();

	/**
	 * 当前的日志是否开启info级别
	 * @return	底层日志实现是否启用info级别
	 */
	boolean isInfoEnabled();

	/**
	 * 当前的日志是否开启debug级别
	 * @return	底层日志实现是否启用debug级别
	 */
	boolean isDebugEnabled();

	/**
	 * 当前的日志是否开启Trace级别
	 * @return	底层日志实现是否启用error级别
	 */
	boolean isTraceEnabled();

	/**
	 * 记录致命级别的日志
	 * @param message 日志内容
	 */
	void fatal(Object message);

	/**
	 * 记录致命级别的日志
	 * @param message log this message
	 * @param t 异常
	 */
	void fatal(Object message, Throwable t);

	/**
	 * 记录错误级别的日志
	 * @param message log this message
	 */
	void error(Object message);

	/**
	 * 记录错误级别的日志
	 * @param message log this message
	 * @param t 异常
	 */
	void error(Object message, Throwable t);

	/**
	 * 记录警告级别的日志
	 * @param message log this message
	 */
	void warn(Object message);

	/**
	 * 记录警告级别的日志
	 * @param message log this message
	 * @param t 异常
	 */
	void warn(Object message, Throwable t);

	/**
	 * 记录信息级别的日志
	 * @param message log this message
	 */
	void info(Object message);

	/**
	 * 记录信息级别的日志
	 * @param message log this message
	 * @param t 异常
	 */
	void info(Object message, Throwable t);

	/**
	 * 记录调试级别的日志
	 * @param message log this message
	 */
	void debug(Object message);

	/**
	 * 记录调试级别的日志
	 * @param message log this message
	 * @param t 异常
	 */
	void debug(Object message, Throwable t);

	/**
	 * 记录追踪级别的日志
	 * @param message log this message
	 */
	void trace(Object message);

	/**
	 * 记录追踪级别的日志
	 * @param message log this message
	 * @param t 异常
	 */
	void trace(Object message, Throwable t);
}
