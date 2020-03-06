package org.framework.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.framework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * spring本地属性的静态持有者
 *
 * <p>加载 spring根目录下 {@code spring.properties} 文件
 * {@link #setProperty} 可以设置属性
 * 检查属性时优先返回本地属性，然后返回 {@link System#getProperty} 获取的JVM属性
 *
 * <p> spring.getenv.ignore/spring.beaninfo.ignore是另一种可以设置spring系统相关属性的方法
 * {@link #setFlag} 将本地标识设为true以允许覆盖本地配置
 *
 * @author dengweichang
 */
public final class SpringProperties {

	private static final String PROPERTIES_RESOURCE_LOCATION = "spring.properties";

	private static final Log logger = LogFactory.getLog(SpringProperties.class);

	private static final Properties localProperties = new Properties();

	static {
		try {
			ClassLoader cl = SpringProperties.class.getClassLoader();
			//cl怎么什么情况下会为空呢 - -
			URL url = (cl != null ? cl.getResource(PROPERTIES_RESOURCE_LOCATION) :
					ClassLoader.getSystemResource(PROPERTIES_RESOURCE_LOCATION));
			if (url != null) {
				logger.debug("found 'spring.properties' file in local classpath");
				try (InputStream is = url.openStream()) {
					localProperties.load(is);
				}
			}
		} catch (IOException e) {
			if (logger.isInfoEnabled()) {
				logger.info("could not load 'spring.properties' file from local classpath: " + e);
			}
		}
	}

	private SpringProperties() {
	}

	/**
	 * 设置本地配置
	 * @param key the property key
	 * @param value if null to reset it
	 */
	public static void setProperty(String key, @Nullable String value) {
		if (value != null) {
			localProperties.setProperty(key, value);
		}
		else {
			localProperties.remove(key);
		}
	}

	/**
	 * 获取配置
	 *
	 * @param key the property key
	 * @return spring property (若spring配置中没有则获取jvm级别的属性)
	 */
	@Nullable
	public static String getProperty(String key) {
		String value = localProperties.getProperty(key);
		if (value != null) {
			return value;
		}
		try {
			value = System.getProperty(key);
		} catch (Exception e) {
			if (logger.isInfoEnabled()) {
				logger.info("could not retrieve system property '" + key + "':" + e );
			}
		}
		return value;
	}

	/**
	 * 将某个配置设为true
	 * @param key the property key
	 */
	public static void setFlag(String key) {
		localProperties.put(key, Boolean.TRUE.toString());
	}

	public static boolean getFlat(String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

}
