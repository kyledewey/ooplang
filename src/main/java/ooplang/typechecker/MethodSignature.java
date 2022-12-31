package ooplang.typechecker;

import ooplang.parser.*;

import java.util.List;
import java.util.ArrayList;

public class MethodSignature {
    public final MethodName methodName;
    public final List<Type> params;

    public MethodSignature(final MethodName methodName,
                           final List<Type> params) {
        this.methodName = methodName;
        this.params = params;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof MethodSignature) {
            final MethodSignature asSig = (MethodSignature)other;
            return (methodName.equals(asSig.methodName) &&
                    params.equals(asSig.params));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return methodName.hashCode() + params.hashCode();
    }

    @Override
    public String toString() {
        return ("MethodSignature(" +
                methodName.toString() + ", " +
                params.toString() + ")");
    }

    public static MethodSignature getSignature(final MethodDef methodDef) {
        final List<Type> params = new ArrayList<Type>();
        for (final Param param : methodDef.params) {
            params.add(param.type);
        }
        return new MethodSignature(methodDef.methodName, params);
    }
}
