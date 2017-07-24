package edu.sjsu.stratagem;

import java.util.List;

/**
 * Values in Stratagem.
 * Evaluating a Stratagem expression should return a Stratagem value.
 */
public interface Value {
    Type getType();
}

//NOTE: Using package access so that all implementations of Value
//can be included in the same file.

/**
 * Boolean values.
 */
class BoolVal implements Value {
    private boolean boolVal;
    public BoolVal(boolean b) { this.boolVal = b; }
    public Type getType() {
        return BoolType.singleton;
    }
    public boolean toBoolean() { return this.boolVal; }
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof BoolVal)) return false;
        return this.boolVal == ((BoolVal) that).boolVal;
    }
    @Override
    public String toString() {
        return "" + this.boolVal;
    }
}

/**
 * A closure.
 * Note that a closure remembers its surrounding scope.
 */
class ClosureVal implements Value {
    private String paramName;
    private Type paramType;
    private Type returnType;
    private Expression body;
    private ValueEnvironment outerEnv;

    /**
     * The environment is the environment where the function was created.
     * This design is what makes this expression a closure.
     */
    public ClosureVal(String paramName, Type paramType, Type returnType, Expression body, ValueEnvironment outerEnv) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.returnType = returnType;
        this.body = body;
        this.outerEnv = outerEnv;
    }

    public Type getType() {
        return new ClosureType(paramType, returnType);
    }

    public String toString() {
        StringBuilder s = new StringBuilder("function(");
        String sep = "";
        s.append(sep).append(paramName).append(": ").append(paramType);
        s.append("): ").append(returnType).append(" {...}");

        return s.toString();
    }

    /**
     * To apply a closure, first create a new local environment, with an outer scope
     * of the environment where the function was created. Each parameter should
     * be bound to its matching argument and added to the new local environment.
     */
    public Value apply(Value argVal) {
        ValueEnvironment newEnv = new ValueEnvironment(outerEnv);
        newEnv.createVar(paramName, argVal);
        return body.evaluate(newEnv);
    }
}

/**
 * Numbers.  Only integers are supported.
 */
class IntVal implements Value {
    private int i;
    public IntVal(int i) { this.i = i; }
    public Type getType() {
        return IntType.singleton;
    }
    public int toInt() { return this.i; }
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof IntVal)) return false;
        return this.i == ((IntVal) that).i;
    }
    @Override
    public String toString() {
        return "" + this.i;
    }
}

/**
 * Strings.
 */
class StringVal implements Value {
    private String s;
    public StringVal(String s) { this.s = s; }
    public Type getType() {
        return StringType.singleton;
    }
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof StringVal)) return false;
        return this.s.equals(((StringVal)that).s);
    }
    @Override
    public String toString() {
        //return "'" + this.s + "'";
        return this.s;
    }
}

class UnitVal implements Value {
    public static final UnitVal singleton = new UnitVal();

    public Type getType() {
        return UnitType.singleton;
    }
    @Override
    public boolean equals(Object that) {
        return (that instanceof UnitVal);
    }
    @Override
    public String toString() {
        return "()";
    }
}
