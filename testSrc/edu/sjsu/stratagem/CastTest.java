package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemCastException;
import org.junit.Test;

import static org.junit.Assert.*;

public class CastTest {

    // Produce an int value with type Any.
    //   fn(n: Int): ? { val }
    private Expression makeAny(int val) {
        FunctionDeclExpr f = new FunctionDeclExpr(
                "n",
                IntType.singleton,
                AnyType.singleton,
                new VarExpr("n"));
        return new FunctionAppExpr(f, new ValueExpr(new IntVal(val)));
    }

    @Test
    // let n: ? = 1
    // in fn(s: String): Unit { unit }
    //  throws StratagemCastException
    public void testApplicationCastException() {
        Expression n = makeAny(1);
        FunctionDeclExpr f = new FunctionDeclExpr(
                "s",
                StringType.singleton,
                UnitType.singleton,
                new ValueExpr(UnitVal.singleton));
        FunctionAppExpr app = new FunctionAppExpr(f, n);

        app.typecheck(new TypeEnvironment());  // Insert cast that will fail.

        try {
            app.evaluate(new ValueEnvironment());
        } catch (StratagemCastException e) {
            return;  // pass
        }
        assertTrue("Failed to throw StratagemCastException", false);
    }
}
