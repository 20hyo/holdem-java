package poker.core;

import java.util.List;

public interface IPlayer {
    public Card[] getCards();
    public Ranking getRankingEnum();
    public List<Card> getRankingList();
    public Card getHighCard();
    public void setRankingEnum(Ranking ranking);
    public void setRankingList(List<Card> rankingList);
    public void setHighCard(Card highCard);
    public void setCard(int index, Card card);
}
