package ooplang.parser;

public class LessThanOp implements Op {
    public String repr() { return "<"; }
    
    @Override
    public boolean equals(final Object other) {
        return other instanceof LessThanOp;
    }

    @Override
    public int hashCode() { return 2; }

    @Override
    public String toString() { return "LessThanOp"; }
}
