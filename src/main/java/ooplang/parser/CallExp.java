package ooplang.parser;

import java.util.List;
import java.util.Optional;

public class CallExp implements Exp {
    public final Exp target;
    public Optional<ClassType> targetType; // needed for codegen
    public final MethodName methodName;
    public final List<Exp> exps;
    public Optional<List<Type>> expTypes; // needed for codegen
    
    public CallExp(final Exp target,
                   final MethodName methodName,
                   final List<Exp> exps) {
        this.target = target;
        targetType = Optional.empty();
        this.methodName = methodName;
        this.exps = exps;
        expTypes = Optional.empty();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof CallExp) {
            final CallExp asCall = (CallExp)other;
            return (target.equals(asCall.target) &&
                    targetType.equals(asCall.targetType) &&
                    methodName.equals(asCall.methodName) &&
                    exps.equals(asCall.exps) &&
                    expTypes.equals(asCall.expTypes));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (target.hashCode() +
                targetType.hashCode() +
                methodName.hashCode() +
                exps.hashCode() +
                expTypes.hashCode());
    }

    @Override
    public String toString() {
        return ("CallExp(" +
                target.toString() + ", " +
                targetType.toString() + ", " +
                methodName.toString() + ", " +
                exps.toString() + ", " +
                expTypes.toString() + ")");
    }
}
