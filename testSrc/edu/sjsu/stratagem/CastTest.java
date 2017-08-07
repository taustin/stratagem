package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemCastException;
import org.junit.Test;

import static org.junit.Assert.*;

public class CastTest {
    // Produce an value with type Any, as in:
    //   fn(x: ?): ? { x }(val)
    // which has type
    //   ?
    // and which evaluates to
    //   val
    private static FunctionAppExpr makeAny(Value value) {
        return new FunctionAppExpr(id, new ValueExpr(value));
    }

    private static FunctionAppExpr makeAny(int n) {
        return makeAny(new IntVal(n));
    }

    // The identity function:
    //   fn(x: ?): ? { x }
    private static final FunctionDeclExpr id = new FunctionDeclExpr(
            "x",
            AnyType.singleton,
            AnyType.singleton,
            new VarExpr("x"));

    // The successor function:
    //   fn(n: Int): Int { n + 1 }
    private static final FunctionDeclExpr succ = new FunctionDeclExpr(
            "n",
            IntType.singleton,
            IntType.singleton,
            new BinOpExpr(Op.ADD, new VarExpr("n"), new ValueExpr(new IntVal(1))));

    @Test
    // Assert that
    //   let n: ? = 1 in
    //   fn(s: String): Unit { unit }
    // throws a StratagemCastException
    public void testApplicationCastException() {
        FunctionDeclExpr f = new FunctionDeclExpr(
                "s",
                StringType.singleton,
                UnitType.singleton,
                ValueExpr.unitSingleton);
        Expression n = makeAny(1);
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
        IfExpr ifExpr = new IfExpr(ValueExpr.trueSingleton, id, succ);

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
    }
}
