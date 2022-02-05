package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Character;
import java.lang.Integer;
import java.lang.Float;
import java.lang.String;

public class Lexer implements ILexer {

    HashMap<String, IToken.Kind> map = new HashMap<>();

    ArrayList<Token> token = new ArrayList<Token>();

    public static enum State {
        START,
        IN_IDENT,
        HAVE_ZERO,
        HAVE_DOT,
        IN_FLOAT,
        IN_NUM,
        HAVE_EQ,
        HAVE_MINUS,
    }

    private State state = State.START;

    @Override
    public IToken next() throws LexicalException {
        return null;
    }

    @Override
    public IToken peek() throws LexicalException {
        return null;
    }

    public Lexer(String value) {

        insertMap(new String[]{"string", "int", "float", "boolean", "color", "image", "void"}, IToken.Kind.TYPE);

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

        value = value + '\0';

        while(true) {

            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);

                switch (state) {
                    case START -> {
                        switch (ch) {
                            case ' ', '\t', '\r' -> {
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
                            case '\0' -> {
                                token.add(new Token(IToken.Kind.EOF, value, i, 1));
                                return;
                            }
                        }
                    }
                    case IN_IDENT -> {
                        switch (ch) {

                        }
                    }
                    case HAVE_ZERO-> {

                    }
                    case HAVE_DOT -> {

                    }
                    case IN_FLOAT -> {

                    }
                    case IN_NUM -> {

                    }
                    case HAVE_EQ -> {
                        switch (ch) {
                            case '=' -> {
                                token.add(new Token(IToken.Kind.EQUALS, value, i, 2));
                                i++;
                            }
                            default -> {
                                throw new UnsupportedOperationException("Not a valid token: = ");
                            }

                        }
                    }
                    case HAVE_MINUS -> {

                    }
                    default -> throw new IllegalStateException("State does not exist");
                }
            }

        }


    }

    public void insertMap(String[] key, IToken.Kind value) {
        for (int i = 0; i < key.length; i++) {
            map.put(key[i], value);
        }
    }
}
