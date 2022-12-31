package ooplang.typechecker;

import ooplang.parser.Type;
import ooplang.parser.Variable;
import ooplang.parser.ClassName;

public class InstanceVariableInformation {
    public final Type type;
    public final ClassName originalDefiner;

    public InstanceVariableInformation(final Type type,
                                       final ClassName originalDefiner) {
        this.type = type;
        this.originalDefiner = originalDefiner;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof InstanceVariableInformation) {
            final InstanceVariableInformation asInfo =
                (InstanceVariableInformation)other;
            return (type.equals(asInfo.type) &&
                    originalDefiner.equals(asInfo.originalDefiner));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return type.hashCode() + originalDefiner.hashCode();
    }

    @Override
    public String toString() {
        return ("InstanceVariableInformation(" +
                type.toString() + ", " +
                originalDefiner.toString() + ")");
    }
}
