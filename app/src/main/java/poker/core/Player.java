package poker.core;

import java.util.List;

public class Player implements IPlayer {
    private Card[] hand;
    private Ranking rankingEnum = null;
    private List<Card> rankingList = null;
    private Card highCard = null;

    public Player(Card[] hand) {
        this.hand = hand;
    }

    public Card[] getCards() {
        return hand;
    }

    public void setCard(int index, Card card) {
        this.hand[index] = card;
    }

    public Ranking getRankingEnum() {
        return rankingEnum;
    }

    public List<Card> getRankingList() {
        return rankingList;
    }

    public Card getHighCard() {
        return highCard;
    }

    public void setRankingEnum(Ranking rankingEnum) {
        this.rankingEnum = rankingEnum;
    }

    public void setRankingList(List<Card> rankingList) {
        this.rankingList = rankingList;
    }

    public void setHighCard(Card highCard) {
        this.highCard = highCard;
    }
}
