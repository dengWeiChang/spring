package org.apache.commons.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import java.io.Serializable;

/**
 * spring JCL 适配器
 * 检测 Log4j 2.x / SLF4J
 *
 * @author dengweichang
 */
final class LogAdapter {

	private static final String LOG4J_SPI = "org.apache.logging.log4j.spi.ExtendedLogger";

	private static final String LOG4J_SLF4J_PROVIDER = "org.apache.logging.slf4j.SLF4JProvider";

	private static final String SLF4J_SPI = "org.slf4j.spi.LocationAwareLogger";

	private static final String SLF4J_API = "org.slf4j.Logger";

	private static final LogApi LOG_API;

	static {
		if (isPresent(LOG4J_SPI)) {
			if (isPresent(LOG4J_SLF4J_PROVIDER) && isPresent(SLF4J_SPI)) {
				//log4j桥接至slf4j，虽然倾向于使用slf4j，但使用桥接可以避免未知的变化
				//slf4j不支持位置识别（自己猜的- -）
				LOG_API = LogApi.SLF4J_LAL;
			} else {
				//使用log4j
				LOG_API = LogApi.SLF4J;
			}
		}
		else if (isPresent(SLF4J_SPI)) {
			//包括位置识别的完整的slf4j spi
			LOG_API = LogApi.SLF4J_LAL;
		}
		else if (isPresent(SLF4J_API)) {
			//简化版的slf4j 不支持位置识别
			LOG_API = LogApi.SLF4J;
		}
		else {
			//默认的java日志
			LOG_API = LogApi.JUL;
		}
	}

	private LogAdapter() {
	}

	public static Log createLog(String name) {
		switch (LOG_API) {
			case LOG4J:
				return Log4jAdapter.createLog(name);
			case SLF4J_LAL:
				return Slf4jAdapter.createLocationAwareLog(name);
			case SLF4J:
				return Slf4jAdapter.createLog(name);
			default:
				//JDK9 默认不再提供日志模块，暂时不考虑了 - -
				return null;
		}
	}

