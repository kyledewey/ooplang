package ooplang.tokenizer;

public class InitToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof InitToken;
    }

    @Override
    public int hashCode() {
        return 21;
    }

    @Override
    public String toString() {
        return "InitToken";
    }
}
