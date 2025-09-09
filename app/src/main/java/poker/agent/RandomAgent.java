package poker.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import poker.core.Action;
import poker.core.Card;

public class RandomAgent implements IAgent {

	private final Random random = new Random();
	private Card[] holeCards; // 에이전트의 홀카드

	@Override
	public ActionDecision decide(IGameState state) {
		Set<Action> legal = state.getLegalActions();
		int toCall = state.getToCall();
		int pot = state.getPotSize();
		int seat = state.getSeatIndex();
		int[] stacks = state.getStacks();
		int[] committed = state.getCommitted();
		int myCommitted = committed[seat];
		int myStack = stacks[seat];
		Integer minRaise = state.getMinRaise();
		int minInc = (minRaise != null && minRaise > 0) ? minRaise : 1;
		
		// 카드 패 가치 평가
		double handStrength = HandEvaluator.evaluateHandStrength(holeCards, state.getCommunityCards());

		if (toCall == 0) {
			// Check vs Bet mix depending on street and hand strength
			double baseBetProb = switch (state.getStreet()) {
				case PRE_FLOP -> 0.30;
				case FLOP -> 0.40;
				case TURN -> 0.45;
				case RIVER -> 0.50;
			};
			
			// 카드 패 가치에 따른 베팅 확률 조정
			double betProbMultiplier = getBetProbabilityMultiplier(handStrength);
			double betProb = baseBetProb * betProbMultiplier;
			betProb = Math.min(betProb, 1.0); // 최대 100%로 제한
			
			// 불법적인 액션 체크
			if (!legal.contains(Action.BET)) betProb = 0.0;
			if (!legal.contains(Action.CHECK)) betProb = 1.0; // only bet available
			
			if (random.nextDouble() < betProb && legal.contains(Action.BET)) {
				int target = chooseBetTargetSetTo(pot, myStack, minInc, handStrength);
				return ActionDecision.bet(target);
			} else if (legal.contains(Action.CHECK)) {
				return ActionDecision.check();
			} else {
				// 폴백: 불법적인 상황에서는 폴드
				return ActionDecision.fold();
			}
		} else {
			// Fold / Call / Raise / All-in based on pot odds, stack size, and hand strength
			double potOdds = (pot + toCall) > 0 ? (double) toCall / (double) (pot + toCall) : 0.0;
			double baseCallProb = clamp(1.0 - potOdds, 0.10, 0.90);
			
			// 카드 패 가치에 따른 콜/레이즈 확률 조정
			double handStrengthMultiplier = getBetProbabilityMultiplier(handStrength);
			double callProb = baseCallProb * handStrengthMultiplier;
			callProb = Math.min(callProb, 0.95); // 최대 95%로 제한
			
			double raiseProb = legal.contains(Action.RAISE) ? 0.15 * handStrengthMultiplier : 0.0;
			raiseProb = Math.min(raiseProb, 0.3); // 최대 30%로 제한
			
			double allInProb = legal.contains(Action.ALL_IN) ? 0.02 * handStrengthMultiplier : 0.0;
			allInProb = Math.min(allInProb, 0.1); // 최대 10%로 제한
			
			double foldProb = Math.max(0.0, 1.0 - callProb - raiseProb - allInProb);
			
			List<Weighted<Action>> choices = new ArrayList<Weighted<Action>>();
			if (legal.contains(Action.FOLD)) choices.add(new Weighted<Action>(Action.FOLD, foldProb));
			if (legal.contains(Action.CALL)) choices.add(new Weighted<Action>(Action.CALL, callProb));
			if (legal.contains(Action.RAISE)) choices.add(new Weighted<Action>(Action.RAISE, raiseProb));
			if (legal.contains(Action.ALL_IN)) choices.add(new Weighted<Action>(Action.ALL_IN, allInProb));
			
			Action chosen = weightedPick(choices, Action.CALL);
			switch (chosen) {
				case FOLD:
					return ActionDecision.fold();
				case CALL:
					return ActionDecision.call();
				case RAISE: {
					int target = chooseRaiseTargetSetTo(pot, toCall, myCommitted, myStack, minInc, handStrength);
					return ActionDecision.raise(target);
				}
				case ALL_IN:
					return ActionDecision.allIn();
				default:
					return ActionDecision.call();
			}
		}
	}

