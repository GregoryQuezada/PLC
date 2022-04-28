package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.List;

import static edu.ufl.cise.plc.IToken.Kind.*;
import static edu.ufl.cise.plc.ast.Types.Type.*;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.runtime.ImageOps;

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
        sb.tab();
        sb.append(stringLitExpr.getValue()).space();
        sb.append("\"\"\"");

        return sb;
    }

    // NOT DONE
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        //CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(intLitExpr.getText());

        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != INT) {
            genTypeConversion(intLitExpr.getType(), intLitExpr.getCoerceTo(), sb);
        }

        return ((CodeGenStringBuilder) arg).append(sb.delegate);
        //return sb;
    }

    // NOT DONE
    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        //CodeGenStringBuilder sb = new CodeGenStringBuilder();
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(String.valueOf(floatLitExpr.getValue()));
        sb.append("f");

        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != FLOAT) {
            genTypeConversion(floatLitExpr.getType(), floatLitExpr.getCoerceTo(), sb);
        }

        //return ((CodeGenStringBuilder) arg).append(sb);
        return sb;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("ColorTuple.unpack(Color.");
        sb.append(colorConstExpr.getText());
        sb.append(".getRGB())");


        return sb;
    }

    private Object genTypeConversion(Types.Type type, Types.Type coerceType, Object arg) {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if (type != coerceType) {
            if (coerceType == INT && type == COLOR) {
                sb.insert(0, " (");
                sb.append(".pack())");
            }
            else if (coerceType == COLOR && type == INT) {
                sb.insert(0, " (new ColorTuple(");
                sb.append("))");
            }
            else {
                sb.insert(0, " (" + getObjectName(coerceType) + ") ");
            }

        }

        return sb;
    }

    private String getObjectName(Types.Type type) {

        return switch (type) {
            case INT -> "Integer";
            case FLOAT -> "Float";
            case STRING -> "String";
            case BOOLEAN -> "Boolean";
            case COLOR -> "ColorTuple";
            case IMAGE -> "BufferedImage";

            default -> throw new UnsupportedOperationException("Not implemented yet");
        };

    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

        //genTypeConversion(consoleExpr.getType(), consoleExpr.getCoerceTo(), sb);
//        if (consoleExpr.getType() != consoleExpr.getCoerceTo()) {
//            sb.lparen().append(getObjectName(consoleExpr.getCoerceTo())).rparen().space();
//        }
        sb.lparen().append(getObjectName(consoleExpr.getCoerceTo())).rparen().space();
        sb.append("ConsoleIO.readValueFromConsole( ").quote();
        sb.append(consoleExpr.getCoerceTo().toString()).quote().comma().space().quote();

        switch (consoleExpr.getCoerceTo()) {
            case INT -> sb.append("Please enter an int value. ");
            case FLOAT -> sb.append("Please enter a float value. ");
            case STRING -> sb.append("Please enter a string value. ");
            case BOOLEAN -> sb.append("Please enter a boolean value. ");
            case COLOR -> sb.append("Please enter a color value with the next three inputs. ");
            default -> throw new UnsupportedOperationException("Not yet implemented");
        }

        sb.quote().rparen();

        return sb;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("new ColorTuple( ");
        colorExpr.getRed().visit(this, sb);
        sb.comma().space();
        colorExpr.getGreen().visit(this, sb);
        sb.comma().space();
        colorExpr.getBlue().visit(this, sb);
        sb.space().rparen();

        return sb;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr expr = unaryExpression.getExpr();
        IToken oper = unaryExpression.getOp();
        IToken.Kind op = unaryExpression.getOp().getKind();
        sb.lparen();

        switch(op) {
            case MINUS,BANG -> {
                sb.append(unaryExpression.getOp().getText());
                expr.visit(this, sb);
            }
            case COLOR_OP -> {
                if (expr.getType() == INT) {
                    sb.append("ColorTuple.");
                    sb.append(oper.getText());
                    sb.lparen();
                    expr.visit(this, sb);
                    sb.rparen();
                }
                else if (expr.getType() == IMAGE) {
                    sb.append("ImageOps.extract");

                    switch (oper.getText()) {
                        case "getRed" -> sb.append("Red");
                        case "getGreen" -> sb.append("Green");
                        case "getBlue" -> sb.append("Blue");
                    }

                    sb.lparen();
                    expr.visit(this, sb);
                    sb.rparen();
                }
                else if (expr.getType() == COLOR) {
                    throw new UnsupportedOperationException("IS it COLOR");
                }

            }
            case IMAGE_OP -> {
                //throw new UnsupportedOperationException("Heck");
                expr.visit(this, sb);
                sb.append("." + oper.getText()).lparen().rparen();
            }
            default -> {
                throw new UnsupportedOperationException("Not yet implemented unary");
            }
        }

        sb.rparen();

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

        if (leftType == IMAGE && rightType == IMAGE) {
            sb.lparen();
        }
        else if (leftType == COLOR && rightType == COLOR) {
            sb.lparen();

            sb.append("ImageOps.binaryTupleOp( ImageOps.");

            if (op == EQUALS || op == NOT_EQUALS) {
                sb.append("Bool");
            }

            sb.append("OP.");
            sb.append(op.name()).comma().space();
            binaryExpr.getLeft().visit(this, sb);
            sb.comma().space();
            binaryExpr.getRight().visit(this, sb);
            sb.rparen();
            sb.rparen();
        }
        else if ((leftType == IMAGE && rightType == COLOR) || (leftType == COLOR && rightType == IMAGE)) {

        }
        else {
            sb.lparen();
            binaryExpr.getLeft().visit(this, sb);
            sb.append(binaryExpr.getOp().getText());
            binaryExpr.getRight().visit(this, sb);
            sb.rparen();
        }

//        switch(type) {
//            case IMAGE -> throw new UnsupportedOperationException("Not yet implemented binary");
//            default -> {
//                sb.lparen();
//                binaryExpr.getLeft().visit(this, sb);
//                sb.append(binaryExpr.getOp().getText());
//                binaryExpr.getRight().visit(this, sb);
//                sb.rparen();
//            }
//        }

        if (binaryExpr.getCoerceTo() != type) {
            //genTypeConversion(type, binaryExpr.getCoerceTo(), sb);
        }

        //return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
        return sb;
    }

    // NOT DONE
    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        //CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(identExpr.getText());

        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != identExpr.getType()) {
            genTypeConversion(identExpr.getType(), identExpr.getCoerceTo(), sb);
        }

        return ((CodeGenStringBuilder) arg).append(sb.delegate);
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
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr width = dimension.getWidth();
        Expr height = dimension.getHeight();
        width.visit(this, sb);
        sb.comma().space();
        height.visit(this, sb);

        //sb.append(dimension.getWidth().getText()).comma().space().append(dimension.getHeight().getText()).space();


        return sb;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        throw new UnsupportedOperationException("Pix Selec");

    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        String name = assignmentStatement.getName();
        PixelSelector pixSel = assignmentStatement.getSelector();
        Expr expr = assignmentStatement.getExpr();
        //sb.append(name);

        //if (assignmentStatement.getTargetDec().getType() == IMAGE && expr.getType() == IMAGE) {
        if (expr.getType() == IMAGE) {
            sb.append(name);
            if (pixSel != null) {
                sb.append("ImageOps.resize");
                expr.visit(this, sb);
                sb.semi().newline();
            }
            else {
                sb.equal();

                if (expr instanceof IdentExpr) {
                    sb.append("ImageOps.clone(");
                    expr.visit(this, sb);
                    sb.rparen();
                    sb.semi().newline();
                }
                else {
                    sb.append("ImageOps.resize(");
                    expr.visit(this, sb);
                    sb.comma().space().append(name + ".getWidth()");
                    sb.comma().space().append(name + ".getHeight()").rparen();
                }

                sb.semi().newline();
            }


        }
        else if (expr.getCoerceTo() == COLOR){
            sb.append("for (int x = 0; x < " + name + ".getWidth(); x++)").newline();
            sb.tab().append("for (int y = 0; y < " + name + ".getHeight(); y++)").newline();
            sb.tab().append("ImageOps.setColor(" + name + ", " + "x, y, ");
            expr.visit(this, sb);
            sb.rparen();
            sb.semi().newline();

        }
        else {
            sb.append(name);
            sb.equal();
            expr.visit(this, sb);
            sb.semi().newline();
        }

        if (pixSel != null) {
            //throw new UnsupportedOperationException("Not yet implemented assign");
        }
