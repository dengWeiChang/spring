package org.apache.commons.logging.staticOrder;

/**
 * 先执行static代码块
 */
public class StaticOrderTest {

	static final A a = new A();

	public StaticOrderTest() {
		System.out.println("construct: test");
	}

	private static class A {
		static int count = 1;
		static {
			System.out.println("执行A静态快");
		}

		public A() {
			count++;
			System.out.println("construct:" + count);
		}
	}

	public static void main(String[] args) {
		new StaticOrderTest();
	}


}