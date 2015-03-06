package com.prolog.jvm.compiler.main;

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.prolog.jvm.compiler.parser.PrologJvmLexer;
import com.prolog.jvm.compiler.parser.PrologJvmParser;

/**
 * TODO Arno
 * 
 * @author Arno Bastenhof
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream(System.in); // TODO Arno input file
		PrologJvmLexer lexer = new PrologJvmLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PrologJvmParser parser = new PrologJvmParser(tokens);
		ParseTree tree = parser.program();
		System.out.println(tree.toStringTree(parser));
	}

}
