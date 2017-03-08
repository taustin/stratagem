package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }
    
    @SuppressWarnings("incomplete-switch")
    public Value evaluate(Environment env) {
        Value v1 = e1.evaluate(env);
        Value v2 = e2.evaluate(env);
        
        // Special cases switch
        switch (op) {
        case EQ:
            return new BoolVal(v1.equals(v2));
        case NE:
            return new BoolVal(!v1.equals(v2));
        case ADD:
            // Specifically skipping cases where we have two numbers
            if (!(v1 instanceof IntVal && v2 instanceof IntVal)) {
                return new StringVal(v1.toString() + v2.toString());
            }
        }
        
        // Int operations case
        if (!(v1 instanceof IntVal && v2 instanceof IntVal))
            throw new RuntimeException("Expected ints, but got " + v1 + " and " + v2);
        int i = ((IntVal) v1).toInt();
        int j = ((IntVal) v2).toInt();
        switch(op) {
        case ADD:
            return new IntVal(i + j);
        case SUBTRACT:
            return new IntVal(i - j);
        case MULTIPLY:
            return new IntVal(i * j);
        case DIVIDE:
            return new IntVal(i / j);
        case MOD:
            return new IntVal(i % j);
        case GT:
            return new BoolVal(i > j);
        case GE:
            return new BoolVal(i >= j);
        case LT:
            return new BoolVal(i < j);
        case LE:
            return new BoolVal(i <= j);
        }
        
        throw new RuntimeException("Unrecognized operator: " + op);
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
    public Value evaluate(Environment env) {
        Value v = this.cond.evaluate(env);
        if (!(v instanceof BoolVal))
            throw new RuntimeException("Expected boolean, but got " + v);
        BoolVal bv = (BoolVal) v;
        if (bv.toBoolean()) {
            return this.thn.evaluate(env);
        } else {
            return this.els.evaluate(env);
        }
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        Value v = this.cond.evaluate(env);
        if (!(v instanceof BoolVal))
            throw new RuntimeException("Expected boolean, but got " + v);
        boolean b = ((BoolVal) v).toBoolean();
        if (b) {
            this.body.evaluate(env);
            return this.evaluate(env);
        } else {
            return new UnitVal();
        }
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
        e1.evaluate(env);
        return e2.evaluate(env);
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        env.createVar(varName, v);
        return v;
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
        Value v = e.evaluate(env);
        env.updateVar(varName, v);
        return v;
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        return new ClosureVal(this.params, this.body, env);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
        ClosureVal closure = (ClosureVal) f.evaluate(env);
        List<Value> argVals = new ArrayList<Value>();
        for (Expression arg : args) {
            argVals.add(arg.evaluate(env));
        }
        return closure.apply(argVals);
    }
}
