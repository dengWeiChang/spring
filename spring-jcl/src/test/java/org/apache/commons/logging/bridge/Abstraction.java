package org.apache.commons.logging.bridge;

/**
 * 抽象化等级结构——抽象化角色
 * @author dengweichang
 */
public abstract class Abstraction {
	Implementor implementor;

	public Abstraction(Implementor implementor) {
		this.implementor = implementor;
	}

	public void operation() {
		implementor.operationImpl();
	}
}
