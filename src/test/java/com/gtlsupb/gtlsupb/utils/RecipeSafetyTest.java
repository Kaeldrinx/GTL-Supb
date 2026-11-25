package com.gtlsupb.gtlsupb.utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RecipeSafetyTest {

    private static int passed;
    private static int failed;

    private RecipeSafetyTest() {}

    public static void main(String[] args) {
        testSafeDivideLong();
        testSafeDivideBigInteger();
        testSaturatedMultiply();
        testClamp();
        testAtIndex();
        testMaxFeasible();

        System.out.println("---------------------------------------------");
        System.out.println("RecipeSafety tests: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testSafeDivideLong() {
        assertEquals("正常整除", 12L, RecipeSafety.safeDivide(100L, 8L, -1L));
        assertEquals("除数为 0 时用兜底值", -1L, RecipeSafety.safeDivide(100L, 0L, -1L));
        assertEquals("除数为负时用兜底值", -1L, RecipeSafety.safeDivide(100L, -5L, -1L));
        assertEquals("被除数为 0", 0L, RecipeSafety.safeDivide(0L, 8L, -1L));
        assertEquals("兜底值本身可以是 0", 0L, RecipeSafety.safeDivide(7L, 0L, 0L));
    }

    private static void testSafeDivideBigInteger() {
        assertEquals("BigInteger 正常整除", BigInteger.valueOf(100L),
                RecipeSafety.safeDivide(BigInteger.valueOf(1000L), 10L, BigInteger.ZERO));
        assertEquals("BigInteger 除数为 0 时用兜底值", BigInteger.ZERO,
                RecipeSafety.safeDivide(BigInteger.valueOf(1000L), 0L, BigInteger.ZERO));
        assertEquals("BigInteger 除数为负时用兜底值", BigInteger.ONE,
                RecipeSafety.safeDivide(BigInteger.valueOf(1000L), -2L, BigInteger.ONE));
        assertEquals("BigInteger 被除数为 null 时用兜底值", BigInteger.ZERO,
                RecipeSafety.safeDivide(null, 10L, BigInteger.ZERO));

        boolean thrown = false;
        try {
            BigInteger.TEN.divide(BigInteger.ZERO);
        } catch (ArithmeticException e) {
            thrown = true;
        }
        assertTrue("原生 BigInteger 除以 0 确实抛 ArithmeticException（复现根因）", thrown);
        assertTrue("safeDivide 不抛异常即视为通过",
                RecipeSafety.safeDivide(BigInteger.TEN, 0L, BigInteger.ONE).equals(BigInteger.ONE));
    }

    private static void testSaturatedMultiply() {
        assertEquals("正常乘法", 16384L, RecipeSafety.saturatedMultiply(1L, 16384L));
        assertEquals("零因子返回零", 0L, RecipeSafety.saturatedMultiply(Long.MAX_VALUE, 0L));
        assertEquals("正溢出饱和到 Long.MAX_VALUE", Long.MAX_VALUE,
                RecipeSafety.saturatedMultiply(Long.MAX_VALUE, 2L));
        assertEquals("负溢出饱和到 Long.MIN_VALUE", Long.MIN_VALUE,
                RecipeSafety.saturatedMultiply(Long.MIN_VALUE, 2L));
        assertEquals("异号溢出饱和到 Long.MIN_VALUE", Long.MIN_VALUE,
                RecipeSafety.saturatedMultiply(Long.MAX_VALUE, -2L));
    }

    private static void testClamp() {
        assertEquals("低于下限钳到下限", 1, RecipeSafety.clamp(0, 1, 200));
        assertEquals("负数钳到下限", 1, RecipeSafety.clamp(-99, 1, 200));
        assertEquals("高于上限钳到上限", 200, RecipeSafety.clamp(99999, 1, 200));
        assertEquals("区间内原样返回", 20, RecipeSafety.clamp(20, 1, 200));
        assertEquals("max<min 时以 min 为准（配置被改坏）", 10, RecipeSafety.clamp(50, 10, 1));
    }

    private static void testAtIndex() {
        List<String> list = Arrays.asList("a", "b");
        assertEquals("首元素", "a", RecipeSafety.at(list, 0));
        assertEquals("末元素", "b", RecipeSafety.at(list, 1));
        assertEquals("负索引返回 null", null, RecipeSafety.at(list, -1));
        assertEquals("超界返回 null", null, RecipeSafety.at(list, 2));
        assertEquals("null 列表返回 null", null, RecipeSafety.at(null, 0));
        assertEquals("空列表返回 null", null, RecipeSafety.at(Collections.emptyList(), 0));
    }

    private static void testMaxFeasible() {
        assertEquals("可行区间内整取", 250L, RecipeSafety.maxFeasible(1000L, n -> n <= 250));
        assertEquals("全可行返回 requested", 7L, RecipeSafety.maxFeasible(7L, n -> true));
        assertEquals("全不可行返回 0", 0L, RecipeSafety.maxFeasible(7L, n -> false));
        assertEquals("requested<=0 返回 0", 0L, RecipeSafety.maxFeasible(0L, n -> true));
        assertEquals("大区间二分不溢出", 2_000_000_000L, RecipeSafety.maxFeasible(5_000_000_000L, n -> n <= 2_000_000_000L));
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            passed++;
        } else {
            failed++;
            System.out.println("[FAIL] " + name + "：期望 " + expected + "，实际 " + actual);
        }
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}
