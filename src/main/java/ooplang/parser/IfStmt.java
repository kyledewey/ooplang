package ooplang.parser;

public class IfStmt implements Stmt {
    public final Exp exp;
    public final Stmt trueBranch;
    public final Stmt falseBranch;

    public IfStmt(final Exp exp,
                  final Stmt trueBranch,
                  final Stmt falseBranch) {
        this.exp = exp;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof IfStmt) {
            final IfStmt asIf = (IfStmt)other;
            return (exp.equals(asIf.exp) &&
                    trueBranch.equals(asIf.trueBranch) &&
                    falseBranch.equals(asIf.falseBranch));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (exp.hashCode() +
                trueBranch.hashCode() +
                falseBranch.hashCode());
    }

    @Override
    public String toString() {
        return ("IfStmt(" +
                exp.toString() + ", " +
                trueBranch.toString() + ", " +
                falseBranch.toString() + ")");
    }
}
