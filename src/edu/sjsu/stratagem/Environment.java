package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemException;

import java.util.Map;
import java.util.HashMap;

/**
 * A variable environment for the program. During typechecking of a program V
 * will be Type, and during evaluation of a program V will be Value.
 */
public class Environment<V> {
    private Map<String,V> env = new HashMap<>();
    private Environment<V> outerEnv;

    /**
     * Constructor for global environment
     */
    public Environment() {}

    /**
     * Constructor for local environment of a function
     */
    public Environment(Environment<V> outerEnv) {
        this.outerEnv = outerEnv;
    }

    /**
     * Handles the logic of resolving a variable.
     * If the variable name is in the current scope, it is returned.
     * Otherwise, search for the variable in the outer scope.
     * If we are at the outermost scope (AKA the global scope)
     * null is returned (similar to how JS returns undefined).
     */
    public V resolveVar(String varName) {
        if (env.containsKey(varName)) {
            return env.get(varName);
        } else if (outerEnv == null) {
            throw new StratagemException("Unbound variable: " + varName);
        } else {
            return outerEnv.resolveVar(varName);
        }
    }

    /**
     * Used for updating existing variables.
     * If a variable has not been defined previously in the current scope,
     * or any of the function's outer scopes, the var is stored in the global scope.
     */
    public void updateVar(String key, V v) {
        if (env.containsKey(key) || outerEnv == null) {
            env.put(key, v);
        } else {
            outerEnv.updateVar(key,v);
        }
    }

    /**
     * Creates a new variable in the local scope.
     * If the variable has been defined in the current scope previously,
     * a StratagemException is thrown.
     */
    public void createVar(String key, V v) {
        if (env.containsKey(key)) {
            throw new StratagemException("Redeclaring existing var " + key);
        }
        env.put(key,v);
    }
}
