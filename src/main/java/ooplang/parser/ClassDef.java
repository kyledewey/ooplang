package ooplang.parser;

import java.util.List;
import java.util.Optional;

public class ClassDef {
    public final ClassName className;
    public final Optional<ClassName> extendsName;
    public final ConsDef consDef;
    public final List<MethodDef> methodDefs;

    public ClassDef(final ClassName className,
                    final Optional<ClassName> extendsName,
                    final ConsDef consDef,
                    final List<MethodDef> methodDefs) {
        this.className = className;
        this.extendsName = extendsName;
        this.consDef = consDef;
        this.methodDefs = methodDefs;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ClassDef) {
            final ClassDef asDef = (ClassDef)other;
            return (className.equals(asDef.className) &&
                    extendsName.equals(asDef.extendsName) &&
                    consDef.equals(asDef.consDef) &&
                    methodDefs.equals(asDef.methodDefs));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (className.hashCode() +
                extendsName.hashCode() +
                consDef.hashCode() +
                methodDefs.hashCode());
    }

    @Override
    public String toString() {
        return ("ClassDef(" +
                className.toString() + ", " +
                extendsName.toString() + ", " +
                consDef.toString() + ", " +
                methodDefs.toString() + ")");
    }
}
