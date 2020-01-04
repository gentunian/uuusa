package org.grupolys.samulan.analyser;

import org.grupolys.samulan.rule.RulesManager;

public abstract class RuleBasedAnalyser implements Analyser {
	
	protected AnalyserConfiguration ac;
	protected RulesManager rm;
	
	public RuleBasedAnalyser(AnalyserConfiguration ac, RulesManager rm) {
		this.ac = ac;
		this.rm = rm;
	}

	public AnalyserConfiguration getAc() {
		return ac;
	}

	public void setAc(AnalyserConfiguration ac) {
		this.ac = ac;
	}

	public RulesManager getRm() {
		return rm;
	}

	public void setRm(RulesManager rm) {
		this.rm = rm;
	}
	
}
