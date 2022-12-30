package ooplang.tokenizer;

public class MultToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof MultToken;
    }

    @Override
    public int hashCode() {
        return 10;
    }

    @Override
    public String toString() {
        return "MultToken";
    }
}
