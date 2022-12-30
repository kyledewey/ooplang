package ooplang.tokenizer;

public class ClassToken implements Token {
    @Override
    public boolean equals(final Object other) {
        return other instanceof ClassToken;
    }

    @Override
    public int hashCode() {
        return 23;
    }

    @Override
    public String toString() {
        return "ClassToken";
    }
}
