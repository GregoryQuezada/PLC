package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.List;

import static edu.ufl.cise.plc.ast.Types.Type.*;
import edu.ufl.cise.plc.IToken.Kind;

public class CodeGenVisitor implements ASTVisitor {

    String packageName;

    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

        if (booleanLitExpr.getValue()) {
            sb.append("true");
        } else {
            sb.append("false");
        }

        return sb;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("\"\"\"\n");
        sb.tab().tab();
        sb.append(stringLitExpr.getValue()).space();
        sb.append("\"\"\"");

        return sb;
    }

    // NOT DONE
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        //CodeGenStringBuilder sb = new CodeGenStringBuilder();
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(intLitExpr.getText());

        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != INT) {

        }

        return sb;
    }

    // NOT DONE
    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(String.valueOf(floatLitExpr.getValue()));
        sb.append("f");

        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != FLOAT) {

        }

        return sb;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        return null;
    }

    private String getObjectName(Types.Type type) {

        return switch (type) {
            case INT -> "Integer";
            case FLOAT -> "Float";
            case STRING -> "String";
            case BOOLEAN -> "Boolean";
            default -> throw new UnsupportedOperationException("Not implemented yet");
        };

    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

        sb.lparen().append(getObjectName(consoleExpr.getCoerceTo())).rparen().space();
        sb.append("ConsoleIO.readValueFromConsole( ").quote();
        sb.append(consoleExpr.getCoerceTo().toString()).quote().comma().space().quote();

        switch (consoleExpr.getCoerceTo()) {
            case INT -> sb.append("Please enter an int value. ");
            case FLOAT -> sb.append("Please enter a float value. ");
            case STRING -> sb.append("Please enter a string value. ");
            case BOOLEAN -> sb.append("Please enter a boolean value. ");
            default -> throw new UnsupportedOperationException("Not yet implemented");
        }

        sb.quote().rparen();

        return sb;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr expr = unaryExpression.getExpr();
        IToken.Kind op = unaryExpression.getOp().getKind();
        sb.lparen();

        switch(op) {
            case MINUS,BANG -> {
                sb.append(unaryExpression.getOp().getText());
                expr.visit(this, sb);
                sb.rparen();
            }
            default -> {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }

        return sb;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        //CodeGenStringBuilder sb = new CodeGenStringBuilder();
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Types.Type type = binaryExpr.getType();
        Expr leftExpr = binaryExpr.getLeft();
        Expr rightExpr = binaryExpr.getRight();
        Types.Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo(): leftExpr.getType();
        Types.Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo(): rightExpr.getType();
        IToken.Kind op = binaryExpr.getOp().getKind();

        switch(type) {
            case IMAGE -> throw new UnsupportedOperationException("Not yet implemented");
            default -> {
                sb.lparen();
                binaryExpr.getLeft().visit(this, sb);
                sb.append(binaryExpr.getOp().getText());
                binaryExpr.getRight().visit(this, sb);
                sb.rparen();
            }
        }

        if (binaryExpr.getCoerceTo() != type) {
            //genTypeConversion(type, binaryExpr.getCoerceTo(), sb);
        }

        //return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
        return sb;
    }

    // NOT DONE
    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(identExpr.getText());

        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != identExpr.getType()) {

        }

        return sb;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr cond = conditionalExpr.getCondition();
        Expr trueCond = conditionalExpr.getTrueCase();
        Expr falseCond = conditionalExpr.getFalseCase();

        sb.lparen();
        cond.visit(this, sb);
        sb.rparen().space().append("?").space();
        trueCond.visit(this, sb);
        sb.space().colon().space();
        falseCond.visit(this, sb);

        return sb;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        String name = assignmentStatement.getName();
        PixelSelector pixSel = assignmentStatement.getSelector();
        Expr expr = assignmentStatement.getExpr();
        sb.append(name);

        if (pixSel != null) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        else {
            sb.equal();
            expr.visit(this, sb);
            sb.semi().newline();
        }

        return sb;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr source = writeStatement.getSource();
        sb.append("ConsoleIO.console.println(");
        //sb.append(writeStatement.getSource().getText());
        source.visit(this, arg);
        sb.rparen().semi().newline();

        return sb;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        PixelSelector pixSelec = readStatement.getSelector();
        Expr expr = readStatement.getSource();

        sb.append(readStatement.getName());

        if (pixSelec != null) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        else {
            sb.equal();
            expr.visit(this, sb);
        }

        sb.semi().newline();

        return sb;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        //CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        CodeGenStringBuilder sb = new CodeGenStringBuilder();


        sb.append("package ").append(packageName).semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.ConsoleIO").semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.FileURLIO").semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.ImageOps").semi().newline();
        sb.append("import java.awt.image.BufferedImage").semi().newline();
        sb.append("public class ").append(program.getName()).append(" {").newline();
        sb.tab().append("public static ").append(switchTypeBackToString(program.getReturnType())).append(" apply").lparen();

        List<NameDef> param = program.getParams();
        int paramCounter = 0;
        for (NameDef node : param) {
            if (paramCounter > 0) {
                sb.comma().space();
            }
            node.visit(this, sb);
            paramCounter++;
        }

        sb.rparen().lbrace().newline();


        List<ASTNode> decAndSta = program.getDecsAndStatements();
        for (ASTNode node : decAndSta) {
            sb.tab().tab();
            node.visit(this, sb);

        }

        sb.tab().rbrace().newline();
        sb.rbrace();

        return sb.delegate.toString();
    }

    private String switchTypeBackToString(Types.Type name) {
        return switch(name) {
            case BOOLEAN -> "boolean";
            case COLOR -> "color";
            case CONSOLE -> "console";
            case FLOAT -> "float";
            case IMAGE -> "BufferedImage";
            case INT -> "int";
            case STRING -> "String";
            case VOID -> "void";
            default -> throw new IllegalArgumentException("Unexpected type value: " + name);
        };
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(switchTypeBackToString(nameDef.getType()));
        sb.space();
        sb.append(nameDef.getName());

        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr expr = returnStatement.getExpr();
        sb.append("return ");
        expr.visit(this, sb);
        sb.semi().newline();
        return sb;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        NameDef nameDef = declaration.getNameDef();
        IToken op = declaration.getOp();
        Expr expr = declaration.getExpr();

        if (nameDef.getType() != IMAGE) {
            nameDef.visit(this, sb);
        }



        if (op != null) {
            if (nameDef.getDim() != null) {

            }
            if (op.getKind() == Kind.ASSIGN) {
                sb.equal();
            }
            else if (op.getKind() == Kind.LARROW) {
                sb.larrow();
            }
            expr.visit(this, sb);
        }
        else {

        }

        sb.semi().newline();

        return sb;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        return null;
    }
}
