package poker.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import poker.agent.IGameState;

public class BettingState implements Serializable, IGameState {

	private static final long serialVersionUID = 1L;

	// IGameState의 Street enum을 사용
	private IGameState.Street street;

	private final BettingConfig config;
	private int potSize;
	private int playerCount;
	private int buttonIndex; // 버튼 좌석
	private int[] playerStacks; // 좌석 순서대로
	private int[] committedThisStreet; // 현재 스트리트 커밋 금액
	private boolean[] folded; // 폴드 여부
	private boolean[] allIn; // 올인 여부
	private int actorIndex; // 현재 액터 좌석
	private boolean streetClosed; // 스트리트 종료 여부
	private int roundStartIndex; // 라운드 시작 좌석(베팅/레이즈 이후의 다음 액터)
	private int lastRaiseSize; // 마지막 레이즈 증분(프리플랍 시작은 BB)

	public BettingState(BettingConfig config, int playerCount, int buttonIndex, int... stacks) {
		if (playerCount < 2 || playerCount > 6) throw new IllegalArgumentException("playerCount must be 2..6");
		if (stacks == null || stacks.length != playerCount) throw new IllegalArgumentException("stacks length must equal playerCount");
		this.config = config;
		this.street = IGameState.Street.PRE_FLOP;
		this.potSize = 0;
		this.playerCount = playerCount;
		this.buttonIndex = mod(buttonIndex, playerCount);
		this.playerStacks = Arrays.copyOf(stacks, stacks.length);
		this.committedThisStreet = new int[playerCount];
		this.folded = new boolean[playerCount];
		this.allIn = new boolean[playerCount];
		this.streetClosed = false;
		postBlindsMulti();
	}

	private int mod(int x, int m) { int r = x % m; return r < 0 ? r + m : r; }

	private int nextIndex(int idx) { return mod(idx + 1, playerCount); }

	private int smallBlindIndex() { return nextIndex(buttonIndex); }
	private int bigBlindIndex() { return nextIndex(smallBlindIndex()); }

	private void postBlindsMulti() {
		post(smallBlindIndex(), config.getSmallBlind());
		post(bigBlindIndex(), config.getBigBlind());
		// 프리플랍 액터는 버튼 다음 다음(UTG)
		actorIndex = nextIndex(bigBlindIndex());
		roundStartIndex = actorIndex;
		lastRaiseSize = config.getBigBlind();
	}

	void post(int player, int amount) {
		int pay = Math.min(amount, playerStacks[player]);
		playerStacks[player] -= pay;
		committedThisStreet[player] += pay;
		potSize += pay;
		if (playerStacks[player] == 0) allIn[player] = true;
	}

	public void commitDelta(int player, int amount) {
		if (amount <= 0) return;
		int pay = Math.min(amount, playerStacks[player]);
		playerStacks[player] -= pay;
		committedThisStreet[player] += pay;
		potSize += pay;
		if (playerStacks[player] == 0) allIn[player] = true;
	}

	public IGameState.Street getStreet() { return street; }
	public int getPotSize() { return potSize; }
	public int getActorIndex() { return actorIndex; }
	public int getPlayerCount() { return playerCount; }
	public int getButtonIndex() { return buttonIndex; }
	public int[] getPlayerStacks() { return playerStacks.clone(); }
	public int[] getCommittedThisStreet() { return committedThisStreet.clone(); }
	public int getCommittedThisStreetFor(int playerIndex) { return committedThisStreet[playerIndex]; }
	public boolean[] getFolded() { return folded.clone(); }
	public boolean[] getAllIn() { return allIn.clone(); }
	public boolean isStreetClosed() { return streetClosed; }
	public int getRoundStartIndex() { return roundStartIndex; }
	public int getLastRaiseSize() { return lastRaiseSize; }
	public void setLastRaiseSize(int size) { lastRaiseSize = Math.max(0, size); }
	
	/**
	 * 플레이어의 스택을 업데이트합니다.
	 * @param playerIndex 플레이어 인덱스
	 * @param newStack 새로운 스택 크기
	 */
	public void updatePlayerStack(int playerIndex, int newStack) {
		if (playerIndex < 0 || playerIndex >= playerCount) {
			throw new IllegalArgumentException("Invalid player index: " + playerIndex);
		}
		playerStacks[playerIndex] = Math.max(0, newStack);
		if (playerStacks[playerIndex] == 0) {
			allIn[playerIndex] = true;
		}
	}
	
	/**
	 * 플레이어의 스택에서 금액을 차감합니다.
	 * @param playerIndex 플레이어 인덱스
	 * @param amount 차감할 금액
	 * @return 실제 차감된 금액
	 */
	public int deductFromStack(int playerIndex, int amount) {
		if (playerIndex < 0 || playerIndex >= playerCount) {
			throw new IllegalArgumentException("Invalid player index: " + playerIndex);
		}
		int actualAmount = Math.min(amount, playerStacks[playerIndex]);
		playerStacks[playerIndex] -= actualAmount;
		if (playerStacks[playerIndex] == 0) {
			allIn[playerIndex] = true;
		}
		return actualAmount;
	}
	
