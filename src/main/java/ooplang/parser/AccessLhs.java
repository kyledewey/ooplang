package ooplang.parser;

import java.util.Optional;

public class AccessLhs implements Lhs {
    public final Lhs lhs;
    public Optional<ClassType> lhsType; // needed for codegen
    public final Variable variable;

    public AccessLhs(final Lhs lhs,
                     final Variable variable) {
        this.lhs = lhs;
        lhsType = Optional.empty();
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AccessLhs) {
            final AccessLhs asAccess = (AccessLhs)other;
            return (lhs.equals(asAccess.lhs) &&
                    lhsType.equals(asAccess.lhsType) &&
                    variable.equals(asAccess.variable));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (lhs.hashCode() +
                lhsType.hashCode() +
                variable.hashCode());
    }

    @Override
    public String toString() {
        return ("AccessLhs(" +
                lhs.toString() + ", " +
                lhsType.toString() + ", " +
                variable.toString() + ")");
    }
}
