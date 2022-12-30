package ooplang.parser;

public class ClassType implements Type {
    public final ClassName name;

    public ClassType(final ClassName name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ClassType &&
                name.equals(((ClassType)other).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ClassType(" + name.toString() + ")";
    }
}

        
