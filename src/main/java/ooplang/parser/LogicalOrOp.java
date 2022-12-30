package ooplang.parser;

public class LogicalOrOp implements Op {
    public String repr() { return "||"; }

    @Override
    public boolean equals(final Object other) {
        return other instanceof LogicalOrOp;
    }

    @Override
    public int hashCode() { return 4; }

    @Override
    public String toString() { return "LogicalOrOp"; }
}
