package edu.sjsu.fwjs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        assertEquals(new UnitVal(), e.evaluate(env));
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
        } catch (Exception e) {}
    }
    
    @Test
    public void testAssignExpr() {
        Environment env = new Environment();
        IntVal inVal = new IntVal(42);
        AssignExpr ae = new AssignExpr("x", new ValueExpr(inVal));
        IntVal iv = (IntVal) ae.evaluate(env);
        assertEquals(inVal, iv);
        assertEquals(inVal, env.resolveVar("x"));
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
        SeqExpr se = new SeqExpr(new AssignExpr("x", new ValueExpr(new IntVal(2))),
                new BinOpExpr(Op.MULTIPLY,
                        new VarExpr("x"),
                        new ValueExpr(new IntVal(3))));
        assertEquals(new IntVal(6), se.evaluate(env));
    }
    
    @Test
    public void testWhileExpr() {
        Environment env = new Environment();
        env.updateVar("x", new IntVal(10));
        WhileExpr we = new WhileExpr(new BinOpExpr(Op.GT,
                    new VarExpr("x"),
                    new ValueExpr(new IntVal(0))),
                new AssignExpr("x",
                        new BinOpExpr(Op.SUBTRACT,
                                new VarExpr("x"),
                                new ValueExpr(new IntVal(1)))));
        we.evaluate(env);
        assertEquals(new IntVal(0), env.resolveVar("x"));
    }
    
    @Test
    // (function(x) { x; })(321);
    public void testIdFunction() {
        Environment env = new Environment();
        List<String> params = new ArrayList<String>();
        params.add("x");
        FunctionDeclExpr f = new FunctionDeclExpr(params, new VarExpr("x"));
        List<Expression> args = new ArrayList<Expression>();
        args.add(new ValueExpr(new IntVal(321)));
        FunctionAppExpr app = new FunctionAppExpr(f,args);
        assertEquals(new IntVal(321), app.evaluate(env));
    }
    
    @Test
    // (function(x,y) { x / y; })(8,2);
    public void testDivFunction() {
        Environment env = new Environment();
        List<String> params = new ArrayList<String>();
        params.add("x");
        params.add("y");
        FunctionDeclExpr f = new FunctionDeclExpr(params,
                new BinOpExpr(Op.DIVIDE,
                        new VarExpr("x"),
                        new VarExpr("y")));
        List<Expression> args = new ArrayList<Expression>();
        args.add(new ValueExpr(new IntVal(8)));
        args.add(new ValueExpr(new IntVal(2)));
        FunctionAppExpr app = new FunctionAppExpr(f,args);
        assertEquals(new IntVal(4), app.evaluate(env));
    }
    
    @Test
    // x=112358; (function() { x; })();
    public void testOuterScope() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new VarExpr("x"));
        SeqExpr seq = new SeqExpr(newVar, new FunctionAppExpr(f, new ArrayList<Expression>()));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(112358), v);
    }
    
    @Test
    // x=112358; (function() { var x=42; x; })();
    public void testScope() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new SeqExpr(new VarDeclExpr("x", new ValueExpr(new IntVal(42))),
                        new VarExpr("x")));
        SeqExpr seq = new SeqExpr(newVar, new FunctionAppExpr(f, new ArrayList<Expression>()));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(42), v);
    }
    
    @Test
    // x=112358; (function() { var x=42; x; })(); x;
    public void testScope2() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new SeqExpr(new VarDeclExpr("x", new ValueExpr(new IntVal(42))),
                        new VarExpr("x")));
        SeqExpr seq = new SeqExpr(new SeqExpr(newVar,
                new FunctionAppExpr(f, new ArrayList<Expression>())),
                new VarExpr("x"));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(112358), v);
    }
    
    @Test
    // x=112358; (function() { x=42; x; })(); x;
    public void testScope3() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new SeqExpr(new AssignExpr("x", new ValueExpr(new IntVal(42))),
                        new VarExpr("x")));
        SeqExpr seq = new SeqExpr(new SeqExpr(newVar,
                new FunctionAppExpr(f, new ArrayList<Expression>())),
                new VarExpr("x"));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(42), v);
    }
    
    @Test
    // var x=99; var x=99;  /* should throw an error */
    public void testVarDecl() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(99)));
        try {
            (new SeqExpr(newVar, newVar)).evaluate(env);
            fail();
        } catch (Exception e) {}
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