	private static boolean isPresent(String className) {
		try {
			//JVM加载日志类，但并不初始化
			Class.forName(className, false, LogAdapter.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}





	/**
	 * log api 枚举
	 */
	private enum LogApi {
		/**
		 * log4j, log4j to slf4j bridge, slf4j, java util log
		 */
		LOG4J, SLF4J_LAL, SLF4J, JUL
	}

	private static class Log4jAdapter {
		public static Log createLog(String name) {
			return new Log4jLog(name);
		}
	}

	/**
	 * 创建slf4j
	 * 有位置感知及没有位置感知
	 */
	private static class Slf4jAdapter {
		public static Log createLocationAwareLog(String name) {
			Logger logger = LoggerFactory.getLogger(name);
			return (logger instanceof LocationAwareLogger) ? new Slf4jLocationAwareLog((LocationAwareLogger) logger) : new Slf4jLog<>(logger);
		}

		public static Log createLog(String name) {
			return new Slf4jLog<>(LoggerFactory.getLogger(name));
		}
	}

	/**
	 * bridge -> 修正抽象化日志 log4j
	 */
	private static class Log4jLog implements Log, Serializable {

		//Full Qualified Class Name
		private static final String FQCN = Log4jLog.class.getName();

		private static final LoggerContext LOGGER_CONTEXT =
				LogManager.getContext(Log4jLog.class.getClassLoader(), false);

		/**
		 * bridge -> 实现化角色
		 * {@link Log4jLog}中通过名称指定具体的实现化角色
		 */
		private final ExtendedLogger logger;

		public Log4jLog(String name) {
			LoggerContext context = LOGGER_CONTEXT;
			if (context == null) {
				System.out.println("构造函数中静态变量为空");
				// Circular call in early-init scenario -> static field not initialized yet
				//TODO:静态变量在构造函数执行时还未初始化的情况有待验证
				context = LogManager.getContext(Log4jLog.class.getClassLoader(), false);
			}
			this.logger = context.getLogger(name);
		}

		@Override
		public boolean isFatalEnabled() {
			return this.logger.isEnabled(Level.FATAL);
		}

		@Override
		public boolean isErrorEnabled() {
			return logger.isEnabled(Level.ERROR);
		}

		@Override
		public boolean isWarnEnabled() {
			return logger.isEnabled(Level.WARN);
		}

		@Override
		public boolean isInfoEnabled() {
			return logger.isEnabled(Level.INFO);
		}

		@Override
		public boolean isDebugEnabled() {
			return logger.isEnabled(Level.DEBUG);
		}

		@Override
		public boolean isTraceEnabled() {
			return logger.isEnabled(Level.TRACE);
		}

		@Override
		public void fatal(Object message) {
			log(Level.FATAL, message, null);
		}

		@Override
		public void fatal(Object message, Throwable t) {
			log(Level.FATAL, message, t);
		}

		@Override
		public void error(Object message) {
			log(Level.ERROR, message, null);
		}

		@Override
		public void error(Object message, Throwable t) {
			log(Level.ERROR, message, t);
		}

		@Override
		public void warn(Object message) {
			log(Level.WARN, message, null);
		}

		@Override
		public void warn(Object message, Throwable t) {
			log(Level.WARN, message, t);
		}

		@Override
		public void info(Object message) {
			log(Level.INFO, message, null);
		}

		@Override
		public void info(Object message, Throwable t) {
			log(Level.INFO, message, t);
		}

		@Override
		public void debug(Object message) {
			log(Level.DEBUG, message, null);
		}

		@Override
		public void debug(Object message, Throwable t) {
			log(Level.DEBUG, message, t);
		}

		@Override
		public void trace(Object message) {
			log(Level.TRACE, message, null);
		}

		@Override
		public void trace(Object message, Throwable t) {
			log(Level.TRACE, message, t);
		}

		private void log(Level level, Object message, Throwable exception) {
			if (message instanceof String) {
				if (exception != null) {
					this.logger.logIfEnabled(FQCN, level, null, (String) message, exception);
				}
				else {
					this.logger.logIfEnabled(FQCN, level, null, (String) message);
				}
			}
			else {
				this.logger.logIfEnabled(FQCN, level, null, message, exception);
			}
		}
	}

	/**
	 * bridge -> 修正抽象化日志 slf4j
	 * @param <T>
	 */
	private static class Slf4jLog<T extends Logger> implements Log, Serializable {
		final String name;

		protected transient T logger;

		public Slf4jLog(T logger) {
			this.name = logger.getName();
			this.logger = logger;
		}

		@Override
		public boolean isFatalEnabled() {
			return false;
		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		/**
		 * Slf4j中没有fatal(飞都- -)级别的日志
		 */
		@Override
		public void fatal(Object message) {
			error(message);
		}

		@Override
		public void fatal(Object message, Throwable t) {
			error(message, t);
		}

		@Override
		public void error(Object message) {
			if (message instanceof String || this.logger.isErrorEnabled()) {
				this.logger.error(String.valueOf(message));
			}
		}

		@Override
		public void error(Object message, Throwable t) {
			if (message instanceof String || this.logger.isErrorEnabled()) {
				this.logger.error(String.valueOf(message), t);
			}
		}

		/**
		 * 记录警告级别的日志
		 *
		 * @param message log this message
		 */
		@Override
		public void warn(Object message) {
			if (message instanceof String || this.logger.isWarnEnabled()) {
				this.logger.warn(String.valueOf(message));
			}
		}

		@Override
		public void warn(Object message, Throwable t) {
			if (message instanceof String || this.logger.isWarnEnabled()) {
				this.logger.warn(String.valueOf(message), t);
			}
		}

		@Override
		public void info(Object message) {
			if (message instanceof String || this.logger.isInfoEnabled()) {
				this.logger.info(String.valueOf(message));
			}
		}

		@Override
		public void info(Object message, Throwable t) {
			if (message instanceof String || this.logger.isInfoEnabled()) {
				this.logger.info(String.valueOf(message), t);
			}
		}

		@Override
		public void debug(Object message) {
			if (message instanceof String || this.logger.isDebugEnabled()) {
				this.logger.debug(String.valueOf(message));
			}
		}

		@Override
		public void debug(Object message, Throwable t) {
			if (message instanceof String || this.logger.isDebugEnabled()) {
				this.logger.debug(String.valueOf(message), t);
			}
		}

		@Override
		public void trace(Object message) {
			if (message instanceof String || this.logger.isTraceEnabled()) {
				this.logger.trace(String.valueOf(message));
			}
		}

		@Override
		public void trace(Object message, Throwable t) {
			if (message instanceof String || this.logger.isTraceEnabled()) {
				this.logger.trace(String.valueOf(message), t);
			}
		}

	}

	/**
	 * bridge -> 修正抽象化日志  带有位置感知的 slf4j
	 */
	private static class Slf4jLocationAwareLog extends Slf4jLog<LocationAwareLogger> implements Serializable {

		private static final String FQCN = Slf4jLocationAwareLog.class.getName();

		public Slf4jLocationAwareLog(LocationAwareLogger logger) {
			super(logger);
		}

		@Override
		public void fatal(Object message) {
			error(message);
		}

		@Override
		public void fatal(Object message, Throwable exception) {
			error(message, exception);
		}

		@Override
		public void error(Object message) {
			if (message instanceof String || this.logger.isErrorEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, String.valueOf(message), null, null);
			}
		}

		@Override
		public void error(Object message, Throwable exception) {
			if (message instanceof String || this.logger.isErrorEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, String.valueOf(message), null, exception);
			}
		}

		@Override
		public void warn(Object message) {
			if (message instanceof String || this.logger.isWarnEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.WARN_INT, String.valueOf(message), null, null);
			}
		}

		@Override
		public void warn(Object message, Throwable exception) {
			if (message instanceof String || this.logger.isWarnEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.WARN_INT, String.valueOf(message), null, exception);
			}
		}

		@Override
		public void info(Object message) {
			if (message instanceof String || this.logger.isInfoEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.INFO_INT, String.valueOf(message), null, null);
			}
		}

		@Override
		public void info(Object message, Throwable exception) {
			if (message instanceof String || this.logger.isInfoEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.INFO_INT, String.valueOf(message), null, exception);
			}
		}

		@Override
		public void debug(Object message) {
			if (message instanceof String || this.logger.isDebugEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, String.valueOf(message), null, null);
			}
		}

		@Override
		public void debug(Object message, Throwable exception) {
			if (message instanceof String || this.logger.isDebugEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, String.valueOf(message), null, exception);
			}
		}

		@Override
		public void trace(Object message) {
			if (message instanceof String || this.logger.isTraceEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, String.valueOf(message), null, null);
			}
		}

		@Override
		public void trace(Object message, Throwable exception) {
			if (message instanceof String || this.logger.isTraceEnabled()) {
				this.logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, String.valueOf(message), null, exception);
			}
		}

	}

}
