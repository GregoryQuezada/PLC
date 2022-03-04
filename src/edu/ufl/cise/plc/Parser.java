package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.ArrayList;

public class Parser implements IParser{

    private ILexer lexer;
    private IToken t;

    public Parser (String value) {
        lexer = CompilerComponentFactory.getLexer(value);
    }

    @Override
    public ASTNode parse() throws PLCException {
        t = lexer.next();
        return program();

    }


    protected boolean isKind(IToken.Kind... kinds) {
        for (IToken.Kind k: kinds) {
            if (k == t.getKind()) {
                return true;
            }
        }
        return false;
    }

    public Program program() throws PLCException {
        IToken firstToken = t;
        IToken name = null;
        Program p = null;

        if (isKind(IToken.Kind.TYPE, IToken.Kind.KW_VOID)) {
            ArrayList<NameDef> nameDefList = new ArrayList<NameDef>();
            ArrayList<ASTNode> ASTNodeList = new ArrayList<ASTNode>();

            Types.Type returnType = Types.Type.toType(t.getText());
            consume();
            name = t;
            consume();
            match(IToken.Kind.LPAREN);

            if (isKind(IToken.Kind.TYPE)) {
                nameDefList.add(nameDef());

                while (isKind(IToken.Kind.COMMA)) {
                    consume();
                    nameDefList.add(nameDef());
                }
            }

            match(IToken.Kind.RPAREN);


            while (isKind(IToken.Kind.TYPE, IToken.Kind.IDENT, IToken.Kind.KW_WRITE, IToken.Kind.RETURN)) {

                if (isKind(IToken.Kind.TYPE)) {
                    ASTNodeList.add(declaration());
                } else {
                    ASTNodeList.add(statement());
                }
                match(IToken.Kind.SEMI);
            }

            p = new Program(firstToken, returnType, name.getText(), nameDefList, ASTNodeList);

        }
        else {
            throw new SyntaxException("Expected TYPE or VOID in program");
        }

        return p;
    }

    NameDef nameDef() throws PLCException {
        IToken firstToken = t;
        IToken type = null;
        IToken name = null;
        Dimension dim = null;

        if (isKind(Token.Kind.TYPE)) {
            type = t;
            consume();
            if (isKind(Token.Kind.LSQUARE)) {
                dim = dimension();
                name = t;
                consume();
                return new NameDefWithDim(firstToken, type, name, dim);
            }
            name = t;
            consume();
            return new NameDef(firstToken, type, name);
        } else {
            throw new SyntaxException("Expected TYPE in nameDef");
        }

    }

    VarDeclaration declaration() throws PLCException {
        IToken firstToken = t;
        NameDef nameDef = nameDef();
        IToken op = null;
        Expr e = null;

        if (isKind(IToken.Kind.ASSIGN, IToken.Kind.LARROW)) {
            op = t;
            consume();
            e = expr();
        }

        return new VarDeclaration(firstToken, nameDef, op, e);
    }

    public Expr expr() throws PLCException {
        IToken firstToken = t;
        Expr e = null;

        if (isKind(IToken.Kind.KW_IF)) {
            e = conditionalExpr();
        }
        else {
            e = logicalOrExpr();
        }



        return e;
    }

    Expr conditionalExpr() throws PLCException {
        IToken firstToken = t;
        Expr expr0 = null;
        Expr expr1 = null;
        Expr expr2 = null;

        if (isKind(IToken.Kind.KW_IF)) {
            consume();
            match(IToken.Kind.LPAREN);
            expr0 = expr();
            match(IToken.Kind.RPAREN);
            expr1 = expr();
            match(IToken.Kind.KW_ELSE);
            expr2 = expr();
            match(IToken.Kind.KW_FI);
        } else {
            throw new SyntaxException("Expected KW_IF in conditionalExpr");
        }

        return new ConditionalExpr(firstToken, expr0, expr1, expr2);
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

        while (isKind(IToken.Kind.AND))
        {
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

        while (isKind(IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.EQUALS, IToken.Kind.NOT_EQUALS, IToken.Kind.LE, IToken.Kind.GE))
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

    Expr unaryExpr() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        Expr right = null;

        if (isKind(IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.COLOR_OP, IToken.Kind.IMAGE_OP)){
            IToken op = t;
            consume();
            right = unaryExpr();
            e = new UnaryExpr(firstToken, op, right);
        }
        else {
            e = unaryExprPostfix();
        }


        return e;
    }

    Expr unaryExprPostfix() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        Expr right = null;

        e = primaryExpr();


        while (isKind(IToken.Kind.LSQUARE))
        {
            return new UnaryExprPostfix(firstToken, e, pixelSelector());
        }

        return e;
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
        else if (isKind(IToken.Kind.COLOR_CONST)) {
            e = new ColorConstExpr(firstToken);
            consume();
        }
        else if (isKind(IToken.Kind.LANGLE)) {
            consume();
            Expr e1 = expr();
            match(IToken.Kind.COMMA);
            Expr e2 = expr();
            match(IToken.Kind.COMMA);
            Expr e3 = expr();
            match(IToken.Kind.RANGLE);
            e = new ColorExpr(firstToken, e1, e2, e3);

        }
        else if (isKind(IToken.Kind.KW_CONSOLE)) {
            e = new ConsoleExpr(firstToken);
            consume();
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

    Dimension dimension() throws PLCException {
        IToken firstToken = t;
        Dimension e = null;
        Expr left = null;
        Expr right = null;

        if (isKind(IToken.Kind.LSQUARE)){
            consume();
            left = expr();
            match(IToken.Kind.COMMA);
            right = expr();
            match(IToken.Kind.RSQUARE);
            e = new Dimension(firstToken, left, right);

        }
        else {
            throw new SyntaxException("Expected LSQUARE in dimension");
        }

        return e;
    }

    Statement statement() throws PLCException {
        IToken firstToken = t;
        IToken str = null;
        PixelSelector p = null;
        IToken op = null;
        Expr e = null;


        if (isKind(IToken.Kind.IDENT)) {
            str = t;
            consume();

            if (isKind(IToken.Kind.LSQUARE)) {
                p = pixelSelector();
            }

            if (isKind(IToken.Kind.ASSIGN, IToken.Kind.LARROW)) {
                op = t;
                consume();
                e = expr();

                if (op.getKind() == IToken.Kind.ASSIGN) {
                    return new AssignmentStatement(firstToken, str.getText(), p, e);
                }
                else {
                    return new ReadStatement(firstToken, str.getText(), p, e);
                }
            }
            else {
                throw new SyntaxException("Expected ASSIGN or LARROW in statement");
            }
        }
        else if (isKind(IToken.Kind.KW_WRITE)) {
            consume();
            e = expr();
            match(IToken.Kind.RARROW);
            Expr dest = expr();
            return new WriteStatement(firstToken, e, dest);
        }
        else if (isKind(IToken.Kind.RETURN)) {
            consume();
            e = expr();
            return new ReturnStatement(firstToken, e);
        }
        else {
            throw new SyntaxException("Expected IDENT, WRITE or RETURN in statement");
        }

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
        if (lexer.peek().getKind() != IToken.Kind.EOF ) {
            t = lexer.next();
        }
    }
}
