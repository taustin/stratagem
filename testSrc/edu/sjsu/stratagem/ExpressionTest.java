package edu.sjsu.stratagem;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ExpressionTest {

    @Test
    public void testValueExpr() {
        Environment env = new Environment();
        ValueExpr ve = new ValueExpr(new IntVal(3));
        IntVal i = (IntVal) ve.evaluate(env);
        assertEquals(3, i.toInt());
    }

    @Test
    public void testVarExpr() {
        Environment env = new Environment();
        Value v = new IntVal(3);
        env.updateVar("x", v);
        Expression e = new VarExpr("x");
        assertEquals(v, e.evaluate(env));
    }

    @Test
    public void testVarNotFoundExpr() {
        Environment env = new Environment();
        Value v = new IntVal(3);
        env.updateVar("x", v);
        Expression e = new VarExpr("y");
        assertEquals(UnitVal.singleton, e.evaluate(env));
    }

    @Test
    public void testIfTrueExpr() {
        Environment env = new Environment();
        IfExpr ife = new IfExpr(new ValueExpr(new BoolVal(true)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) ife.evaluate(env);
        assertEquals(1, iv.toInt());
    }

    @Test
    public void testIfFalseExpr() {
        Environment env = new Environment();
        IfExpr ife = new IfExpr(new ValueExpr(new BoolVal(false)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) ife.evaluate(env);
        assertEquals(2, iv.toInt());
    }

    @Test
    public void testBadIfExpr() {
        Environment env = new Environment();
        IfExpr ife = new IfExpr(new ValueExpr(new IntVal(0)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        try {
            ife.evaluate(env);
            fail();
        } catch (Exception ignored) {}
    }

    @Test
    public void testBinOpExpr() {
        Environment env = new Environment();
        BinOpExpr boe = new BinOpExpr(Op.ADD,
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) boe.evaluate(env);
        assertEquals(new IntVal(3), iv);
    }

    @Test
    public void testSeqExpr() {
        Environment env = new Environment();
        SeqExpr se = new SeqExpr(new Expression[] {
                new ValueExpr(new IntVal(2)),
                new BinOpExpr(Op.MULTIPLY,
                        new ValueExpr(new IntVal(2)),
                        new ValueExpr(new IntVal(3)))
        });
        assertEquals(new IntVal(6), se.evaluate(env));
    }

    @Test
    // (function(x) { x; })(321);
    public void testIdFunction() {
        Environment env = new Environment();
        List<String> params = new ArrayList<>();
        params.add("x");
        FunctionDeclExpr f = new FunctionDeclExpr(params, new VarExpr("x"));
        List<Expression> args = new ArrayList<>();
        args.add(new ValueExpr(new IntVal(321)));
        FunctionAppExpr app = new FunctionAppExpr(f,args);
        assertEquals(new IntVal(321), app.evaluate(env));
    }

    @Test
    // (function(x,y) { x / y; })(8,2);
    public void testDivFunction() {
        Environment env = new Environment();
        List<String> params = new ArrayList<>();
        params.add("x");
        params.add("y");
        FunctionDeclExpr f = new FunctionDeclExpr(params,
                new BinOpExpr(Op.DIVIDE,
                        new VarExpr("x"),
                        new VarExpr("y")));
        List<Expression> args = new ArrayList<>();
        args.add(new ValueExpr(new IntVal(8)));
        args.add(new ValueExpr(new IntVal(2)));
        FunctionAppExpr app = new FunctionAppExpr(f,args);
        assertEquals(new IntVal(4), app.evaluate(env));
    }

    @Test
    // fn(name: String) { fn(unused: String) { name }("Bob") }("Alice")
    public void testScope1() {
        StringVal alice = new StringVal("Alice");
        StringVal bob = new StringVal("Bob");

        Environment env = new Environment();
        FunctionDeclExpr innerDecl = new FunctionDeclExpr(
                new String[] {"unused"},
                new VarExpr("name"));
        FunctionDeclExpr outerDecl = new FunctionDeclExpr(
                new String[] {"name"},
                new FunctionAppExpr(innerDecl, new Expression[] { new ValueExpr(bob) }));

        FunctionAppExpr outerApp = new FunctionAppExpr(
                outerDecl,
                new Expression[] { new ValueExpr(alice) });
        Value v = outerApp.evaluate(env);
        assertEquals(v, alice);
    }

    @Test
    // fn(name: String) { fn(name: String) { name }("Bob") }("Alice")
    public void testScope2() {
        StringVal alice = new StringVal("Alice");
        StringVal bob = new StringVal("Bob");

        Environment env = new Environment();
        FunctionDeclExpr innerDecl = new FunctionDeclExpr(
                new String[] {"name"},
                new VarExpr("name"));
        FunctionDeclExpr outerDecl = new FunctionDeclExpr(
                new String[] {"name"},
                new FunctionAppExpr(innerDecl, new Expression[] { new ValueExpr(bob) }));

        FunctionAppExpr outerApp = new FunctionAppExpr(
                outerDecl,
                new Expression[] { new ValueExpr(alice) });
        Value v = outerApp.evaluate(env);
        assertEquals(v, bob);
    }

    @Test
    // fn(name: String) { fn(name: String) { name }("Bob"); name }("Alice")
    public void testScope3() {
        StringVal alice = new StringVal("Alice");
        StringVal bob = new StringVal("Bob");

        Environment env = new Environment();
        FunctionDeclExpr innerDecl = new FunctionDeclExpr(
                new String[] {"name"},
                new VarExpr("name"));
        FunctionDeclExpr outerDecl = new FunctionDeclExpr(
                new String[] {"name"},
                new SeqExpr(new Expression[] {
                        new FunctionAppExpr(innerDecl, new Expression[] { new ValueExpr(bob) }),
                        new VarExpr("name")
                }));

        FunctionAppExpr outerApp = new FunctionAppExpr(
                outerDecl,
                new Expression[] { new ValueExpr(alice) });
        Value v = outerApp.evaluate(env);
        assertEquals(v, alice);
    }

    @Test
    // "no" + "table"
    public void testStringAppend() {
        Environment env = new Environment();
        Expression s1 = new ValueExpr(new StringVal("no"));
        Expression s2 = new ValueExpr(new StringVal("table"));
        Expression exp = new BinOpExpr(Op.ADD, s1, s2);
        assertEquals(new StringVal("notable"), exp.evaluate(env));
    }
}
