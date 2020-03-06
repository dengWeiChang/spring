package org.framework.core;

import org.framework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 发现方法以及构造函数的参数名的接口
 *
 * <p>参数名不一定会被发现
 * 可以尝试各种策略
 * eg 利用编译时的debug信息
 * eg aspectJ的arg name注释
 *
 * @author dengweichang
 */
public interface ParameterNamedDiscoverer {

	/**
	 * 获取方法的参数名
	 * @param method 查找参数的方法
	 * @return 可被解析的参数名称数组
	 */
	@Nullable
	String[] getParameterNames(Method method);

	/**
	 * 获取方法参数名
	 * @param ctor 构造函数
	 * @return 可被解析的参数名称数组
	 */
	@Nullable
	String[] getParameterNames(Constructor<?> ctor);
}
