package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemRuntimeException;

import java.util.Map;
import java.util.HashMap;

/**
 * A variable environment for the runtime of the program.
 */
public class ValueEnvironment {
    private Map<String,Value> env = new HashMap<>();
    private ValueEnvironment outerEnv;

    /**
     * Constructor for global environment
     */
    public ValueEnvironment() {}

    /**
     * Constructor for local environment of a function
     */
    public ValueEnvironment(ValueEnvironment outerEnv) {
        this.outerEnv = outerEnv;
    }

    /**
     * Handles the logic of resolving a variable.
     * If the variable name is in the current scope, it is returned.
     * Otherwise, search for the variable in the outer scope.
     * If we are at the outermost scope (AKA the global scope)
     * null is returned (similar to how JS returns undefined).
     */
    public Value resolveVar(String varName) {
        if (env.containsKey(varName)) {
            return env.get(varName);
        } else if (outerEnv == null) {
            throw new StratagemRuntimeException("Unbound variable: " + varName);
        } else {
            return outerEnv.resolveVar(varName);
        }
    }

    /**
     * Used for updating existing variables.
     * If a variable has not been defined previously in the current scope,
     * or any of the function's outer scopes, the var is stored in the global scope.
     */
    public void updateVar(String key, Value v) {
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
    public void createVar(String key, Value v) {
        if (env.containsKey(key)) {
            throw new StratagemRuntimeException("Redeclaring existing var " + key);
        }
        env.put(key,v);
    }
}
