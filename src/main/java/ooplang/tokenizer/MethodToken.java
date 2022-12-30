package ooplang.tokenizer;

public class MethodToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof MethodToken;
    }

    @Override
    public int hashCode() {
        return 24;
    }

    @Override
    public String toString() {
        return "MethodToken";
    }
}
