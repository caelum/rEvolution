package edu.usp.ime.revolution.analyzers;

import java.util.ArrayList;
import java.util.List;

import edu.usp.ime.revolution.analyzers.observers.AnalyzerObserver;
import edu.usp.ime.revolution.builds.Build;
import edu.usp.ime.revolution.builds.BuildResult;
import edu.usp.ime.revolution.metrics.MetricSet;
import edu.usp.ime.revolution.metrics.MetricSetFactory;
import edu.usp.ime.revolution.scm.ChangeSet;
import edu.usp.ime.revolution.scm.ChangeSetCollection;
import edu.usp.ime.revolution.tools.MetricTool;

public class DefaultAnalyzer implements Analyzer {

	private final Build build;
	private final List<MetricTool> tools;
	private final MetricSetFactory metricSetFactory;
	private final List<AnalyzerObserver> observers;

	public DefaultAnalyzer(Build build, MetricSetFactory metricSetFactory, List<MetricTool> tools) {
		this.build = build;
		this.metricSetFactory = metricSetFactory;
		this.tools = tools;
		this.observers = new ArrayList<AnalyzerObserver>();
	}

	public void start(ChangeSetCollection collection) {
		for(ChangeSet changeSet : collection) {
			BuildResult currentBuild = build.build(changeSet);
			MetricSet metricSet = metricSetFactory.setFor(changeSet);
			
			for(MetricTool tool : tools) {
				tool.calculate(changeSet, currentBuild, metricSet);
			}
			
			notifyAll(changeSet, metricSet);
		}
	}

	public void addObserver(AnalyzerObserver observer) {
		observers.add(observer);
	}

	private void notifyAll(ChangeSet cs, MetricSet set) {
		for(AnalyzerObserver observer : observers) {
			observer.notify(cs, set);
		}
	}

}