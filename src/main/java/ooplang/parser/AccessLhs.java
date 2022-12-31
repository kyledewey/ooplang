package ooplang.parser;

public class AccessLhs implements Lhs {
    public final Lhs lhs;
    public final Variable variable;

    public AccessLhs(final Lhs lhs,
                     final Variable variable) {
        this.lhs = lhs;
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AccessLhs) {
            final AccessLhs asAccess = (AccessLhs)other;
            return (lhs.equals(asAccess.lhs) &&
                    variable.equals(asAccess.variable));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return lhs.hashCode() + variable.hashCode();
    }

    @Override
    public String toString() {
        return ("AccessLhs(" +
                lhs.toString() + ", " +
                variable.toString() + ")");
    }
}
