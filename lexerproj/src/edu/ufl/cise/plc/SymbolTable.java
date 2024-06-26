package edu.ufl.cise.plc;

import java.util.HashMap;

import edu.ufl.cise.plc.ast.Declaration;

public class SymbolTable {

	HashMap<String, Declaration> entries = new HashMap<>(); 
	
	@SuppressWarnings("exports")
	public boolean insert(String name, Declaration dec){
		return (entries.putIfAbsent(name, dec)==null); 
	}
	
	@SuppressWarnings("exports")
	public Declaration lookup(String name) {
		return entries.get(name); 
	}
}
