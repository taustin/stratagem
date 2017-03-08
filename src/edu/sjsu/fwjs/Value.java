package edu.sjsu.fwjs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Values in FWJS.
 * Evaluating a FWJS expression should return a FWJS value.
 */
public interface Value {}

//NOTE: Using package access so that all implementations of Value
//can be included in the same file.

/**
 * Boolean values.
 */
class BoolVal implements Value {
    private boolean boolVal;
    public BoolVal(boolean b) { this.boolVal = b; }
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
 * Numbers.  Only integers are supported.
 */
class IntVal implements Value {
    private int i;
    public IntVal(int i) { this.i = i; }
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
 * A closure.
 * Note that a closure remembers its surrounding scope.
 */
class ClosureVal implements Value {
    private List<String> params;
    private Expression body;
    private Environment outerEnv;
    /**
     * The environment is the environment where the function was created.
     * This design is what makes this expression a closure.
     */
    public ClosureVal(List<String> params, Expression body, Environment env) {
        this.params = params;
        this.body = body;
        this.outerEnv = env;
    }
    public String toString() {
        String s = "function(";
        String sep = "";
        for (int i=0; i<params.size(); i++) {
            s += sep + params.get(i);
            sep = ",";
        }
        s += ") {...};";
        return s;
    }
    /**
     * To apply a closure, first create a new local environment, with an outer scope
     * of the environment where the function was created. Each parameter should
     * be bound to its matching argument and added to the new local environment.
     */
    public Value apply(List<Value> argVals) {
        Environment newEnv = new Environment(outerEnv);
        for (int i=0; i<argVals.size(); i++) {
            String varName = params.get(i);
            Value v = argVals.get(i);
            newEnv.createVar(varName, v);
        }
        return body.evaluate(newEnv);
    }
}
