package edu.ufl.cise.plc;

public class CodeGenStringBuilder {
    StringBuilder delegate = new StringBuilder();

    public CodeGenStringBuilder append(String s) {
        delegate.append(s);
        return this;
    }

    public CodeGenStringBuilder append(StringBuilder s) {
        delegate.append(s.toString());
        return this;
    }

    public CodeGenStringBuilder comma() {
        delegate.append(",");
        return this;
    }

    public CodeGenStringBuilder semi() {
        delegate.append(";");
        return this;
    }

    public CodeGenStringBuilder newline() {
        delegate.append("\n");
        return this;
    }

    public CodeGenStringBuilder equal() {
        delegate.append(" = ");
        return this;
    }

    public CodeGenStringBuilder lparen() {
        delegate.append("(");
        return this;
    }

    public CodeGenStringBuilder rparen() {
        delegate.append(")");
        return this;
    }

    public CodeGenStringBuilder tab() {
        delegate.append("\t");
        return this;
    }

    public CodeGenStringBuilder lbrace() {
        delegate.append("{");
        return this;
    }

    public CodeGenStringBuilder rbrace() {
        delegate.append("}");
        return this;
    }

    public CodeGenStringBuilder space() {
        delegate.append(" ");
        return this;
    }

    public CodeGenStringBuilder larrow() {
        delegate.append(" <- ");
        return this;
    }

    public CodeGenStringBuilder quote() {
        delegate.append("\"");
        return this;
    }

    public CodeGenStringBuilder colon() {
        delegate.append(":");
        return this;
    }

    public CodeGenStringBuilder insert(int offset, CharSequence sequence) {
        delegate.insert(offset, sequence);
        return this;
    }
}
