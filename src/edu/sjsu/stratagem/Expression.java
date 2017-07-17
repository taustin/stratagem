package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemRuntimeException;
import edu.sjsu.stratagem.exception.StratagemTypecheckException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stratagem expressions.
 */
public interface Expression {
    /**
     * Determines the value type that evaluation of the expression will result in.
     * Throws an exception if the expression is not well-typed.
     */
    Type typecheck(TypeEnvironment env);

    /**
     * Evaluate the expression in the context of the specified environment.
     */
    Value evaluate(ValueEnvironment env);
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
    BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    public Type typecheck(TypeEnvironment env) {
        Type t1 = e1.typecheck(env);
        Type t2 = e2.typecheck(env);

        switch (op) {
        case EQ:
        case NE:
            if (!t1.equals(t2)) {
                throw new StratagemTypecheckException(
                        "Binary operator expected identical types, got: " + t1 + " and " + t2);
            }
            return BoolType.singleton;
        default:
            if (t1 != IntType.singleton || t2 != IntType.singleton) {
                throw new StratagemTypecheckException(
                        "Binary operator expected integer arguments, got: " + t1 + " and " + t2);
            }
            return IntType.singleton;
        }
    }

    @SuppressWarnings("incomplete-switch")
    public Value evaluate(ValueEnvironment env) {
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
        if (!(v1 instanceof IntVal && v2 instanceof IntVal)) {
            throw new StratagemRuntimeException("Expected ints, but got " + v1 + " and " + v2);
        }
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

        throw new StratagemRuntimeException("Unrecognized operator: " + op);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private static final Expression[] expressionArrayHint = new Expression[0];

    private Expression f;
    private Expression[] args;

    FunctionAppExpr(Expression f, Expression[] args) {
        this.f = f;
        this.args = args;
    }

    FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args.toArray(expressionArrayHint);
    }

    public Type typecheck(TypeEnvironment env) {
        Type fType = f.typecheck(env);

        if (!(fType instanceof ClosureType)) {
            throw new StratagemTypecheckException("Not a function: " + fType.toString());
        }
        ClosureType closureType = (ClosureType) fType;

        Type[] argTypes = new Type[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].typecheck(env);
        }

        Type[] closureArgTypes = closureType.getArgTypes();
        if (!Arrays.equals(closureArgTypes, argTypes)) {
            throw new StratagemTypecheckException(
                    "Incorrect argument types, expected: " + Arrays.toString(closureArgTypes) +
                                                 ", got: " + Arrays.toString(argTypes));
        }

        return closureType.getReturnType();
    }

    public Value evaluate(ValueEnvironment env) {
        ClosureVal closure = (ClosureVal) f.evaluate(env);
        List<Value> argVals = new ArrayList<>();
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
    private static final Type[] typeArrayHint = new Type[0];

    private String[] paramNames;
    private Type[] paramTypes;
    private Type returnType;  // Type that the programmer ascribed within the Stratagem code.
    private Expression body;

    FunctionDeclExpr(String[] paramNames, Type[] paramTypes, Type returnType, Expression body) {
        this.paramNames = paramNames;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.body = body;
    }

    FunctionDeclExpr(List<String> paramNames, List<Type> paramTypes, Type returnType, Expression body) {
        this.paramNames = paramNames.toArray(stringArrayHint);
        this.paramTypes = paramTypes.toArray(typeArrayHint);
        this.returnType = returnType;
        this.body = body;
    }

    public Type typecheck(TypeEnvironment outerEnv) {
        TypeEnvironment innerEnv = new TypeEnvironment(outerEnv);
        for (int i = 0; i < paramNames.length; i++) {
            innerEnv.createVar(paramNames[i], paramTypes[i]);
        }
        Type bodyT = body.typecheck(innerEnv);
        if (returnType == null) {
            // Infer the type for function body based on what we found.
            returnType = bodyT;
        } else {
            if (!bodyT.equals(returnType)) {
                throw new StratagemTypecheckException(
                        "Function's body doesn't have ascribed type, ascribed: " + returnType + ", had: " + bodyT);
            }
        }
        return new ClosureType(paramTypes, returnType);
    }

    public Value evaluate(ValueEnvironment env) {
        return new ClosureVal(paramNames, paramTypes, returnType, body, env);
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

    IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }

    public Type typecheck(TypeEnvironment env) {
        Type condT = cond.typecheck(env);
        Type thnT = thn.typecheck(env);
        Type elsT = els.typecheck(env);

        if (condT != BoolType.singleton) {
            throw new StratagemTypecheckException("If-expression expected boolean in condition, got: " + condT);
        }
        if (!thnT.equals(elsT)) {
            throw new StratagemTypecheckException("If-expression branches have unequal type: " + thnT + " and " + elsT);
        }
        return thnT;
    }

    public Value evaluate(ValueEnvironment env) {
        Value v = cond.evaluate(env);
        if (!(v instanceof BoolVal)) {
            throw new StratagemRuntimeException("Expected boolean, but got " + v);
        }
        BoolVal bv = (BoolVal) v;
        if (bv.toBoolean()) {
            return thn.evaluate(env);
        } else {
            return els.evaluate(env);
        }
    }
}

/**
 * Print expression. Hard to express in the type system, so we make it a language-level construct.
 */
class PrintExpr implements Expression {
    private Expression arg;

    PrintExpr(Expression arg) {
        this.arg = arg;
    }

    public Type typecheck(TypeEnvironment env) {
        return UnitType.singleton;
    }

    public Value evaluate(ValueEnvironment env) {
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

    SeqExpr(Expression[] exprs) {
        this.exprs = exprs;
    }

    SeqExpr(List<Expression> exprs) {
        this.exprs = exprs.toArray(expressionArrayHint);
    }

    public Type typecheck(TypeEnvironment env) {
        return exprs[exprs.length - 1].typecheck(env);
    }

    public Value evaluate(ValueEnvironment env) {
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

    ValueExpr(Value v) {
        this.val = v;
    }

    public Type typecheck(TypeEnvironment env) {
        return val.getType();
    }

    public Value evaluate(ValueEnvironment env) {
        return this.val;
    }
}

/**
 * Expressions that are a Stratagem variable.
 */
class VarExpr implements Expression {
    private String varName;

    VarExpr(String varName) {
        this.varName = varName;
    }

    public Type typecheck(TypeEnvironment env) {
        return env.resolveVar(varName);
    }

    public Value evaluate(ValueEnvironment env) {
        return env.resolveVar(varName);
    }
}
