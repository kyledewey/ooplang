package ooplang.typechecker;

import ooplang.parser.MethodDef;
import ooplang.parser.ClassName;

public class MethodInformation {
    public final MethodDef methodDef;
    public final ClassName originalDefiner;
    public final ClassName mostRecentDefiner;

    public MethodInformation(final MethodDef methodDef,
                             final ClassName originalDefiner,
                             final ClassName mostRecentDefiner) {
        this.methodDef = methodDef;
        this.originalDefiner = originalDefiner;
        this.mostRecentDefiner = mostRecentDefiner;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof MethodInformation) {
            final MethodInformation asInfo = (MethodInformation)other;
            return (methodDef.equals(asInfo.methodDef) &&
                    originalDefiner.equals(asInfo.originalDefiner) &&
                    mostRecentDefiner.equals(asInfo.mostRecentDefiner));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (methodDef.hashCode() +
                originalDefiner.hashCode() +
                mostRecentDefiner.hashCode());
    }

    @Override
    public String toString() {
        return ("MethodInformation(" +
                methodDef.toString() + ", " +
                originalDefiner.toString() + ", " +
                mostRecentDefiner.toString() + ")");
    }
}