//        else {
//            sb.equal();
//            expr.visit(this, sb);
//            sb.semi().newline();
//        }

        return sb;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr source = writeStatement.getSource();
        Expr dest = writeStatement.getDest();

        if (source.getType() == IMAGE && dest.getType() == CONSOLE) {
            sb.append("ConsoleIO.displayImageOnScreen(");
            source.visit(this, sb);
        }
        else if (dest.getType() == STRING) {
            if (source.getType() == IMAGE) {
                sb.append("FileURLIO.writeImage(");
                source.visit(this, sb);
                sb.comma().space();
                dest.visit(this, sb);
            }
            else {
                sb.append("FileURLIO.writeValue(");
                source.visit(this, sb);
                sb.comma().space();
                dest.visit(this, sb);
            }
        }
        else {
            sb.append("ConsoleIO.console.println(");
            //sb.append(writeStatement.getSource().getText());
            source.visit(this, arg);
        }

//        sb.append("ConsoleIO.console.println(");
//        //sb.append(writeStatement.getSource().getText());
//        source.visit(this, arg);
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
            throw new UnsupportedOperationException("Not yet implemented read state");
        }

        sb.equal();

        if (expr.getType() == STRING) {
            sb.lparen();
            sb.lparen().append(getObjectName(readStatement.getTargetDec().getType())).rparen().space();
            sb.append("FileURLIO.readValueFromFile(");
            expr.visit(this, sb);
            sb.rparen().rparen();
        }
        else {
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
        sb.append("import edu.ufl.cise.plc.runtime.ColorTuple").semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.FileURLIO").semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.ImageOps").semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.ImageOpsComparison").semi().newline();
        sb.append("import java.awt.image.BufferedImage").semi().newline();
        sb.append("import java.awt.Color").semi().newline();
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
            case COLOR -> "ColorTuple";
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
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(switchTypeBackToString(nameDefWithDim.getType()));
        sb.space();
        sb.append(nameDefWithDim.getName());

        return sb;
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
        Dimension dim = declaration.getDim();
        IToken op = declaration.getOp();
        Expr expr = declaration.getExpr();

