package ooplang.typechecker;

import ooplang.parser.ClassDef;
import ooplang.parser.Variable;
import ooplang.parser.MethodDef;
import ooplang.parser.ClassName;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SingleClassInformation {
    public final ClassDef classDef;
    public final Map<MethodSignature, MethodInformation> methods;
    public final Map<Variable, InstanceVariableInformation> instanceVariables;

    public SingleClassInformation(final ClassDef classDef,
                                  final Map<MethodSignature, MethodInformation> methods,
                                  final Map<Variable, InstanceVariableInformation> instanceVariables) {
        this.classDef = classDef;
        this.methods = methods;
        this.instanceVariables = instanceVariables;
    }

    // Gets all the methods that are newly introduced
    // on this class.  Overridden methods do not count for this.
    public List<MethodInformation> introducedMethods() {
        final List<MethodInformation> retval = new ArrayList<MethodInformation>();
        for (final MethodInformation method : methods.values()) {
            if (method.originallyDefinedOn(classDef)) {
                retval.add(method);
            }
        }
        return retval;
    } // introducedMethods
                
    @Override
    public boolean equals(final Object other) {
        if (other instanceof SingleClassInformation) {
            final SingleClassInformation asInfo = (SingleClassInformation)other;
            return (classDef.equals(asInfo.classDef) &&
                    methods.equals(asInfo.methods) &&
                    instanceVariables.equals(asInfo.instanceVariables));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (classDef.hashCode() +
                methods.hashCode() +
                instanceVariables.hashCode());
    }

    @Override
    public String toString() {
        return ("SingleClassInformation(" +
                classDef.toString() + ", " +
                methods.toString() + ", " +
                instanceVariables.toString() + ")");
    }
} // SingleClassInformation
