package ooplang.parser;

import java.util.Optional;

public class AccessExp implements Exp {
    public final Exp exp;
    public Optional<ClassType> expType; // needed for codegen
    public final Variable variable;

    public AccessExp(final Exp exp,
                     final Variable variable) {
        this.exp = exp;
        expType = Optional.empty();
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AccessExp) {
            final AccessExp asAccess = (AccessExp)other;
            return (exp.equals(asAccess.exp) &&
                    expType.equals(asAccess.expType) &&
                    variable.equals(asAccess.variable));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (exp.hashCode() +
                expType.hashCode() +
                variable.hashCode());
    }

    @Override
    public String toString() {
        return ("AccessExp(" +
                exp.toString() + ", " +
                expType.toString() + ", " +
                variable.toString() + ")");
    }
}
