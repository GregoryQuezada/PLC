package edu.ufl.cise.plc;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Character;
import java.lang.Integer;
import java.lang.Float;
import java.lang.String;

public class Lexer implements ILexer {

    HashMap<String, IToken.Kind> map = new HashMap<>();

    ArrayList<Token> token = new ArrayList<Token>();

    public static ArrayList<Integer> endLinePos = new ArrayList<Integer>();

    public static enum State {
        START,
        IN_IDENT,
        HAVE_ZERO,
        HAVE_DOT,
        IN_FLOAT,
        IN_NUM,
        HAVE_EQ,
        HAVE_MINUS,
        HAVE_LESS_THAN,
        HAVE_GREATER_THAN,
        HAVE_BANG,
        HAVE_QUOTES,
        COMMENT
    }

    private State state = State.START;
    private int arrayPos = -1;
    private int startPos = 0;
    private boolean loop = true;

    @Override
    public IToken next() throws LexicalException {
        arrayPos++;

        IToken.Kind tKind = token.get(arrayPos).getKind();

        switch (tKind) {
            case ERROR -> {
                throw new LexicalException("Error token");
            }
            case INT_LIT -> {
                try {
                    token.get(arrayPos).getIntValue();
                } catch (Exception e) {
                    throw new LexicalException("Int too big");
                }

            }
            case FLOAT_LIT -> {
                try {
                    token.get(arrayPos).getFloatValue();
                } catch (Exception e) {
                    throw new LexicalException("Float too big");
                }
            }
        }
        return token.get(arrayPos);

    }

    @Override
    public IToken peek() throws LexicalException {
        return token.get(arrayPos + 1);
    }

