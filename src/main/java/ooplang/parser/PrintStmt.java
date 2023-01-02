package ooplang.parser;

import java.util.Optional;

public class PrintStmt implements Stmt {
    public final Exp exp;
    public Optional<Type> expType; // needed for codegen
    
    public PrintStmt(final Exp exp) {
        this.exp = exp;
        expType = Optional.empty();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof PrintStmt) {
            final PrintStmt asPrint = (PrintStmt)other;
            return (exp.equals(asPrint.exp) &&
                    expType.equals(asPrint.expType));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return exp.hashCode() + expType.hashCode();
    }

    @Override
    public String toString() {
        return ("PrintStmt(" +
                exp.toString() + ", " +
                expType.toString() + ")");
    }
}
