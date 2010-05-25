package edu.usp.ime.revolution;

import edu.usp.ime.revolution.analyzers.Analyzer;
import edu.usp.ime.revolution.scm.ChangeSetCollection;

public class Revolution {

	private final Analyzer analyzer;
	private final ChangeSetCollection collection;

	public Revolution(Analyzer analyzer, ChangeSetCollection collection) {
		this.analyzer = analyzer;
		this.collection = collection;
	}

	public void start() {
		analyzer.start(collection);
	}

}