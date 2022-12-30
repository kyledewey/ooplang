package ooplang.parser;

import java.util.List;

public class ClassDef {
    public final ClassName className;
    public final ConsDef consDef;
    public final List<MethodDef> methodDefs;

    public ClassDef(final ClassName className,
                    final ConsDef consDef,
                    final List<MethodDef> methodDefs) {
        this.className = className;
        this.consDef = consDef;
        this.methodDefs = methodDefs;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ClassDef) {
            final ClassDef asDef = (ClassDef)other;
            return (className.equals(asDef.className) &&
                    consDef.equals(asDef.consDef) &&
                    methodDefs.equals(asDef.methodDefs));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (className.hashCode() +
                consDef.hashCode() +
                methodDefs.hashCode());
    }

    @Override
    public String toString() {
        return ("ClassDef(" +
                className.toString() + ", " +
                consDef.toString() + ", " +
                methodDefs.toString() + ")");
    }
}
