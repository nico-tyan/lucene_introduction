package com.nico.index.seacher;

import org.junit.Test;

/**
 * 测试final结果
 * @Title: 
 * @Package com.nico.index.seacher  
 * @Description: 
 * @author fangshu  
 * @date 2018年11月14日  
 * @version
 */
public class TestFinal {
	@Test
	public void testFinal() throws Exception {

		//System.out.println(doFinal());

		//System.out.println(doFinalInt());

		System.out.println(doFinalStrBuild());
	}

	public String doFinal() {
		String a = "123";
		try {
			a = "124";
			System.out.println("1 try");
			return a;
		} catch (Exception e) {
			System.out.println("2 catch");
			return a;
		} finally {
			a = "125";
			System.out.println("3 finally");
		}

	}

	public int doFinalInt() {
		int a = 123;
		try {
			a = 124;
			System.out.println("1 try");
			return a;
		} catch (Exception e) {
			System.out.println("2 catch");
			return a;
		} finally {
			a = 125;
			System.out.println("3 finally");
		}

	}

	public StringBuilder doFinalStrBuild() {
		StringBuilder a = new StringBuilder("123");
		try {
			a.append("124");
			System.out.println("1 try");
			return a;
		} catch (Exception e) {
			System.out.println("2 catch");
			return a;
		} finally {
			a.append("125");
			System.out.println("3 finally");
		}

	}

}
