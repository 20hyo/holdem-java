package poker.agent;

import java.util.List;
import java.util.Set;

import poker.core.Action;
import poker.core.Card;

public interface IGameState {

	public enum Street { PRE_FLOP, FLOP, TURN, RIVER }

	// 게임/포지션 정보
	Street getStreet();
	int getPlayerCount();
	int getButtonIndex();
	int getSeatIndex();
	int getActorIndex();

	// 칩/베팅 정보
	int getPotSize();
	int[] getStacks(); // 남은 스택
	int[] getCommitted(); // 현재 스트리트 커밋
	int getToCall();
	Integer getMinRaise();
	Set<Action> getLegalActions();

	// 카드 정보
	Card[] getHoleCards(); // 길이 2
	List<Card> getCommunityCards(); // 0~5장
}
