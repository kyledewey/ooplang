package ooplang.parser;

import java.util.List;

public class Program {
    public final List<ClassDef> classDefs;
    public final Stmt entryPoint;

    public Program(final List<ClassDef> classDefs,
                   final Stmt entryPoint) {
        this.classDefs = classDefs;
        this.entryPoint = entryPoint;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Program) {
            final Program asProgram = (Program)other;
            return (classDefs.equals(asProgram.classDefs) &&
                    entryPoint.equals(asProgram.entryPoint));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return classDefs.hashCode() + entryPoint.hashCode();
    }

    @Override
    public String toString() {
        return ("Program(" +
                classDefs.toString() + ", " +
                entryPoint.toString() + ")");
    }
}