	/**
	 * 플레이어의 스택에 금액을 추가합니다.
	 * @param playerIndex 플레이어 인덱스
	 * @param amount 추가할 금액
	 */
	public void addToStack(int playerIndex, int amount) {
		if (playerIndex < 0 || playerIndex >= playerCount) {
			throw new IllegalArgumentException("Invalid player index: " + playerIndex);
		}
		playerStacks[playerIndex] += amount;
		allIn[playerIndex] = false; // 스택이 생기면 올인 상태 해제
	}
	
	/**
	 * 모든 플레이어의 스택을 업데이트합니다.
	 * @param newStacks 새로운 스택 배열
	 */
	public void updateAllStacks(int[] newStacks) {
		if (newStacks == null || newStacks.length != playerCount) {
			throw new IllegalArgumentException("Stacks array must have length " + playerCount);
		}
		for (int i = 0; i < playerCount; i++) {
			playerStacks[i] = Math.max(0, newStacks[i]);
			allIn[i] = (playerStacks[i] == 0);
		}
	}
	public int getMaxCommittedThisStreet() {
		int max = 0;
		for (int v : committedThisStreet) max = Math.max(max, v);
		return max;
	}

	public int getToCallFor(int playerIndex) {
		int max = 0;
		for (int v : committedThisStreet) max = Math.max(max, v);
		return Math.max(0, max - committedThisStreet[playerIndex]);
	}

	public int getToCall() { return getToCallFor(actorIndex); }

	public Integer getMinRaise() { return config.getMinRaise(); }

	public void nextStreet() {
		this.committedThisStreet = new int[playerCount];
		this.streetClosed = false;
		switch (street) {
			case PRE_FLOP: street = IGameState.Street.FLOP; break;
			case FLOP: street = IGameState.Street.TURN; break;
			case TURN: street = IGameState.Street.RIVER; break;
			case RIVER: streetClosed = true; break;
		}
		if (streetClosed) return;
		setRoundStartToNextOf(actorIndex);
		actorIndex = roundStartIndex;
		lastRaiseSize = 0; // 스트리트 시작 시 초기화. 첫 베팅 시 설정됨
		advanceToNextActorIfNeeded();
	}

	public void rotateActor() {
		actorIndex = nextIndex(actorIndex);
		advanceToNextActorIfNeeded();
	}

	public void markFold(int player) { folded[player] = true; }

	public void setRoundStartToNextOf(int player) {
		int idx = nextIndex(player);
		int tries = 0;
		while (tries < playerCount && (folded[idx] || allIn[idx])) {
			idx = nextIndex(idx);
			tries++;
		}
		roundStartIndex = idx;
	}

	private void advanceToNextActorIfNeeded() {
		int tries = 0;
		while (tries < playerCount && (folded[actorIndex] || allIn[actorIndex])) {
			actorIndex = nextIndex(actorIndex);
			tries++;
		}
	}

	// IGameState 인터페이스 구현
	@Override
	public int getSeatIndex() {
		return actorIndex;
	}

	@Override
	public int[] getStacks() {
		return getPlayerStacks();
	}

	@Override
	public int[] getCommitted() {
		return getCommittedThisStreet();
	}

	@Override
	public Set<Action> getLegalActions() {
		// BettingEngine의 getLegalActions 로직을 여기서 구현
		int toCall = getToCall();
		int actorIndex = getActorIndex();
		
		
		EnumSet<Action> actions = EnumSet.noneOf(Action.class);
		actions.add(Action.FOLD);
		if (toCall == 0) {
			// 아직 베팅이 없는 상황: CHECK 또는 BET 가능
			actions.add(Action.CHECK);
			actions.add(Action.BET);
		} else {
			// 베팅이 있는 상황
			actions.add(Action.CALL);
			
			// 올인된 플레이어가 있으면 RAISE, ALL_IN 불가
			if (!hasAllInPlayer()) {
				actions.add(Action.RAISE);
				actions.add(Action.ALL_IN);
			}
		}
		return actions;
	}

	@Override
	public Card[] getHoleCards() {
		// 임시로 빈 카드 배열 반환 (실제 구현에서는 플레이어별 홀카드 관리 필요)
		return new Card[2];
	}

	@Override
	public List<Card> getCommunityCards() {
		// 임시로 빈 리스트 반환 (실제 구현에서는 커뮤니티 카드 관리 필요)
		return Collections.emptyList();
	}

	private boolean hasAllInPlayer() {
		boolean[] folded = getFolded();
		boolean[] allIn = getAllIn();
		int[] stacks = getPlayerStacks();
		
		for (int i = 0; i < getPlayerCount(); i++) {
			if (!folded[i] && (allIn[i] || stacks[i] == 0)) {
				return true;
			}
		}
		return false;
	}
}


