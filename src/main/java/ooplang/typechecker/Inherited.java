package ooplang.typechecker;

import java.util.Map;

// Tracks all the information that was inherited by a clas
public class Inherited {
    public final Map<MethodSignature, MethodDef> methods;
    public final Map<Variable, Type> instanceVariables;

    public Inherited(final Map<MethodSignature, MethodDef> methods,
                     final Map<Variable, Type> instanceVariables) {
        this.methods = methods;
        this.instanceVariables = instanceVariables;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Inherited) {
            final Inherited asInherited = (Inherited)other;
            return (methods.equals(asInherited.methods) &&
                    instanceVariables.equals(asInherited.instanceVariables));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return methods.hashCode() + instanceVariables.hashCode();
    }

    @Override
    public String toString() {
        return ("Inherited(" +
                methods.toString() + ", " +
                instanceVariables.toString() + ")");
    }
}
