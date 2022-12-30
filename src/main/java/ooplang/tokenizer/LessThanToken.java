package ooplang.tokenizer;

public class LessThanToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof LessThanToken;
    }

    @Override
    public int hashCode() {
        return 11;
    }

    @Override
    public String toString() {
        return "LessThanToken";
    }
}
