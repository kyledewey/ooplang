package ooplang.parser;

import java.util.List;
import java.util.Optional;

public class CallExp implements Exp {
    public final Exp target;
    public Optional<ClassType> targetType; // needed for codegen
    public final MethodName methodName;
    public final List<Exp> exps;

    public CallExp(final Exp target,
                   final MethodName methodName,
                   final List<Exp> exps) {
        this.target = target;
        targetType = Optional.empty();
        this.methodName = methodName;
        this.exps = exps;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof CallExp) {
            final CallExp asCall = (CallExp)other;
            return (target.equals(asCall.target) &&
                    methodName.equals(asCall.methodName) &&
                    exps.equals(asCall.exps));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (target.hashCode() +
                methodName.hashCode() +
                exps.hashCode());
    }
}
