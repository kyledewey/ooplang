package ooplang.parser;

public class MethodName {
    public final String name;
    public MethodName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof MethodName &&
                name.equals(((MethodName)other).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "MethodName(" + name + ")";
    }
}
