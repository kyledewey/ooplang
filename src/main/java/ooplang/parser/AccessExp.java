package ooplang.parser;

public class AccessExp implements Exp {
    public final Exp exp;
    public final Variable variable;

    public AccessExp(final Exp exp,
                     final Variable variable) {
        this.exp = exp;
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AccessExp) {
            final AccessExp asAccess = (AccessExp)other;
            return (exp.equals(asAccess.exp) &&
                    variable.equals(asAccess.variable));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return exp.hashCode() + variable.hashCode();
    }

    @Override
    public String toString() {
        return ("AccessExp(" +
                exp.toString() + ", " +
                variable.toString() + ")");
    }
}
