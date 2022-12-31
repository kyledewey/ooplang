package ooplang.parser;

public class AssignStmt implements Stmt {
    public final Lhs lhs;
    public final Exp exp;

    public AssignStmt(final Lhs lhs,
                      final Exp exp) {
        this.lhs = lhs;
        this.exp = exp;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AssignStmt) {
            final AssignStmt asAssign = (AssignStmt)other;
            return (lhs.equals(asAssign.lhs) &&
                    exp.equals(asAssign.exp));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return lhs.hashCode() + exp.hashCode();
    }

    @Override
    public String toString() {
        return ("AssignStmt(" +
                lhs.toString() + ", " +
                exp.toString() + ")");
    }
}
