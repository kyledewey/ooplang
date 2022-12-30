package ooplang.parser;

public class IntLiteralExp implements Exp {
    public final int value;
    public IntLiteralExp(final int value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof IntLiteralExp &&
                value == ((IntLiteralExp)other).value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return "IntLiteralExp(" + value + ")";
    }
}
