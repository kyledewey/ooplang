package ooplang.parser;

public class LogicalAndOp implements Op {
    public String repr() { return "&&"; }

    @Override
    public boolean equals(final Object other) {
        return other instanceof LogicalAndOp;
    }

    @Override
    public int hashCode() { return 3; }

    @Override
    public String toString() { return "LogicalAndOp"; }
}