//        if (nameDef.getType() != IMAGE) {
//            nameDef.visit(this, sb);
//        }
        nameDef.visit(this, sb);


        if (nameDef.getType() == IMAGE) {
            if (expr instanceof UnaryExpr) {
                if (((UnaryExpr) expr).getOp().getKind() == COLOR_OP) {
                    sb.equal();
                    expr.visit(this, sb);
                }
            }
            else if (op != null) {
                if (op.getKind() == Kind.ASSIGN) {
                    sb.equal();
                }
                else if (op.getKind() == Kind.LARROW) {
                    sb.equal();
                }



                if (nameDef.getDim() != null) {
                    if (expr.getType() != STRING) {
                        sb.append("ImageOps.resize(");
                    }
                    else {
                        sb.append("FileURLIO.readImage( ");
                    }
                    expr.visit(this, sb);
                    sb.comma().space();
                    dim.visit(this, sb);
                    sb.rparen().semi().newline();
                }
                else {
                    if (expr instanceof IdentExpr && expr.getType() != STRING) {
                        sb.append("ImageOps.clone(");
                        expr.visit(this, sb);
                        sb.rparen().semi().newline();
                    }
                    else {
                        sb.append("FileURLIO.readImage( ");
                        expr.visit(this, sb);
                        sb.space().rparen().semi().newline();
                        // Come back
                    }

                }

                sb.append("FileURLIO.closeFiles()");

            }
            else {
                sb.equal();
                sb.append("new BufferedImage(");
                dim.visit(this, sb);
                sb.comma().space();
                sb.append("BufferedImage.TYPE_INT_RGB)");
            }
        }
//        else if (nameDef.getType() == COLOR) {
//            sb.equal();
//            expr.visit(this, sb);
//        }
        else if (op != null) {

            if (op.getKind() == Kind.ASSIGN) {
                sb.equal();
                expr.visit(this, sb);
            }
            else if (op.getKind() == Kind.LARROW) {
                sb.equal();

                if (expr.getType() == STRING) {
                    sb.lparen();
                    sb.lparen().append(getObjectName(nameDef.getType())).rparen().space();
                    sb.append("FileURLIO.readValueFromFile(");
                    expr.visit(this, sb);
                    sb.rparen().rparen();
                }
                else {
                    expr.visit(this, sb);
                }
            }

            if (nameDef.getType() == COLOR) {

            }

//            expr.visit(this, sb);
        }


        sb.semi().newline();

        return sb;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented unarypostfix");

    }
}
