package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemCastException;
import edu.sjsu.stratagem.exception.StratagemRuntimeException;
import edu.sjsu.stratagem.exception.StratagemTypecheckException;

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
            if (!t1.consistentWith(t2)) {
                throw new StratagemTypecheckException(
                        "Binary operator expected identical types, got: " + t1 + " and " + t2);
            }
            return BoolType.singleton;
        default:
            if (!t1.consistentWith(IntType.singleton) || !t2.consistentWith(IntType.singleton)) {
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
        }

        // Int operations case
        if (!(v1 instanceof IntVal && v2 instanceof IntVal)) {
            throw new StratagemCastException("Expected ints, but got " + v1 + " and " + v2);
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
 * Runtime cast from a type involving an Any to a concrete type.
 */
class CastExpr implements Expression {
    private Type target;
    private Expression body;

    CastExpr(Type target, Expression body) {
        this.target = target;
        this.body = body;
    }

    public Type typecheck(TypeEnvironment env) {
        // Continue typechecking the body, but discard its result here.
        body.typecheck(env);

        return target;
    }

    public Value evaluate(ValueEnvironment env) {
        Value v = body.evaluate(env);
        if (v.getType().consistentWith(target)) {
            return v;
        } else {
            throw new StratagemCastException(null);
        }
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression closureExpr;
    private Expression arg;

    FunctionAppExpr(Expression closureExpr, Expression arg) {
        this.closureExpr = closureExpr;
        this.arg = arg;
    }

    public Type typecheck(TypeEnvironment env) {
        // Typecheck the closureExpr and args under this application.
        Type closureType = closureExpr.typecheck(env);
        Type argType = arg.typecheck(env);

        // Make sure our closureExpr expression can result in a closure.
        if (!(closureType instanceof ClosureType) && closureType != AnyType.singleton) {
            throw new StratagemTypecheckException("Function called on a non-function: " + closureType);
        }

        // Cast insertion rule (CApp1).
        if (closureType == AnyType.singleton) {
            // Wrap the closureExpr in a cast to ensure it can take our argument at runtime.
            closureType = new ClosureType(argType, AnyType.singleton);
            closureExpr = new CastExpr(closureType, closureExpr);
        }

        // closureType is necessarily a ClosureType now. Great!
        ClosureType closureType_ = (ClosureType) closureType;
        Type closureArgType = closureType_.getArgType();
        Type closureReturnType = closureType_.getReturnType();

        // Cast insertion rule (CApp2).
        if (!closureArgType.equals(argType)) {
            if (!closureArgType.consistentWith(argType)) {
                throw new StratagemTypecheckException(
                        "Inconsistent argument type: expected " + closureArgType +
                                                  ", got "      + argType);
            }

            // Wrap the argument in a cast to ensure it can be given to our closureExpr at runtime.
            arg = new CastExpr(closureArgType, arg);
        }

        // Typing rule (TApp).
        return closureReturnType;
    }

    public Value evaluate(ValueEnvironment env) {
        ClosureVal closure = (ClosureVal) closureExpr.evaluate(env);
        Value argVal = arg.evaluate(env);
        return closure.apply(argVal);
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private String paramName;
    private Type paramType;
    private Type returnType;  // Type that the programmer ascribed within the Stratagem code.
    private Expression body;

    FunctionDeclExpr(String paramName, Type paramType, Type returnType, Expression body) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.returnType = returnType;
        this.body = body;
    }

    public Type typecheck(TypeEnvironment outerEnv) {
        TypeEnvironment innerEnv = new TypeEnvironment(outerEnv);
        innerEnv.createVar(paramName, paramType);
        Type bodyT = body.typecheck(innerEnv);
        if (returnType == null) {
            // Infer the type for function body based on what we found.
            returnType = bodyT;
        } else {
            if (!bodyT.consistentWith(returnType)) {
                throw new StratagemTypecheckException(
                        "Function's body doesn't have ascribed type, ascribed: " + returnType + ", had: " + bodyT);
            }
        }
        return new ClosureType(paramType, returnType);
    }

    public Value evaluate(ValueEnvironment env) {
        return new ClosureVal(paramName, paramType, returnType, body, env);
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

        // Make sure our condition expression can result in a boolean.
        if (condT != BoolType.singleton && condT != AnyType.singleton) {
            throw new StratagemTypecheckException("If-expression expected boolean in condition, got: " + condT);
        }

        // Cast insertion rule (CIf1).
        if (condT == AnyType.singleton) {
            // Wrap the condition expression in a cast to ensure it is a boolean at runtime.
            cond = new CastExpr(BoolType.singleton, cond);
        }

        // Find the lowest type that is a supertype of both the then-branch and the else-branch.
        Type supertype = thnT.findSupertypeWith(elsT);

        // Since it's a supertype of both, it should be consistent with the then-branch and the else-branch.
        assert(thnT.consistentWith(supertype));
        assert(elsT.consistentWith(supertype));

        // Cast insertion rule (CIf2).
        if (!thnT.equals(supertype)) {
            // Cast the left branch to the supertype of the left and right.
            thn = new CastExpr(supertype, thn);
        }

        // Cast insertion rule (CIf3).
        if (!elsT.equals(supertype)) {
            // Cast the right branch to the supertype of the left and right.
            els = new CastExpr(supertype, els);
        }

        // Typing rule (TIf).
        return supertype;
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
    public static final ValueExpr unitSingleton = new ValueExpr(UnitVal.singleton);
    public static final ValueExpr trueSingleton = new ValueExpr(BoolVal.trueSingleton);
    public static final ValueExpr falseSingleton = new ValueExpr(BoolVal.falseSingleton);

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
