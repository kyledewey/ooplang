package ooplang.tokenizer;

public class SuperToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof SuperToken;
    }

    @Override
    public int hashCode() {
        return 22;
    }

    @Override
    public String toString() {
        return "SuperToken";
    }
}
