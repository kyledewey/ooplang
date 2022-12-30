package ooplang.tokenizer;

public class ThisToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof ThisToken;
    }

    @Override
    public int hashCode() {
        return 2;
    }

    @Override
    public String toString() {
        return "ThisToken";
    }
}
