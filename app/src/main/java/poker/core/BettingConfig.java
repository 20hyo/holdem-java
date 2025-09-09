package poker.core;

import java.io.Serializable;

public class BettingConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int smallBlind;
	private final int bigBlind;
	private final int minRaise;

	public BettingConfig(int smallBlind, int bigBlind) {
		this.smallBlind = smallBlind;
		this.bigBlind = bigBlind;
		this.minRaise = bigBlind; // 기본 최소 레이즈 증분은 빅블라인드
	}

	public BettingConfig(int smallBlind, int bigBlind, int minRaise) {
		this.smallBlind = smallBlind;
		this.bigBlind = bigBlind;
		this.minRaise = minRaise;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public int getMinRaise() {
		return minRaise;
	}
}


