package poker.agent;

import java.io.Serializable;

import poker.core.Action;

public class ActionDecision implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Action action;
	private final Integer amount; // set-to amount for BET/RAISE, null otherwise

	public ActionDecision(Action action, Integer amount) {
		this.action = action;
		this.amount = amount;
	}

	public static ActionDecision fold() { return new ActionDecision(Action.FOLD, null); }
	public static ActionDecision check() { return new ActionDecision(Action.CHECK, null); }
	public static ActionDecision call() { return new ActionDecision(Action.CALL, null); }
	public static ActionDecision bet(int targetAmount) { return new ActionDecision(Action.BET, targetAmount); }
	public static ActionDecision raise(int targetAmount) { return new ActionDecision(Action.RAISE, targetAmount); }
	public static ActionDecision allIn() { return new ActionDecision(Action.ALL_IN, null); }

	public Action getAction() { return action; }
	public Integer getAmount() { return amount; }
}


