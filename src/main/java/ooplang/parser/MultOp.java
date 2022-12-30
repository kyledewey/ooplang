package ooplang.parser;

public class MultOp implements Op {
    public String repr() { return "*"; }
    
    @Override
    public boolean equals(final Object other) {
        return other instanceof MultOp;
    }

    @Override
    public int hashCode() { return 1; }

    @Override
    public String toString() { return "MultOp"; }
}

        
