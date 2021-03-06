package com.prolog.jvm.compiler;

import java.io.IOException;

import com.prolog.jvm.compiler.ast.Ast;
import com.prolog.jvm.compiler.ast.AstWalker;
import com.prolog.jvm.compiler.parser.PrologParser;
import com.prolog.jvm.compiler.parser.Tokens;
import com.prolog.jvm.compiler.visitor.PrologVisitor;
import com.prolog.jvm.compiler.visitor.SourcePass;
import com.prolog.jvm.exceptions.RecognitionException;
import com.prolog.jvm.symbol.Scope;
import com.prolog.jvm.zip.api.PrologBytecode;

/**
 * A compiler for Prolog programs.
 *
 * @author Arno Bastenhof
 *
 */
public final class ProgramCompiler extends AbstractCompiler {

    /**
     * @param code the target for writing the generated bytecode to; not allowed
     * to be null
     * @param scope the ground scope to use when resolving symbols; not allowed
     * to be null
     * @throws NullPointerException if {@code code == null || scope == null}
     */
    public ProgramCompiler(final PrologBytecode<?> code, final Scope scope) {
        super(code, scope);
    }

    @Override
    protected SourcePass createSourcePassVisitor() {
        return new SourcePass(Tokens.PROGRAM);
    }

    @Override
    protected void parseSource(final PrologParser parser) throws IOException,
            RecognitionException {
        parser.parseProgram();
    }

    @Override
    protected void walkAst(final Ast root, final PrologVisitor<Ast> visitor) {
        AstWalker.INSTANCE.walkProgram(root, visitor);
    }
}