package ooplang.tokenizer;

public class LogicalAndToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof LogicalAndToken;
    }

    @Override
    public int hashCode() {
        return 12;
    }

    @Override
    public String toString() {
        return "LogicalAndToken";
    }
}
