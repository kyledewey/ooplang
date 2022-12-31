package ooplang.tokenizer;

public class CallToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof CallToken;
    }

    @Override
    public int hashCode() {
        return 25;
    }

    @Override
    public String toString() {
        return "CallToken";
    }
}
