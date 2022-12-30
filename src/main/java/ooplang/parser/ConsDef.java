package ooplang.parser;

import java.util.List;

public class ConsDef {
    public final List<Param> params;
    public final List<Exp> exps;
    public final Stmt stmt;

    public ConsDef(final List<Param> params,
                   final List<Exp> exps,
                   final Stmt stmt) {
        this.params = params;
        this.exps = exps;
        this.stmt = stmt;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ConsDef) {
            final ConsDef asCons = (ConsDef)other;
            return (params.equals(asCons.params) &&
                    exps.equals(asCons.exps) &&
                    stmt.equals(asCons.stmt));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (params.hashCode() +
                exps.hashCode() +
                stmt.hashCode());
    }

    @Override
    public String toString() {
        return ("ConsDef(" +
                params.toString() + ", " +
                exps.toString() + ", " +
                stmt.toString() + ")");
    }
}
