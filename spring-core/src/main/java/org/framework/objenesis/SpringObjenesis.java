package org.framework.objenesis;

import org.objenesis.Objenesis;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.Objects;

/**
 * {@link org.objenesis.ObjenesisStd} / {@link org.objenesis.ObjenesisBase}
 * 提供基于{@code Class}的缓存（不是类名）
 * 允许有选择的使用缓存
 *
 * @author dengweichang
 */
public class SpringObjenesis implements Objenesis {

	/**
	 * 表明spring忽略objenesis属性，
	 * <p>若该属性为true，则spring运行期间不尝试使用objenesis实例化对象并立即回退代码路径
	 * 这意味着所有 CGLIB AOP 都是通过常规方式初始化对象
	 */
	public static final String IGNORE_OBJENESIS_PROPERTY_NAME = "spring.objenesis.ignore";

	/**
	 * 实例化策略
	 * 选择JVM实现等（自己猜的 - -）
	 */
	private final InstantiatorStrategy strategy;

//	private final ConcurrentRex

	private volatile Boolean worthTrying;

	/**
	 * 使用标准策略实例化
	 */
	public SpringObjenesis() {
		this(null);
	}

	/**
	 * 指定实例化策略
	 * @param strategy 要使用的实例化策略
	 */
	public SpringObjenesis(InstantiatorStrategy strategy) {
		this.strategy = (strategy != null ? strategy : new StdInstantiatorStrategy());
	}

	/**
	 * 是否存在尝试加载的价值
	 * i.e. 是否还没有使用过 或者是否有效
	 * <p>如果当前的实例化策略被JVM认为不可运行，或者spring.objenesis.ignore被设为true，则返回false
	 * @return true/false
	 */
	public boolean isWorthTrying() {
		return Objects.equals(this.worthTrying, Boolean.TRUE);
	}

	@Override
	public <T> T newInstance(Class<T> aClass) {
		return null;
	}

	@Override
	public <T> ObjectInstantiator<T> getInstantiatorOf(Class<T> aClass) {
		return null;
	}
}
