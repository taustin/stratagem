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
    public void testPrimitiveIfCast1() {
        IfExpr ifExpr = new IfExpr(
                ValueExpr.trueSingleton,
                ValueExpr.trueSingleton,
                ValueExpr.unitSingleton);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());

        assertEquals(AnyType.singleton, ifResultType);
    }

    @Test
    // Assert that
    //   if (true) { true } else { <?>unit }
    // has type ?
    public void testPrimitiveIfCast2() {
        IfExpr ifExpr = new IfExpr(
                ValueExpr.trueSingleton,
                ValueExpr.trueSingleton,
                TestUtils.makeAny(UnitVal.singleton));

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());

        assertEquals(AnyType.singleton, ifResultType);
    }

    @Test
    // Assert that
    //   let fn1: Int -> Unit = ... in
    //   let fn2: Unit -> Unit = ... in
    //   if (true) { fn1 } else { fn2 }
    // has type ? -> Unit
    public void testSimpleFunctionIfCast1() {
        Expression fn1 = TestUtils.makeTrivialFn(IntType.singleton, UnitType.singleton);
        Expression fn2 = TestUtils.makeTrivialFn(UnitType.singleton, UnitType.singleton);

        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, fn1, fn2);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type anyToUnit = new ClosureType(AnyType.singleton, UnitType.singleton);

        assertEquals(anyToUnit, ifResultType);
    }

    @Test
    // Assert that
    //   let fn1: ? -> Unit = ... in
    //   let fn2: Unit -> Unit = ... in
    //   if (true) { fn1 } else { fn2 }
    // has type ? -> Unit
    public void testSimpleFunctionIfCast2() {
        Expression fn1 = TestUtils.makeTrivialFn(AnyType.singleton, UnitType.singleton);
        Expression fn2 = TestUtils.makeTrivialFn(UnitType.singleton, UnitType.singleton);

        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, fn1, fn2);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type anyToUnit = new ClosureType(AnyType.singleton, UnitType.singleton);

        assertEquals(anyToUnit, ifResultType);
    }

    @Test
    // Assert that
    //   let fn1: Unit -> Int = ... in
    //   let fn2: Unit -> Unit = ... in
    //   if (true) { fn1 } else { fn2 }
    // has type Unit -> ?
    public void testSimpleFunctionIfCast3() {
        Expression fn1 = TestUtils.makeTrivialFn(UnitType.singleton, IntType.singleton);
        Expression fn2 = TestUtils.makeTrivialFn(UnitType.singleton, UnitType.singleton);

        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, fn1, fn2);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type unitToAny = new ClosureType(UnitType.singleton, AnyType.singleton);

        assertEquals(unitToAny, ifResultType);
    }

    @Test
    // Assert that
    //   let fn1: Unit -> ? = ... in
    //   let fn2: Unit -> Int = ... in
    //   if (true) { fn1 } else { fn2 }
    // has type Unit -> ?
    public void testSimpleFunctionIfCast4() {
        Expression fn1 = TestUtils.makeTrivialFn(UnitType.singleton, AnyType.singleton);
        Expression fn2 = TestUtils.makeTrivialFn(UnitType.singleton, UnitType.singleton);

        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, fn1, fn2);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type unitToAny = new ClosureType(UnitType.singleton, AnyType.singleton);

        assertEquals(unitToAny, ifResultType);
    }

    @Test
    // Assert that
    //   let identity: ? -> ? = fn(x: ?): ? { x } in
    //   let succ: Int -> Int = fn(n: Int): Int { n + 1 } in
    //   if (true) { identity } else { succ }
    // has type ? -> ?
    public void testSimpleFunctionIfCast5() {
        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, TestUtils.id, TestUtils.succ);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type anyToAny = new ClosureType(AnyType.singleton, AnyType.singleton);

        assertEquals(anyToAny, ifResultType);
    }

    @Test
    // Assert that
    //   let fn1: String -> Int -> ? = ... in
    //   let fn2: Unit -> Int -> Int = ... in
    //   if (true) { fn1 } else { fn2 }
    // has type ? -> Int -> ?
    public void testComplexFunctionIfCast() {
        Expression fn1 = TestUtils.makeTrivialFn(StringType.singleton, IntType.singleton, AnyType.singleton);
        Expression fn2 = TestUtils.makeTrivialFn(UnitType.singleton, IntType.singleton, IntType.singleton);

        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, fn1, fn2);

        Type ifResultType = ifExpr.typecheck(new TypeEnvironment());
        Type anyToIntToAny = new ClosureType(
                AnyType.singleton,
                new ClosureType(
                        IntType.singleton,
                        AnyType.singleton));

        assertEquals(anyToIntToAny, ifResultType);
    }
}
