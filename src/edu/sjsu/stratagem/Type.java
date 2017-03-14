package edu.sjsu.stratagem;

/**
 * Types in FWJS.
 * Evaluating a FWJS expression should return a FWJS value.
 */
public interface Type {}

//NOTE: Using package access so that all implementations of Type
//can be included in the same file.

/**
 * Boolean types.
 */
class BoolType implements Type {
    @Override
    public boolean equals(Object that) {
        return that instanceof BoolType;
    }
    @Override
    public String toString() {
        return "Bool";
    }
}

/**
 * Numbers. Only integers are supported.
 */
class IntType implements Type {
    @Override
    public boolean equals(Object that) {
        return that instanceof IntType;
    }
    @Override
    public String toString() {
        return "Int";
    }
}

/**
 * Strings.
 */
class StringType implements Type {
    @Override
    public boolean equals(Object that) {
        return that instanceof StringType;
    }
    @Override
    public String toString() {
        return "String";
    }
}

/**
 * Type for the Unit value.
 */
class UnitType implements Type {
    @Override
    public boolean equals(Object that) {
        return (that instanceof UnitVal);
    }
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
        return (that instanceof UnitVal);
    }
    @Override
    public String toString() {
        return arg + " -> " + ret;
    }
}
