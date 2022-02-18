package edu.ufl.cise.plc;

import java.lang.Character;
import java.lang.Integer;
import java.lang.Float;

public class Token implements IToken{

//    HashMap<String, Kind> map = new HashMap<>();

    public static int[] endLine;

    private Kind kind;
    private String input;
    private int pos;
    private int length;
    private SourceLocation loc = null;

    public Token(Kind kind, String input, int pos, int length) {

        this.kind = kind;
        this.input = input;
        this.pos = pos;
        this.length = length;



//        insertMap(new String[]{"string", "int", "float", "boolean", "color", "image", "void"}, Kind.TYPE);
//
//        insertMap(new String[]{"getWidth", "getHeight"}, Kind.IMAGE_OP);
//
//        insertMap(new String[]{"getRed", "getGreen", "getBlue"}, Kind.COLOR_OP);
//
//        insertMap(new String[]{"BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA",
//                "ORANGE", "PINK", "RED", "WHITE", "YELLOW"}, Kind.COLOR_CONST);
//
//        insertMap(new String[]{"true", "false"}, Kind.BOOLEAN_LIT);
//
//        map.put("if", Kind.KW_IF);
//        map.put("else", Kind.KW_ELSE);
//        map.put("fi", Kind.KW_FI);
//        map.put("write", Kind.KW_WRITE);
//        map.put("console", Kind.KW_CONSOLE);


    }

//    public void insertMap(String[] key, Kind value) {
//        for (int i = 0; i < key.length; i++) {
//            map.put(key[i], value);
//        }
//    }
    public void setSource() {
        int line = 0;
        int lineIndex = 0;

        for (int i = 0; i < Lexer.endLinePos.size(); i++) {
            if (pos > Lexer.endLinePos.get(i)){
                line++;
                lineIndex = Lexer.endLinePos.get(i);
            } else {
                i = Lexer.endLinePos.size();
            }
        }

        if (line > 0) {
            lineIndex++;
        }

        loc = new SourceLocation(line, pos - lineIndex);
    }

    public int getPos() {
        return pos;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getText() {
        return input.substring(pos, pos + length);
    }

    @Override
    public SourceLocation getSourceLocation() {
        if (loc == null) {
            setSource();
        }

        return loc;
    }

    @Override
    public int getIntValue() {
        if (kind == Kind.INT_LIT) {
            return Integer.parseInt(getText());
        }
        else {
            throw new UnsupportedOperationException("Kind is not an INT_LIT");
        }

    }

    @Override
    public float getFloatValue() {
        if (kind == Kind.FLOAT_LIT) {
            return Float.parseFloat(getText());
        }
        else {
            throw new UnsupportedOperationException("Kind is not a FLOAT_INT");
        }
    }

    @Override
    public boolean getBooleanValue() {
        if (kind == Kind.BOOLEAN_LIT) {
            return Boolean.parseBoolean(getText());
        }
        else {
            throw new UnsupportedOperationException("Kind is not a BOOLEAN_LIT");
        }
    }

    @Override
    public String getStringValue() {
        if (kind == Kind.STRING_LIT) {
            String in = getText();
            String out = "";

            for (int i = 0; i < in.length(); i++) {
                char a = in.charAt(i);

                switch(a) {
                    case '\\' -> {
                        out = out;
                    }
                    case '\n' -> {
                        out = out + "\n";
                    }
                    case '\b' -> {
                        out = out + "\b";
                    }
                    case '\t' -> {
                        out = out + "\t";
                    }
                    case '\f' -> {
                        out = out + "\f";
                    }
                    case '\r' -> {
                        out = out + "\r";
                    }
                    case '\"' -> {
                        if (i > 0) {
                            out = out + "\"";
                        }

                    }
                    case '\'' -> {
                        out = out + "\'";
                    }
                    default -> {
                        out = out + a;
                    }
                }
            }
            return out;
        }
        else {
            throw new UnsupportedOperationException("Kind is not a STRING_LIT");
        }

    }

}
