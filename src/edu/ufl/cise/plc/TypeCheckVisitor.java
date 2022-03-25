package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};//may be useful for constructing lookup tables.
	record Tuple<T0,T1,T2>(T0 t0, T1 t1, T2 t2){}; // made for three element lookup tables
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}
	
	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		// Done?
		stringLitExpr.setType(Type.STRING);
		return Type.STRING;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		// Done?
		intLitExpr.setType(Type.INT);
		return Type.INT;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		//TODO:  implement this method
		// Done?
		colorConstExpr.setType(Type.COLOR);
		return Type.COLOR;
		// throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}
	
	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}	

	
	
	//Maps forms a lookup table that maps an operator expression pair into result type.  
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);
	
	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type. 
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}

	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		//TODO:  implement this method
		// Done?
		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;

		switch (op) {
			case AND, OR -> {
				if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN)
					resultType = Type.BOOLEAN;
				else
					check(false, binaryExpr, "incompatible types for operator AND or OR");
			}
			case EQUALS,NOT_EQUALS -> {
				check(leftType == rightType, binaryExpr, "incompatible types for comparision");
				resultType = Type.BOOLEAN;
			}
			case PLUS, MINUS -> {
				if (leftType == Type.INT && rightType == Type.INT)
					resultType = Type.INT;
				else if (leftType == Type.FLOAT && rightType == Type.FLOAT)
					resultType = Type.FLOAT;
				else if (leftType == Type.INT && rightType == Type.FLOAT) {
					resultType = Type.FLOAT;
					binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				}
				else if (leftType == Type.FLOAT && rightType == Type.INT) {
					resultType = Type.FLOAT;
					binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				}
				else if (leftType == Type.COLOR && rightType == Type.COLOR)
					resultType = Type.COLOR;
				else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT)
					resultType = Type.COLORFLOAT;
				else if (leftType == Type.COLORFLOAT && rightType == Type.COLOR) {
					resultType = Type.COLORFLOAT;
					binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				}
				else if (leftType == Type.COLOR && rightType == Type.COLORFLOAT) {
					resultType = Type.COLORFLOAT;
					binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				}
				else if (leftType == Type.IMAGE && rightType == Type.IMAGE)
					resultType = Type.IMAGE;
				else
					check(false, binaryExpr, "incompatible types for operator PLUS or MINUS");
			}
			case TIMES, DIV, MOD -> {
				if (leftType == Type.INT && rightType == Type.INT)
					resultType = Type.INT;
				else if (leftType == Type.FLOAT && rightType == Type.FLOAT)
					resultType = Type.FLOAT;
				else if (leftType == Type.INT && rightType == Type.FLOAT) {
					resultType = Type.FLOAT;
					binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				}
				else if (leftType == Type.FLOAT && rightType == Type.INT) {
					resultType = Type.FLOAT;
					binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				}
				else if (leftType == Type.COLOR && rightType == Type.COLOR)
					resultType = Type.COLOR;
				else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT)
					resultType = Type.COLORFLOAT;
				else if (leftType == Type.COLORFLOAT && rightType == Type.COLOR) {
					resultType = Type.COLORFLOAT;
					binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				}
				else if (leftType == Type.COLOR && rightType == Type.COLORFLOAT) {
					resultType = Type.COLORFLOAT;
					binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				}
				else if (leftType == Type.IMAGE && rightType == Type.IMAGE)
					resultType = Type.IMAGE;
				else if (leftType == Type.IMAGE && rightType == Type.INT)
					resultType = Type.IMAGE;
				else if (leftType == Type.IMAGE && rightType == Type.FLOAT)
					resultType = Type.IMAGE;
				else if (leftType == Type.INT && rightType == Type.COLOR) {
					resultType = Type.COLOR;
					binaryExpr.getLeft().setCoerceTo(Type.COLOR);
				}
				else if (leftType == Type.COLOR && rightType == Type.INT) {
					resultType = Type.COLOR;
					binaryExpr.getRight().setCoerceTo(Type.COLOR);
				}
				else if (leftType == Type.FLOAT && rightType == Type.COLOR) {
					resultType = Type.COLORFLOAT;
					binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
					binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				}
				else if (leftType == Type.COLOR && rightType == Type.FLOAT) {
					resultType = Type.COLORFLOAT;
					binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
					binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				}
				else
					check(false, binaryExpr, "incompatible types for operator TIMES, DIV, or MOD");
			}
			case LT, LE, GT, GE -> {
				if (leftType == Type.INT && rightType == Type.INT)
					resultType = Type.BOOLEAN;
				else if (leftType == Type.FLOAT && rightType == Type.FLOAT)
					resultType = Type.BOOLEAN;
				else if (leftType == Type.INT && rightType == Type.FLOAT) {
					resultType = Type.BOOLEAN;
					binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				}
				else if (leftType == Type.FLOAT && rightType == Type.INT) {
					resultType = Type.BOOLEAN;
					binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				}
				else
					check(false, binaryExpr, "incompatible types for operator LT, LE, GT or GE");
			}
			default -> {
				throw new TypeCheckException("Compiler error in visitBinaryExpr");
			}
		}
		binaryExpr.setType(resultType);
		return resultType;

		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		//TODO:  implement this method
		// Done?
		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name);
		check(dec != null, identExpr, "undefined identifier" + name);
		//check(dec.isInitialized(), identExpr, "using uninitialized variable in identExpr");
		identExpr.setDec(dec);
		Type resultType = dec.getType();
		identExpr.setType(resultType);
		return resultType;

		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
		// Done?
		Type condition = (Type) conditionalExpr.getCondition().visit(this, arg);
		Type trueCase = (Type) conditionalExpr.getTrueCase().visit(this, arg);
		Type falseCase = (Type) conditionalExpr.getFalseCase().visit(this, arg);
		check(condition == Type.BOOLEAN, conditionalExpr, "condition type is not BOOLEAN");
		check(trueCase == falseCase, conditionalExpr, "trueCase is not the same type as falseCase");
		Type resultType = trueCase;
		conditionalExpr.setType(resultType);
		return resultType;

		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		//TODO  implement this method
		// Done?
		Type width = (Type) dimension.getWidth().visit(this, arg);
		Type height = (Type) dimension.getHeight().visit(this, arg);
		check(width == Type.INT && height == Type.INT, dimension, "both expressions of Dimension are not type INT");
		return null;

		//throw new UnsupportedOperationException();
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	private boolean assignmentCompatibleCase1(Type targetType, Type exprType) {
		return (targetType == exprType || targetType == Type.INT && exprType == Type.FLOAT
		|| targetType == Type.FLOAT && exprType == Type.INT || targetType == Type.INT && exprType == Type.COLOR
		|| targetType == Type.COLOR && exprType == Type.INT);
	}

	private boolean assignmentCompatibleCase2(Type targetType, Type exprType) {
		return (targetType == exprType || targetType == Type.INT && exprType == Type.FLOAT
				|| targetType == Type.FLOAT && exprType == Type.INT || targetType == Type.INT && exprType == Type.COLOR
				|| targetType == Type.COLOR && exprType == Type.INT);
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		//TODO:  implement this method
		String lhs = assignmentStatement.getName();
		Declaration dec = symbolTable.lookup(lhs);
		Type targetType = dec.getType();
		check(dec.isInitialized(), assignmentStatement, "using uninitialized variable in assignment statement");

		PixelSelector pixSelec = assignmentStatement.getSelector();
		Expr expr = assignmentStatement.getExpr();
		Type exprType = (Type) assignmentStatement.getExpr().visit(this, arg);

		switch(targetType) {
			case IMAGE -> {
				if (pixSelec == null) {
					switch(exprType) {
						case INT -> expr.setCoerceTo(Type.COLOR);
						case FLOAT -> expr.setCoerceTo(Type.COLORFLOAT);
						case COLOR, COLORFLOAT -> {
						}
						default -> {
							check(false, assignmentStatement, "expressions type and target type are not assignment compatible");
						}
					}
				} else {

				}
			}
			default -> {
				if (pixSelec == null) {
					check(assignmentCompatibleCase1(targetType, exprType), assignmentStatement, "expressions type and target type are not assignment compatible");
					if (targetType != exprType) {
						expr.setCoerceTo(targetType);
					}
				} else {
					check(false, assignmentStatement, "compiler error in visitAssignmentStatement");
				}
			}
		}

		return null;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		//TODO:  implement this method
		String lhs = readStatement.getName();
		Declaration dec = symbolTable.lookup(lhs);
		check(dec != null, readStatement, "undefined identifier" + lhs);
		check(dec.isInitialized(), readStatement, "using uninitialized variable in read statement");
		readStatement.setTargetDec(dec);
		Type selector = (Type) readStatement.getSelector().visit(this, arg);
		Type rhs = (Type) readStatement.getSource().visit(this, arg);
		check(selector == null, readStatement, "Read statement cannot have a PixelSelector");
		check(rhs == Type.CONSOLE || rhs == Type.STRING, readStatement, "Rhs for read statement must be CONSOLE or STRING");

		throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		//TODO:  implement this method
		String name = declaration.getName();
		boolean inserted = symbolTable.insert(name, declaration);
		check(inserted, declaration, "variable " + name + " already declared");
		IToken initializer = declaration.getOp();
		if (initializer != null) {
			declaration.getExpr().visit(this, arg);
		}
		else {
			check(false, declaration, "No initializer present in visitVarDeclaration");
		}
		return null;

		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//TODO:  this method is incomplete, finish it.  
		
		//Save root of AST so return type can be accessed in return statements
		root = program;

		symbolTable.insertProgramName(program.getName());

		List<NameDef> param = program.getParams();
		for (NameDef node : param) {
			node.visit(this,arg);
		}
		
		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDef.getName();
		boolean inserted = symbolTable.insert(name, nameDef);
		check(inserted, nameDef, "variable " + name + " already declared");

		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDefWithDim.getName();
		nameDefWithDim.getDim().visit(this, arg);
		boolean inserted = symbolTable.insert(name, nameDefWithDim);
		check(inserted, nameDefWithDim, "variable " + name + " already declared");
		return null;

		//throw new UnsupportedOperationException();
	}
 
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}
