package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import edu.arizona.biosemantics.oto2.steps.shared.model.Term;

public class TermTreeNode extends TextTreeNode {

	private Term term;

	public TermTreeNode(Term term) {
		this.term = term;
	}
	
	@Override
	public String getText() {
		return term.getTerm();
	}

	public Term getTerm() {
		return term;
	}

	@Override
	public String getId() {
		return "term-" + term.getId();
	}
	
}