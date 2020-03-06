package org.framework.core;

import org.framework.lang.Nullable;

/**
 * 公开spring版本
 * @author dengweichang
 */
public class SpringVersion {
	public SpringVersion() {
	}

	@Nullable
	public static String getVersion() {
		Package pkg = SpringVersion.class.getPackage();
		return pkg != null ? pkg.getImplementationVersion() : null;
	}

	public static void main(String[] args) {
		System.out.println(SpringVersion.getVersion());
	}
}
