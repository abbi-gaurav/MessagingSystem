package com.asl.console;

import java.io.IOException;

public interface Printable {

	public abstract void print() throws IOException;
	
	public void printHelpMessage();

	public abstract Printable getPrinter(String choice);
}