package ooplang.typechecker;

import ooplang.parser.Variable;
import ooplang.parser.Type;

import java.util.Map;

public class StmtResult {
    public final Map<Variable, Type> typeEnv;
    public final boolean returnsOnAllPaths;

    public StmtResult(final Map<Variable, Type> typeEnv,
                      final boolean returnsOnAllPaths) {
        this.typeEnv = typeEnv;
        this.returnsOnAllPaths = returnsOnAllPaths;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof StmtResult) {
            final StmtResult asRes = (StmtResult)other;
            return (typeEnv.equals(asRes.typeEnv) &&
                    returnsOnAllPaths == asRes.returnsOnAllPaths);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (typeEnv.hashCode() +
                ((returnsOnAllPaths) ? 1 : 0));
    }

    @Override
    public String toString() {
        return ("StmtResult(" +
                typeEnv.toString() + ", " +
                returnsOnAllPaths + ")");
    }
}
