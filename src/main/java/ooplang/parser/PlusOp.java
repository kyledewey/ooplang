package ooplang.parser;

public class PlusOp implements Op {
    public String repr() { return "+"; }
    
    @Override
    public boolean equals(final Object other) {
        return other instanceof PlusOp;
    }

    @Override
    public int hashCode() { return 0; }

    @Override
    public String toString() { return "PlusOp"; }
}

        
