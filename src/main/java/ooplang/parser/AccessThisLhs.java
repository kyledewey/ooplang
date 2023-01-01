package ooplang.parser;

import java.util.Optional;

public class AccessThisLhs implements Lhs {
    public final Variable variable;
    public Optional<ClassType> targetType; // needed for codegen
    
    public AccessThisLhs(final Variable variable) {
        this.variable = variable;
        targetType = Optional.empty();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AccessThisLhs) {
            final AccessThisLhs asAccess = (AccessThisLhs)other;
            return (variable.equals(asAccess.variable) &&
                    targetType.equals(asAccess.targetType));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return variable.hashCode() + targetType.hashCode();
    }

    @Override
    public String toString() {
        return ("AccessThisLhs(" +
                variable.toString() + ", " +
                targetType.toString() + ")");
    }
}
