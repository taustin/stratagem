package edu.sjsu.stratagem;

class TestUtils {
    // Produce an value with type Any, as in:
    //   fn(x: ?): ? { x }(val)
    // which has type
    //   ?
    // and which evaluates to
    //   val
    static FunctionAppExpr makeAny(Value value) {
        return new FunctionAppExpr(id, new ValueExpr(value));
    }

    static FunctionAppExpr makeAny(int n) {
        return makeAny(new IntVal(n));
    }

    // The identity function:
    //   fn(x: ?): ? { x }
    static final FunctionDeclExpr id = new FunctionDeclExpr(
            "x",
            AnyType.singleton,
            AnyType.singleton,
            new VarExpr("x"));

    // The successor function:
    //   fn(n: Int): Int { n + 1 }
    static final FunctionDeclExpr succ = new FunctionDeclExpr(
            "n",
            IntType.singleton,
            IntType.singleton,
            new BinOpExpr(Op.ADD, new VarExpr("n"), new ValueExpr(new IntVal(1))));

    // Produces an expression of the type:
    //   arg -> ret
    static Expression makeTrivialFn(Type arg, Type ret) {
        Type fnType = new ClosureType(arg, ret);
        return makeTrivialExpression(fnType);
    }

    // Produces an expression of the type:
    //   arg -> ret1 -> ret2
    static Expression makeTrivialFn(Type arg, Type ret1, Type ret2) {
        Type fnType = new ClosureType(arg, new ClosureType(ret1, ret2));
        return makeTrivialExpression(fnType);
    }

    // Produces the simplest possible expression of the requested type.
    static Expression makeTrivialExpression(Type type) {
        if (type instanceof AnyType) {
            return makeAny(UnitVal.singleton);
        } else if (type instanceof BoolType) {
            return ValueExpr.trueSingleton;
        } else if (type instanceof ClosureType) {
            ClosureType closureType = (ClosureType) type;
            return new FunctionDeclExpr(
                    "x",
                    closureType.getArgType(),
                    closureType.getReturnType(),
                    makeTrivialExpression(closureType.getReturnType()));
        } else if (type instanceof IntType) {
            return new ValueExpr(new IntVal(0));
        } else if (type instanceof StringType) {
            return new ValueExpr(new StringVal(""));
        } else if (type instanceof UnitType) {
            return ValueExpr.unitSingleton;
        } else {
            throw new RuntimeException("I don't know how to make a value of that type yet!");
        }
    }
}
