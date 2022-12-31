package ooplang.parser;

public class VariableLhs implements Lhs {
    public final Variable variable;
    public VariableLhs(final Variable variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof VariableLhs &&
                variable.equals(((VariableLhs)other).variable));
    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }

    @Override
    public String toString() {
        return "VariableLhs(" + variable.toString() + ")";
    }
}
