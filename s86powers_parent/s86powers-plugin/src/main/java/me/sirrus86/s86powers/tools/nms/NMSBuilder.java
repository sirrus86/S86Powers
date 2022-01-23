package me.sirrus86.s86powers.tools.nms;

import java.io.File;

public class NMSBuilder {

	public void buildSource() throws Exception {
		
		File sourceFile = File.createTempFile("NMSLibrary", ".java");
		sourceFile.deleteOnExit();
		
		String className = sourceFile.getName().split("\\.")[0];
		String sourceCode = "public class " + className + " extends me.sirrus86.s86powers.tools.nms.NMSLibrary {\n";
		
		
	}
	
}
