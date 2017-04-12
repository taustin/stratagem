package edu.sjsu.stratagem;

/**
 * Types in Stratagem.
 * Typechecking a Stratagem expression should return a Stratagem type.
 */
public interface Type {}

//NOTE: Using package access so that all implementations of Type
//can be included in the same file.

/**
 * Boolean types.
 */
class BoolType implements Type {
    public static final BoolType singleton = new BoolType();

    @Override
    public String toString() {
        return "Bool";
    }
}

/**
 * Numbers. Only integers are supported.
 */
class IntType implements Type {
    public static final IntType singleton = new IntType();

    @Override
    public String toString() {
        return "Int";
    }
}

/**
 * Strings.
 */
class StringType implements Type {
    public static final StringType singleton = new StringType();

    @Override
    public String toString() {
        return "String";
    }
}

/**
 * Type for the Unit value.
 */
class UnitType implements Type {
    public static final UnitType singleton = new UnitType();

    @Override
    public String toString() {
        return "()";
    }
}

/**
 * A function's type. Functions take one argument and have one return value.
 */
class FunctionType implements Type {
    private Type arg;
    private Type ret;

    public FunctionType(Type arg, Type ret) {
        this.arg = arg;
        this.ret = ret;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof FunctionType)) {
            return false;
        }
        FunctionType other = (FunctionType) that;
        return arg.equals(other.arg) && ret.equals(other.ret);
    }

    @Override
    public String toString() {
        return arg + " -> " + ret;
    }
}
