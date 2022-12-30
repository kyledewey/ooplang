package ooplang.parser;

public class Param {
    public final Type type;
    public final Variable variable;

    public Param(final Type type,
                 final Variable variable) {
        this.type = type;
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Param) {
            final Param asParam = (Param)other;
            return (type.equals(asParam.type) &&
                    variable.equals(asParam.variable));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return type.hashCode() + variable.hashCode();
    }

    @Override
    public String toString() {
        return ("Param(" +
                type.toString() + ", " +
                variable.toString() + ")");
    }
}
