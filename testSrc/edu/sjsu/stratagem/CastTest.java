package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemCastException;
import org.junit.Test;

import static org.junit.Assert.*;

public class CastTest {
    @Test
    // Assert that
    //   let n: ? = 1 in
    //   fn(s: String): Unit { unit }
    // throws a StratagemCastException
    public void testApplicationCastException() {
        Expression f = TestUtils.makeTrivialFn(StringType.singleton, UnitType.singleton);
        Expression n = TestUtils.makeAny(1);
        FunctionAppExpr app = new FunctionAppExpr(f, n);

        app.typecheck(new TypeEnvironment());  // Insert cast that will fail.

        try {
            app.evaluate(new ValueEnvironment());
        } catch (StratagemCastException e) {
            return;  // Test passed. No need to call assert.
        }

        assertTrue("Failed to throw StratagemCastException", false);
    }

    @Test
    // Assert that
    //   if (true) { true } else { unit }
    // has type ?
    public void testPrimitiveIfCast() {
        IfExpr ifExpr = new IfExpr(
                ValueExpr.trueSingleton,
                ValueExpr.trueSingleton,
                ValueExpr.unitSingleton);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());

        assertEquals(AnyType.singleton, ifResultType);
    }

    @Test
    // Assert that
    //   let identity: ? -> ? = fn(x: ?): ? { x } in
    //   let succ: Int -> Int = fn(n: Int): Int { n + 1 } in
    //   if (true) { identity } else { succ }
    // has type Int -> ?
    public void testSimpleFunctionIfCast() {
        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, TestUtils.id, TestUtils.succ);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type intToAny = new ClosureType(IntType.singleton, AnyType.singleton);

        assertEquals(intToAny, ifResultType);
    }

    @Test
    // Assert that
    //   let fn1: String -> Int -> ? = ... in
    //   let fn2: Unit -> Int -> Int = ... in
    //   if (true) { fn1 } else { fn2 }
    // has type ? -> Int -> Int
    public void testComplexFunctionIfCast() {
        Expression fn1 = TestUtils.makeTrivialFn(StringType.singleton, IntType.singleton, AnyType.singleton);
        Expression fn2 = TestUtils.makeTrivialFn(UnitType.singleton, IntType.singleton, IntType.singleton);

        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, fn1, fn2);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type anyToIntToInt = new ClosureType(
                AnyType.singleton,
                new ClosureType(
                        IntType.singleton,
                        IntType.singleton));

        assertEquals(anyToIntToInt, ifResultType);
    }
}
