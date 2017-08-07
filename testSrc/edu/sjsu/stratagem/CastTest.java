package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemCastException;
import org.junit.Test;

import static org.junit.Assert.*;

public class CastTest {

    // Produce an int value with type Any, as in:
    //   fn(n: Int): ? { n }(val)
    private Expression makeAny(int val) {
        FunctionDeclExpr f = new FunctionDeclExpr(
                "n",
                IntType.singleton,
                AnyType.singleton,
                new VarExpr("n"));
        return new FunctionAppExpr(f, new ValueExpr(new IntVal(val)));
    }

    @Test
    // Assert that
    //   let n: ? = 1 in
    //   fn(s: String): Unit { unit }
    // throws a StratagemCastException
    public void testApplicationCastException() {
        Expression n = makeAny(1);
        FunctionDeclExpr f = new FunctionDeclExpr(
                "s",
                StringType.singleton,
                UnitType.singleton,
                ValueExpr.unitSingleton);
        FunctionAppExpr app = new FunctionAppExpr(f, n);

        app.typecheck(new TypeEnvironment());  // Insert cast that will fail.

        try {
            app.evaluate(new ValueEnvironment());
        } catch (StratagemCastException e) {
            return;  // pass
        }
        assertTrue("Failed to throw StratagemCastException", false);
    }

    @Test
    // Assert that
    //   if (true) { 1 } else { unit }
    // has type ?
    public void testPrimitiveIfCast() {
    }

    @Test
    // Assert that
    //   let identity: ? -> ? = fn(x: ?): ? { x } in
    //   let succ: Int -> Int = fn(n: Int): Int { n + 1 } in
    //   if (true) { identity } else { succ }
    // has type Int -> ?
    public void testSimpleFunctionIfCast() {
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
