package edu.sjsu.stratagem;

import java.util.Arrays;

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
 * A closure's type. Closures take one argument and have one return value.
 */
class ClosureType implements Type {
    private Type[] args;
    private Type ret;

    public ClosureType(Type[] args, Type ret) {
        this.args = args;
        this.ret = ret;
    }

    public Type[] getArgTypes() {
        return args;
    }

    public Type getReturnType() {
        return ret;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof ClosureType)) {
            return false;
        }
        ClosureType other = (ClosureType) that;
        return Arrays.equals(args, other.args) && ret.equals(other.ret);
    }

    @Override
    public String toString() {
        return Arrays.toString(args) + " -> " + ret;
    }
}
