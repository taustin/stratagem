package edu.sjsu.stratagem;

import org.junit.Test;

public class ExpressionTypecheckTest {

    @Test
    // fn(a: Int b: Int): Int { a + b }(1, 2)
    public void testMultipleParameters() {
        Type[] twoInts = new Type[] {IntType.singleton, IntType.singleton};
        Type anInt = IntType.singleton;
        VarExpr a = new VarExpr("a");
        VarExpr b = new VarExpr("b");
        BinOpExpr add = new BinOpExpr(Op.ADD, a, b);
        FunctionDeclExpr decl = new FunctionDeclExpr(new String[] {"a", "b"}, twoInts, anInt, add);

        ValueExpr one = new ValueExpr(new IntVal(1));
        ValueExpr two = new ValueExpr(new IntVal(2));
        Expression[] args = new Expression[] {one, two};
        FunctionAppExpr app = new FunctionAppExpr(decl, args);

        app.typecheck(new TypeEnvironment());
    }

    @Test
    // fn(): () { () }
    public void testNoParameters() {
        Type[] noParams = new Type[] {};
        Type noReturn = UnitType.singleton;
        ValueExpr unit = new ValueExpr(UnitVal.singleton);
        FunctionDeclExpr decl = new FunctionDeclExpr(new String[] {}, noParams, noReturn, unit);

        Expression[] noArgs = new Expression[] {};
        FunctionAppExpr app = new FunctionAppExpr(decl, noArgs);

        app.typecheck(new TypeEnvironment());
    }
}
