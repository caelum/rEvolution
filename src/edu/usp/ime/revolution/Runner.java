package edu.usp.ime.revolution;

import java.io.FileInputStream;
import java.io.InputStream;

import edu.usp.ime.revolution.config.Config;
import edu.usp.ime.revolution.config.PropertiesConfig;

public class Runner {
	public static void main(String[] args) throws Exception {
		if(args.length==0) throw new Exception("missing config file");
		
		InputStream configStream = new FileInputStream(args[0]);
		Config config = new PropertiesConfig(configStream);
		
		new RevolutionFactory().basedOn(config).start();
	}
}
