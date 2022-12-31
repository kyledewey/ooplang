package ooplang.parser;

public class AccessThisLhs implements Lhs {
    public final Variable variable;
    public AccessThisLhs(final Variable variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof AccessThisLhs &&
                variable.equals(((AccessThisLhs)other).variable));
    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }

    @Override
    public String toString() {
        return "AccessThisLhs(" + variable.toString() + ")";
    }
}
