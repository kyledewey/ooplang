package ooplang.parser;

public class ReturnStmt implements Stmt {
    public final Exp exp;

    public ReturnStmt(final Exp exp) {
        this.exp = exp;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ReturnStmt &&
                exp.equals(((ReturnStmt)other).exp));
    }

    @Override
    public int hashCode() {
        return exp.hashCode();
    }

    @Override
    public String toString() {
        return "ReturnStmt(" + exp.toString() + ")";
    }
}