	@Override
	public int chooseBetTargetSetTo(int pot, int myStack, int minInc, double handStrength) {
		// choose from half-pot, two-thirds, pot; set-to absolute committed for this street
		int[] options = new int[] { 
			(int) Math.max(minInc, Math.round(pot * 0.5)), 
			(int) Math.max(minInc, Math.round(pot * 0.66)), 
			Math.max(minInc, pot) 
		};
		int pick = options[random.nextInt(options.length)];
		
		// 카드 패 가치에 따른 베팅 크기 조정
		double sizeMultiplier = getBetSizeMultiplier(handStrength);
		int adjustedPick = (int) Math.round(pick * sizeMultiplier);
		
		return Math.max(minInc, Math.min(adjustedPick, myStack));
	}

	@Override
	public int chooseRaiseTargetSetTo(int pot, int toCall, int myCommitted, int myStack, int minInc, double handStrength) {
		// 현재 테이블의 최대 커밋 금액을 기준으로 레이즈 계산
		// toCall은 내가 콜해야 할 금액이므로, 현재 최대 커밋 = toCall + myCommitted
		int currentMaxCommitted = toCall + myCommitted;
		
		// 레이즈 옵션들
		int minTarget = currentMaxCommitted + minInc;  // 최소 레이즈
		int potRaise = currentMaxCommitted + Math.max(minInc, pot / 2);  // 팟 사이즈의 절반 레이즈
		
		// 80% 확률로 최소 레이즈, 20% 확률로 팟 레이즈
		int target = (random.nextDouble() < 0.8) ? minTarget : potRaise;
		
		// 카드 패 가치에 따른 레이즈 크기 조정
		double sizeMultiplier = getBetSizeMultiplier(handStrength);
		int adjustedTarget = (int) Math.round(target * sizeMultiplier);
		
		// 올인 제한 적용
		int maxTarget = myCommitted + myStack;
		return Math.min(adjustedTarget, maxTarget);
	}

	private static double clamp(double v, double lo, double hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	private <T> T weightedPick(List<Weighted<T>> items, T fallback) {
		double sum = 0.0;
		for (Weighted<T> w : items) sum += w.weight;
		if (sum <= 0.0) return fallback;
		double r = random.nextDouble() * sum;
		double acc = 0.0;
		for (Weighted<T> w : items) {
			acc += w.weight;
			if (r <= acc) return w.value;
		}
		return fallback;
	}

	/**
	 * 카드 패의 가치를 평가합니다 (0.0 ~ 1.0)
	 * 0.0: 매우 약한 패, 1.0: 매우 강한 패
	 */
	@Override
	public void setHoleCards(Card[] holeCards) {
		this.holeCards = holeCards;
	}
	
	@Override
	public Card[] getHoleCards() {
		return holeCards;
	}

	/**
	 * 카드 패의 강도에 따른 베팅 확률 배수를 반환합니다.
	 * 강한 패일수록 베팅할 확률이 높아집니다.
	 * @param handStrength 카드 패의 강도 (0.0 ~ 1.0)
	 * @return 베팅 확률 배수 (0.5 ~ 2.0)
	 */
	@Override
	public double getBetProbabilityMultiplier(double handStrength) {
		// handStrength가 0.0일 때 0.5배, 1.0일 때 2.0배
		// 선형적으로 증가하는 배수
		return 0.5 + (handStrength * 1.5);
	}
	
	/**
	 * 카드 패의 강도에 따른 베팅 크기 배수를 반환합니다.
	 * 강한 패일수록 더 큰 베팅을 합니다.
	 * @param handStrength 카드 패의 강도 (0.0 ~ 1.0)
	 * @return 베팅 크기 배수 (0.5 ~ 2.0)
	 */
	@Override
	public double getBetSizeMultiplier(double handStrength) {
		// handStrength가 0.0일 때 0.5배, 1.0일 때 2.0배
		// 선형적으로 증가하는 배수
		return 0.5 + (handStrength * 1.5);
	}

	private static class Weighted<T> {
		final T value;
		final double weight;
		Weighted(T v, double w) { this.value = v; this.weight = w; }
	}
}

