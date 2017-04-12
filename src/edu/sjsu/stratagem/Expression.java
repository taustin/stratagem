package edu.sjsu.stratagem;

import java.util.ArrayList;
import java.util.List;

/**
 * Stratagem expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    Value evaluate(Environment<Value> env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

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
    public Value evaluate(Environment<Value> env) {
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
            throw new StratagemException("Expected ints, but got " + v1 + " and " + v2);
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

        throw new StratagemException("Unrecognized operator: " + op);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private static final Expression[] expressionArrayHint = new Expression[0];

    private Expression f;
    private Expression[] args;
    public FunctionAppExpr(Expression f, Expression[] args) {
        this.f = f;
        this.args = args;
    }
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args.toArray(expressionArrayHint);
    }
    public Value evaluate(Environment<Value> env) {
        ClosureVal closure = (ClosureVal) f.evaluate(env);
        List<Value> argVals = new ArrayList<Value>();
        for (Expression arg : args) {
            argVals.add(arg.evaluate(env));
        }
        return closure.apply(argVals);
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private static final String[] stringArrayHint = new String[0];

    private String[] params;
    private Expression body;
    public FunctionDeclExpr(String[] params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params.toArray(stringArrayHint);
        this.body = body;
    }
    public Value evaluate(Environment<Value> env) {
        return new ClosureVal(this.params, this.body, env);
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
    public Value evaluate(Environment<Value> env) {
        Value v = this.cond.evaluate(env);
        if (!(v instanceof BoolVal))
            throw new StratagemException("Expected boolean, but got " + v);
        BoolVal bv = (BoolVal) v;
        if (bv.toBoolean()) {
            return this.thn.evaluate(env);
        } else {
            return this.els.evaluate(env);
        }
    }
}

/**
 * Print expression. Hard to express in the type system, so we make it a language-level construct.
 */
class PrintExpr implements Expression {
    private Expression arg;

    public PrintExpr(Expression arg) {
        this.arg = arg;
    }

    public Value evaluate(Environment<Value> env) {
        Value value = arg.evaluate(env);
        print(value);
        return UnitVal.singleton;
    }

    private void print(Value value) {
        System.out.println(value.toString());
    }
}

/**
 * Sequence expressions (i.e. several back-to-back expressions).
 */
class SeqExpr implements Expression {
    private static final Expression[] expressionArrayHint = new Expression[0];

    private Expression[] exprs;
    public SeqExpr(Expression[] exprs) {
        this.exprs = exprs;
    }
    public SeqExpr(List<Expression> exprs) {
        this.exprs = exprs.toArray(expressionArrayHint);
    }
    public Value evaluate(Environment<Value> env) {
        Value value = UnitVal.singleton;
        for (Expression e : exprs) {
            value = e.evaluate(env);
        }
        return value;
    }
}

/**
 * Stratagem constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment<Value> env) {
        return this.val;
    }
}

/**
 * Expressions that are a Stratagem variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment<Value> env) {
        return env.resolveVar(varName);
    }
}
