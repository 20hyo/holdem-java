package poker.agent;

import poker.core.Card;

public interface IAgent {
	public ActionDecision decide(IGameState state);
	
	// 카드 관련 메서드들
	public void setHoleCards(Card[] holeCards);
	public Card[] getHoleCards();
	
	// 베팅 크기 결정 메서드들
	public int chooseBetTargetSetTo(int pot, int myStack, int minInc, double handStrength);
	public int chooseRaiseTargetSetTo(int pot, int toCall, int myCommitted, int myStack, int minInc, double handStrength);
	
	// 베팅 확률 및 크기 조정 메서드들
	/**
	 * 카드 패의 강도에 따른 베팅 확률 배수를 반환합니다.
	 * @param handStrength 카드 패의 강도 (0.0 ~ 1.0)
	 * @return 베팅 확률 배수
	 */
	public double getBetProbabilityMultiplier(double handStrength);
	
	/**
	 * 카드 패의 강도에 따른 베팅 크기 배수를 반환합니다.
	 * @param handStrength 카드 패의 강도 (0.0 ~ 1.0)
	 * @return 베팅 크기 배수
	 */
	public double getBetSizeMultiplier(double handStrength);
}
