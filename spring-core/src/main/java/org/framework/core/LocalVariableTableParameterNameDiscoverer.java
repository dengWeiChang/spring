package org.framework.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ParameterNamedDiscoverer}
 *
 * @author dengweichang
 */
public class LocalVariableTableParameterNameDiscoverer implements ParameterNamedDiscoverer {

	private static final Log logger = LogFactory.getLog(LocalVariableTableParameterNameDiscoverer.class);

	/**
	 * 被标记为没有debug信息的class对象
	 */
	private static final Map<Executable, String[]> NO_DEBUG_INFO_MAP = Collections.emptyMap();

	private final Map<Class<?>, Map<Executable, String[]>> parameterNamesCache = new ConcurrentHashMap<>(32);

	@Override
	public String[] getParameterNames(Method method) {
//		B
		return new String[0];
	}

	@Override
	public String[] getParameterNames(Constructor<?> ctor) {
		return new String[0];
	}
}
