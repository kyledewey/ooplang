package ooplang;

import java.io.File;
import java.io.IOException;

import ooplang.tokenizer.*;
import ooplang.parser.*;
import ooplang.typechecker.*;
import ooplang.codegen.*;

public class Ooplang {
    public static void usage() {
        System.out.println("Takes the following params:");
        System.out.println("-Input ooplang file");
        System.out.println("-Output C file");
    }
    
    public static void main(String[] args)
        throws TokenizerException,
               IOException,
               ParseException,
               TypeErrorException,
               CodegenException {
        if (args.length != 2) {
            usage();
        } else {
            final Token[] tokens = Tokenizer.tokenize(new File(args[0]));
            final Program program = Parser.parseProgram(tokens);
            final ClassInformation classInfo = Typechecker.typecheck(program);
            Codegen.writeProgram(program, classInfo, new File(args[1]));
        }
    } // main
} // Ooplang
