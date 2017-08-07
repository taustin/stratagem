package edu.sjsu.stratagem;

/**
 * Types in Stratagem.
 * Typechecking a Stratagem expression should return a Stratagem type.
 */
public interface Type {
    boolean consistentWith(Type other);
}

//NOTE: Using package access so that all implementations of Type
//can be included in the same file.

/**
 * Dynamic any type.
 */
class AnyType implements Type {
    public static final AnyType singleton = new AnyType();

    public boolean consistentWith(Type other) {
        return true;
    }

    @Override
    public String toString() {
        return "?";
    }
}

/**
 * Boolean types.
 */
class BoolType implements Type {
    public static final BoolType singleton = new BoolType();

    public boolean consistentWith(Type other) {
        return other instanceof BoolType || other instanceof AnyType;
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
    public static final IntType singleton = new IntType();

    public boolean consistentWith(Type other) {
        return other instanceof IntType || other instanceof AnyType;
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
    public static final StringType singleton = new StringType();

    public boolean consistentWith(Type other) {
        return other instanceof StringType || other instanceof AnyType;
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
    public static final UnitType singleton = new UnitType();

    public boolean consistentWith(Type other) {
        return other instanceof UnitType || other instanceof AnyType;
    }

    @Override
    public String toString() {
        return "Unit";
    }
}

/**
 * A closure's type. Closures take one argument and have one return value.
 */
class ClosureType implements Type {
    private Type arg;
    private Type ret;

    public ClosureType(Type arg, Type ret) {
        this.arg = arg;
        this.ret = ret;
    }

    public Type getArgType() {
        return arg;
    }

    public Type getReturnType() {
        return ret;
    }

    public boolean consistentWith(Type other) {
        if (other instanceof AnyType) {
            return true;
        }
        if (!(other instanceof ClosureType)) {
            return false;
        }
        ClosureType that = (ClosureType)other;
        return arg.consistentWith(that.arg) && ret.consistentWith(that.ret);
    }

    @Override
    public String toString() {
        return arg + " -> " + ret;
    }
}
