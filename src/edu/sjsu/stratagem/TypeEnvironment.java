package edu.sjsu.stratagem;

import edu.sjsu.stratagem.exception.StratagemTypecheckException;

import java.util.HashMap;
import java.util.Map;

/**
 * A variable environment for typechecking the program.
 */
public class TypeEnvironment {
    private Map<String,Type> env = new HashMap<>();
    private TypeEnvironment outerEnv;

    /**
     * Constructor for global environment
     */
    public TypeEnvironment() {}

    /**
     * Constructor for local environment of a function
     */
    public TypeEnvironment(TypeEnvironment outerEnv) {
        this.outerEnv = outerEnv;
    }

    /**
     * Handles the logic of resolving a variable.
     * If the variable name is in the current scope, it is returned.
     * Otherwise, search for the variable in the outer scope.
     * If we are at the outermost scope (AKA the global scope)
     * null is returned (similar to how JS returns undefined).
     */
    public Type resolveVar(String varName) {
        if (env.containsKey(varName)) {
            return env.get(varName);
        } else if (outerEnv == null) {
            throw new StratagemTypecheckException("Unbound variable: " + varName);
        } else {
            return outerEnv.resolveVar(varName);
        }
    }

    /**
     * Used for updating existing variables.
     * If a variable has not been defined previously in the current scope,
     * or any of the function's outer scopes, the var is stored in the global scope.
     */
    public void updateVar(String key, Type v) {
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
    public void createVar(String key, Type v) {
        if (env.containsKey(key)) {
            throw new StratagemTypecheckException("Redeclaring existing var " + key);
        }
        env.put(key,v);
    }
}