    public Lexer(String value) {

        insertMap(new String[]{"string", "int", "float", "boolean", "color", "image"}, IToken.Kind.TYPE);

        insertMap(new String[]{"getWidth", "getHeight"}, IToken.Kind.IMAGE_OP);

        insertMap(new String[]{"getRed", "getGreen", "getBlue"}, IToken.Kind.COLOR_OP);

        insertMap(new String[]{"BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA",
                "ORANGE", "PINK", "RED", "WHITE", "YELLOW"}, IToken.Kind.COLOR_CONST);

        insertMap(new String[]{"true", "false"}, IToken.Kind.BOOLEAN_LIT);

        map.put("if", IToken.Kind.KW_IF);
        map.put("else", IToken.Kind.KW_ELSE);
        map.put("fi", IToken.Kind.KW_FI);
        map.put("write", IToken.Kind.KW_WRITE);
        map.put("console", IToken.Kind.KW_CONSOLE);
        map.put("void", IToken.Kind.KW_VOID);

        value = value + '\0';

        int i = 0;

        while(loop) {

            //for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);

                switch (state) {
                    case START -> {
                        startPos = i;

                        if (Character.isJavaIdentifierStart(ch)) {
                            state = State.IN_IDENT;
                            i++;
                        } else {
                            switch (ch) {
                                case ' ', '\t' -> {
                                    i++;
                                }
                                case '\r', '\n' -> {
                                    endLinePos.add(i);
                                    i++;
                                }
                                case '#' -> {
                                    state = State.COMMENT;
                                    i++;
                                }
                                case '"' -> {
                                    state = State.HAVE_QUOTES;
                                    i++;
                                }
                                case '0' -> {
                                    state = State.HAVE_ZERO;
                                    i++;
                                }
                                case '1', '2', '3', '4', '5' , '6', '7', '8', '9' -> {
                                    state = State.IN_NUM;
                                    i++;
                                }
                                case '+' -> {
                                    token.add(new Token(IToken.Kind.PLUS, value, i, 1));
                                    i++;
                                }
                                case '=' -> {
                                    state = State.HAVE_EQ;
                                    i++;
                                }
                                case '&' -> {
                                    token.add(new Token(IToken.Kind.AND, value, i, 1));
                                    i++;
                                }
                                case '!' -> {
                                    state = State.HAVE_BANG;
                                    i++;
                                }
                                case ',' -> {
                                    token.add(new Token(IToken.Kind.COMMA, value, i, 1));
                                    i++;
                                }
                                case '/' -> {
                                    token.add(new Token(IToken.Kind.DIV, value, i, 1));
                                    i++;
                                }
                                case '>' -> {
                                    state = State.HAVE_GREATER_THAN;
                                    i++;
                                }
                                case '<' -> {
                                    state = State.HAVE_LESS_THAN;
                                    i++;
                                }
                                case '(' -> {
                                    token.add(new Token(IToken.Kind.LPAREN, value, startPos, 1));
                                    i++;
                                }
                                case '[' -> {
                                    token.add(new Token(IToken.Kind.LSQUARE, value, startPos, 1));
                                    i++;
                                }
                                case '-' -> {
                                    state = State.HAVE_MINUS;
                                    i++;
                                }
                                case '%' -> {
                                    token.add(new Token(IToken.Kind.MOD, value, startPos, 1));
                                    i++;
                                }
                                case '|' -> {
                                    token.add(new Token(IToken.Kind.OR, value, startPos, 1));
                                    i++;
                                }
                                case '^' -> {
                                    token.add(new Token(IToken.Kind.RETURN, value, startPos, 1));
                                    i++;
                                }
                                case ')' -> {
                                    token.add(new Token(IToken.Kind.RPAREN, value, startPos, 1));
                                    i++;
                                }
                                case ']' -> {
                                    token.add(new Token(IToken.Kind.RSQUARE, value, startPos, 1));
                                    i++;
                                }
                                case ';' -> {
                                    token.add(new Token(IToken.Kind.SEMI, value, startPos, 1));
                                    i++;
                                }
                                case '*' -> {
                                    token.add(new Token(IToken.Kind.TIMES, value, startPos, 1));
                                    i++;
                                }
                                case '\0' -> {
                                    token.add(new Token(IToken.Kind.EOF, value, i, 1));
                                    loop = false;
                                }
                                default -> {
                                    token.add(new Token(IToken.Kind.ERROR, value, startPos, i - startPos));
                                    i++;
                                }
                            }
                        }


                    }
                    case COMMENT -> {
                        switch (ch) {
                            case '\r', '\n' -> {
                                endLinePos.add(i);
                                i++;
                                state = State.START;
                            }
                            case '\0' -> {
                                token.add(new Token(IToken.Kind.EOF, value, i, 1));
                                loop = false;
                            }
                            default -> {
                                i++;
                            }
                        }
                    }
                    case HAVE_QUOTES -> {
                        switch (ch) {
                            case '"' -> {
                                token.add(new Token(IToken.Kind.STRING_LIT, value, startPos, i - startPos));
                                state = State.START;
                                i++;
                            }
//                            case '\r', '\n' -> {
//                                endLinePos.add(i);
//                                i++;
//                                state = State.START;
//                            }
                            default -> {
                                i++;
                            }
                        }
                    }
                    case IN_IDENT -> {
                        if (Character.isJavaIdentifierPart(ch)) {
                            i++;
                        }
                        else {
                            token.add(new Token(map.getOrDefault(value.substring(startPos, i), IToken.Kind.IDENT), value, startPos, i - startPos));
                            state = State.START;

                        }
                    }
                    case HAVE_ZERO-> {
                        switch (ch) {
                            case '.' -> {
                                i++;
                                state = State.HAVE_DOT;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.INT_LIT, value, startPos, i - startPos));
                                state = State.START;
                            }
                        }
                    }
                    case HAVE_DOT -> {
                        switch (ch) {
                            case '0', '1', '2', '3', '4', '5' , '6', '7', '8', '9' -> {
                                i++;
                                state = State.IN_FLOAT;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.ERROR, value, startPos, i - startPos));
                                state = State.START;
                            }
                        }
                    }
                    case IN_FLOAT -> {
                        switch (ch) {
                            case '0', '1', '2', '3', '4', '5' , '6', '7', '8', '9' -> {
                                i++;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.FLOAT_LIT, value, startPos, i - startPos));
                                state = State.START;
                            }
                        }
                    }
                    case IN_NUM -> {
                        switch (ch) {
                            case '0', '1', '2', '3', '4', '5' , '6', '7', '8', '9' -> {
                                i++;
                            }
                            case '.' -> {
                                i++;
                                state = State.HAVE_DOT;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.INT_LIT, value, startPos, i - startPos));
                                state = State.START;
                            }
                        }
                    }
                    case HAVE_EQ -> {
                        switch (ch) {
                            case '=' -> {
                                token.add(new Token(IToken.Kind.EQUALS, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.ASSIGN, value, startPos, 1));
                                state = State.START;
                            }

                        }
                    }
                    case HAVE_BANG -> {
                        switch (ch) {
                            case '=' -> {
                                token.add(new Token(IToken.Kind.NOT_EQUALS, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.BANG, value, i, 1));
                                state = State.START;
                            }
                        }
                    }
                    case HAVE_MINUS -> {
                        switch (ch) {
                            case '>' -> {
                                token.add(new Token(IToken.Kind.RARROW, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.MINUS, value, startPos, 1));
                                state = State.START;
                            }
                        }
                    }
                    case HAVE_GREATER_THAN -> {
                        switch (ch) {
                            case '=' -> {
                                token.add(new Token(IToken.Kind.GE, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            case '>' -> {
                                token.add(new Token(IToken.Kind.RANGLE, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.GT, value, startPos, 1));
                                state = State.START;
                            }
                        }
                    }
                    case HAVE_LESS_THAN -> {
                        switch (ch) {
                            case '=' -> {
                                token.add(new Token(IToken.Kind.LE, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            case '<' -> {
                                token.add(new Token(IToken.Kind.LANGLE, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            case '-' -> {
                                token.add(new Token(IToken.Kind.LARROW, value, startPos, 2));
                                i++;
                                state = State.START;
                            }
                            default -> {
                                token.add(new Token(IToken.Kind.LT, value, startPos, 1));
                                state = State.START;
                            }
                        }
                    }
                    default -> throw new IllegalStateException("State does not exist");
                }
            //}

        }


    }

    public void insertMap(String[] key, IToken.Kind value) {
        for (int i = 0; i < key.length; i++) {
            map.put(key[i], value);
        }
    }

//    public void getEndLinePos() {
//        Token current = token.get(arrayPos);
//        int curPos = current.getPos();
//        int line = 0;
//        int lineIndex = 0;
//
//        for (int i = 0; i < endLinePos.size(); i++) {
//            if (curPos > endLinePos.get(i)){
//                line++;
//                lineIndex = endLinePos.get(i);
//            } else {
//                i = endLinePos.size();
//            }
//        }
//
//        token.get(arrayPos).setSource(line, curPos - lineIndex, this);
//
//
//    }
}
