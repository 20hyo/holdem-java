package poker.core;

public class BettingEngine {

	private final BettingState state;

	public BettingEngine(BettingState state) {
		this.state = state;
	}

	public void apply(Action action, Integer amount) {
		int actor = state.getActorIndex();
		switch (action) {
			case FOLD:
				state.markFold(actor);
				if (isOnlyOnePlayerLeft()) {
					state.nextStreet();
				} else {
					state.rotateActor();
				}
				break;
			case CHECK:
				if (isRoundClosedAfter(actor)) {
					state.nextStreet();
				} else {
					state.rotateActor();
				}
				break;
			case CALL:
				commit(actor, state.getToCall());
				if (isRoundClosedAfter(actor)) {
					state.nextStreet();
				} else {
					state.rotateActor();
				}
				break;
			case BET:
				int bet = (amount != null) ? amount : state.getMinRaise();
				// set-to semantics: commit up to target this-street amount
				int[] committedB = state.getCommittedThisStreet();
				int currentMax = state.getMaxCommittedThisStreet();
				int targetBet = Math.max(bet, currentMax + state.getMinRaise()); // 최소 베팅은 minRaise 이상
				int payB = Math.max(0, targetBet - committedB[actor]);
				commit(actor, payB);
				state.setLastRaiseSize(targetBet - currentMax);
				state.setRoundStartToNextOf(actor);
				state.rotateActor();
				break;
			case RAISE:
				int toCall = state.getToCall();
				int minRaise = state.getMinRaise();
				int lastRaise = Math.max(state.getLastRaiseSize(), minRaise);
				int target = (amount != null) ? amount : (state.getCommittedThisStreet()[actor] + toCall + lastRaise);
				// set-to semantics: bring actor's committed to target
				int[] committedR = state.getCommittedThisStreet();
				int currentMaxR = state.getMaxCommittedThisStreet();
				// 최소 레이즈: 새로운 최대 커밋이 기존 최대 + lastRaise 이상이어야 함
				int minTarget = currentMaxR + lastRaise;
				if (target < minTarget) {
					// 레이즈 금액이 최소 레이즈보다 작으면 콜로 처리
					commit(actor, toCall);
					// 콜로 처리되더라도 라운드가 계속되므로 rotateActor만 호출
					state.rotateActor();
					break;
				}
				int payR = Math.max(0, target - committedR[actor]);
				commit(actor, payR);
				state.setLastRaiseSize(target - currentMaxR);
				state.setRoundStartToNextOf(actor);
				state.rotateActor();
				break;
			case ALL_IN:
				// 모든 남은 스택을 베팅
				int allInAmount = state.getPlayerStacks()[actor];
				commit(actor, allInAmount);
				state.setLastRaiseSize(Math.max(allInAmount, state.getLastRaiseSize()));
				state.setRoundStartToNextOf(actor);
				state.rotateActor();
				break;
			default:
				throw new IllegalArgumentException("Unsupported action: " + action);
		}
	}

	private void commit(int player, int amount) {
		state.commitDelta(player, amount);
	}

	private boolean isRoundClosedAfter(int lastActor) {
		int start = state.getRoundStartIndex();
		int pc = state.getPlayerCount();
		boolean[] folded = state.getFolded();
		boolean[] allIn = state.getAllIn();
		
		// 활성 플레이어들만 고려하여 라운드가 한 바퀴 돌았는지 확인
		boolean roundComplete = isRoundCompleteForActivePlayers(lastActor, start, folded, allIn);
		
		// 모든 활성 플레이어가 동일한 금액을 커밋했는지 확인
		boolean allCommitted = true;
		
		for (int i = 0; i < pc; i++) {
			if (!folded[i] && !allIn[i]) {
				int toCall = state.getToCallFor(i);
				// 활성 플레이어가 아직 콜해야 할 금액이 있으면 라운드가 끝나지 않음
				if (toCall > 0) {
					allCommitted = false;
					break;
				}
			}
		}
		
		boolean result = roundComplete && allCommitted;
		
		// 라운드가 한 바퀴 돌았고, 모든 활성 플레이어가 동일한 금액을 커밋했을 때만 라운드 종료
		return result;
	}
	
	/**
	 * 활성 플레이어들만 고려하여 라운드가 완료되었는지 확인
	 */
	private boolean isRoundCompleteForActivePlayers(int lastActor, int start, boolean[] folded, boolean[] allIn) {
		// 마지막 액터부터 시작점까지 활성 플레이어들이 모두 액션을 했는지 확인
		int current = nextIndex(lastActor);
		
		// 시작점에 도달할 때까지 반복
		while (current != start) {
			// 활성 플레이어가 있으면 아직 라운드가 완료되지 않음
			if (!folded[current] && !allIn[current]) {
				return false;
			}
			current = nextIndex(current);
		}
		
		// 시작점에 도달했고, 그 사이에 활성 플레이어가 없었다면 라운드 완료
		return true;
	}

	private int nextIndex(int idx) {
		int pc = state.getPlayerCount();
		int n = (idx + 1) % pc;
		return n < 0 ? n + pc : n;
	}

	private boolean isOnlyOnePlayerLeft() {
		int alive = 0;
		boolean[] f = state.getFolded();
		for (int i = 0; i < state.getPlayerCount(); i++) {
			if (!f[i]) alive++;
		}
		return alive <= 1;
	}

	public boolean isAllInSituation() {
		int activePlayers = 0;
		int allInPlayers = 0;
		boolean[] folded = state.getFolded();
		boolean[] allIn = state.getAllIn();
		int[] stacks = state.getPlayerStacks();
		
		for (int i = 0; i < state.getPlayerCount(); i++) {
			if (!folded[i]) {
				activePlayers++;
				if (allIn[i] || stacks[i] == 0) {
					allInPlayers++;
				}
			}
		}
		return activePlayers > 1 && allInPlayers == activePlayers;
	}
}


