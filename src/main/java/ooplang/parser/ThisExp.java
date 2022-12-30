package ooplang.parser;

public class ThisExp implements Exp {
    @Override
    public boolean equals(final Object other) {
        return other instanceof ThisExp;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "ThisExp";
    }
}

        
