package org.apache.commons.logging.bridge;

/**
 * 抽象化等级——修正抽象化角色
 * @author dengweichang
 */
public class RefinedAbstraction extends Abstraction {


	public RefinedAbstraction(Implementor implementor) {
		super(implementor);
	}

	public void otherOperation() {

	}
}
