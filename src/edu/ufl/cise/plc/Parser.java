package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

public class Parser implements IParser{

    private ILexer lexer;
    private IToken t;

    public Parser (String value) {
        ILexer lexer = CompilerComponentFactory.getLexer(value);
    }

    @Override
    public ASTNode parse() throws PLCException {
        ASTNode tree;

        return null;
    }

    protected boolean isKind(IToken.Kind kind) {
        return t.getKind() == kind;
    }

    protected boolean isKind(IToken.Kind... kinds) {
        for (IToken.Kind k: kinds) {
            if (k == t.getKind()) {
                return true;
            }
        }
        return false;
    }

    public Expr expr() {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;


        return null;
    }

    Expr conditionalExpr() throws PLCException {

        if (isKind(IToken.Kind.KW_IF)) {
            consume();
            match(IToken.Kind.LPAREN);

        }

        return null;
    }

    Expr logicalOrExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = logicalAndExpr();

        while (isKind(IToken.Kind.OR))
        {
            IToken op = t;
            consume();
            right = logicalAndExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }

        return left;
    }

    Expr logicalAndExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = comparisonExpr();

        while (isKind(IToken.Kind.AND)) {
            IToken op = t;
            consume();
            right = comparisonExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }

        return left;
    }

    Expr comparisonExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = additiveExpr();

        while(isKind(IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.EQUALS, IToken.Kind.NOT_EQUALS, IToken.Kind.LE, IToken.Kind.GE));
        {
            IToken op = t;
            consume();
            right = additiveExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }

        return left;
    }

    Expr additiveExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = multiplicativeExpr();

        while (isKind(IToken.Kind.PLUS, IToken.Kind.MINUS))
        {
            IToken op = t;
            consume();
            right = multiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }

        return left;
    }

    Expr multiplicativeExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = unaryExpr();

        while (isKind(IToken.Kind.TIMES, IToken.Kind.DIV, IToken.Kind.MOD))
        {
            IToken op = t;
            consume();
            right = unaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }

        return left;
    }

    Expr unaryExpr() {
        return null;
    }

    Expr unaryExprPostfix() {
        return null;
    }

    Expr primaryExpr() throws PLCException {
        IToken firstToken = t;
        Expr e = null;

        if (isKind(IToken.Kind.BOOLEAN_LIT)) {
            e = new BooleanLitExpr(firstToken);
            consume();
        }
        else if (isKind(IToken.Kind.STRING_LIT)) {
            e = new StringLitExpr(firstToken);
            consume();
        }
        else if (isKind(IToken.Kind.INT_LIT)) {
            e = new IntLitExpr(firstToken);
            consume();
        }
        else if (isKind(IToken.Kind.FLOAT_LIT)) {
            e = new FloatLitExpr(firstToken);
            consume();
        }
        else if (isKind(IToken.Kind.IDENT)) {
            e = new IdentExpr(firstToken);
            consume();
        }
        else if (isKind(IToken.Kind.LPAREN)) {
            consume();
            e = expr();
            match(IToken.Kind.RPAREN);
        }
        else {
            throw new SyntaxException("PrimaryExpr did not work");
        }

        return e;
    }

    PixelSelector pixelSelector() throws PLCException {
        IToken firstToken = t;
        PixelSelector e = null;
        Expr left = null;
        Expr right = null;

        if (isKind(IToken.Kind.LSQUARE)){
            consume();
            left = expr();
            match(IToken.Kind.COMMA);
            right = expr();
            match(IToken.Kind.RSQUARE);
            e = new PixelSelector(firstToken, left, right);

        } else {
            throw new SyntaxException("Expected LSQUARE in pixelSelector");
        }

        return e;
    }

    private void match (IToken.Kind... types) throws PLCException {
        for (IToken.Kind k: types) {
            if (k == t.getKind()) {
                consume();
            } else {
                throw new SyntaxException("Match did not work " + k);
            }
        }
    }

    private void consume() throws PLCException {
        lexer.next();
    }
}
