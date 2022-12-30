package ooplang.parser;

public class WhileStmt implements Stmt {
    public final Exp exp;
    public final Stmt stmt;

    public WhileStmt(final Exp exp,
                     final Stmt stmt) {
        this.exp = exp;
        this.stmt = stmt;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof WhileStmt) {
            final WhileStmt asWhile = (WhileStmt)other;
            return (exp.equals(asWhile.exp) &&
                    stmt.equals(asWhile.stmt));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return exp.hashCode() + stmt.hashCode();
    }

    @Override
    public String toString() {
        return ("WhileStmt(" +
                exp.toString() + ", " +
                stmt.toString() + ")");
    }
}
