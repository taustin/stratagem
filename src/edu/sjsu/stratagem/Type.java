package edu.sjsu.stratagem;

/**
 * Types in Stratagem.
 * Typechecking a Stratagem expression should return a Stratagem type.
 */
public interface Type {
    boolean consistentWith(Type other);
    Type findSupertypeWith(Type other);
}

// NOTE: Using package access so that all implementations of Type
// can be included in the same file.

/**
 * Dynamic any type.
 */
class AnyType implements Type {
    public static final AnyType singleton = new AnyType();

    public boolean consistentWith(Type other) {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AnyType;
    }

    public Type findSupertypeWith(Type other) {
        return this;
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
        return equals(other) || other instanceof AnyType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BoolType;
    }

    public Type findSupertypeWith(Type other) {
        return other instanceof BoolType ? this
                                         : AnyType.singleton;
    }

    @Override
    public String toString() {
        return "Bool";
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

        // The below is like equals() but instead calls .consistentWith() on arg and ret.
        if (!(other instanceof ClosureType)) {
            return false;
        }

        ClosureType that = (ClosureType)other;

        return arg.consistentWith(that.arg) && ret.consistentWith(that.ret);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ClosureType)) {
            return false;
        }

        ClosureType that = (ClosureType)other;

        return arg.equals(that.arg) && ret.equals(that.ret);
    }

    public Type findSupertypeWith(Type other) {
        if (!(other instanceof ClosureType)) {
            return AnyType.singleton;
        }
        ClosureType that = (ClosureType)other;
        return new ClosureType(
                arg.findSupertypeWith(that.arg),
                ret.findSupertypeWith(that.ret));
    }

    @Override
    public String toString() {
        return arg + " -> " + ret;
    }
}

/**
 * Numbers. Only integers are supported.
 */
class IntType implements Type {
    public static final IntType singleton = new IntType();

    public boolean consistentWith(Type other) {
        return equals(other) || other instanceof AnyType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IntType;
    }

    public Type findSupertypeWith(Type other) {
        return other instanceof IntType ? this
                                        : AnyType.singleton;
    }

    @Override
    public String toString() {
        return "Int";
    }
}

/**
 * References.
 */
class RefType implements Type {
    private Type cell;

    public RefType(Type cell) {
        this.cell = cell;
    }

    public Type getCellType() {
        return cell;
    }

    public boolean consistentWith(Type other) {
        // “Reference types are invariant with respect to consistency.”
        //   -- Gradual Typing for Functional Languages, p. 85
        //
        // Two reference types are not consistent just because their cell types are consistent. Instead, for references
        // to be consistent, their cell types must follow the stronger requirement of being equal. Otherwise, we could
        // violate type safety with the following program:
        //
        //   let r1 = ref (fn(x) { x }) in
        //   let r2 : ref ? = r1 in
        //     r2 ← 1;
        //     !r1(2)
        //
        // The application of 2 to r1 fails because r1, which has the type of a reference to function, points (unsafely)
        // to a cell with an integer.
        return equals(other) || other instanceof AnyType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RefType)) {
            return false;
        }

        RefType that = (RefType)other;

        return cell.equals(that.cell);
    }

    public Type findSupertypeWith(Type other) {
        // We cannot use the consistent-with relationship here or we would violate type safety, for example:
        //
        //   let r1 = ref (fn(x) { x }) in
        //   let r2 : Ref ? = if (true) { r1 } else { ref unit } in
        //     r2 ← 1;
        //     !r1(2)
        //
        // See also consistentWith() above.
        return equals(other) ? this
                             : AnyType.singleton;
    }

    @Override
    public String toString() {
        return "Ref " + cell.toString();
    }
}

/**
 * Strings.
 */
class StringType implements Type {
    public static final StringType singleton = new StringType();

    public boolean consistentWith(Type other) {
        return equals(other) || other instanceof AnyType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StringType;
    }

    public Type findSupertypeWith(Type other) {
        return other instanceof StringType ? this
                                           : AnyType.singleton;
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
        return equals(other) || other instanceof AnyType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UnitType;
    }

    public Type findSupertypeWith(Type other) {
        return other instanceof UnitType ? this
                                         : AnyType.singleton;
    }

    @Override
    public String toString() {
        return "Unit";
    }
}
