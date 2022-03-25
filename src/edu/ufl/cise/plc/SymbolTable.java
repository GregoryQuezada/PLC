package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;

import java.util.HashMap;

public class SymbolTable {

//TODO:  Implement a symbol table class that is appropriate for this language.

    HashMap<String, Declaration> entries = new HashMap<String, Declaration>();
    String programName = "";

    public boolean insert(String name, Declaration declaration) throws TypeCheckException {
        if (name != programName) {
            return (entries.putIfAbsent(name, declaration)) == null;
        }
        throw new TypeCheckException("Name cannot match program name");
    }

    public void insertProgramName(String programN) {
        programName = programN;
    }

    public Declaration lookup(String name) {
        return entries.get(name);
    }

    public void remove(String name) {
        entries.remove(name);
    }



}
