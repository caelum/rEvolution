package edu.usp.ime.revolution.scm.git;

import edu.usp.ime.revolution.config.Config;
import edu.usp.ime.revolution.executor.SysCommandExecutor;
import edu.usp.ime.revolution.scm.SCM;
import edu.usp.ime.revolution.scm.SpecificSCMFactory;

public class GitFactory implements SpecificSCMFactory {

	public SCM build(Config config) {
		return new Git(
				config.get("scm.repository"), 
				new DefaultGitLogParser(), 
				new SysCommandExecutor());
	}

}