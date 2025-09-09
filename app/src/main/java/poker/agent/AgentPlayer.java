package poker.agent;

import java.util.List;

import poker.core.Card;
import poker.core.IPlayer;
import poker.core.Ranking;

/**
 * IAgent를 사용하는 플레이어 클래스
 * 실제 카드와 에이전트의 의사결정을 연결하는 역할
 */
public class AgentPlayer implements IPlayer {
    private final IAgent agent;
    private Card[] hand;
    private Ranking rankingEnum = null;
    private List<Card> rankingList = null;
    private Card highCard = null;
    private boolean isActive = true;
    private int stack;
    private int committedThisStreet = 0;
    
    public AgentPlayer(IAgent agent, int initialStack) {
        this.agent = agent;
        this.stack = initialStack;
        this.hand = new Card[2]; // 홀덤은 2장의 홀카드
    }
    
    public IAgent getAgent() {
        return agent;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public int getStack() {
        return stack;
    }
    
    public void setStack(int stack) {
        this.stack = stack;
    }
    
    public int getCommittedThisStreet() {
        return committedThisStreet;
    }
    
    public void setCommittedThisStreet(int committed) {
        this.committedThisStreet = committed;
    }
    
    public void addToStack(int amount) {
        this.stack += amount;
    }
    
    public void subtractFromStack(int amount) {
        this.stack -= amount;
    }
    
    public void addCommitted(int amount) {
        this.committedThisStreet += amount;
    }
    
    public void resetCommitted() {
        this.committedThisStreet = 0;
    }
    
    // IPlayer 인터페이스 구현
    @Override
    public Card[] getCards() {
        return hand;
    }
    
    @Override
    public void setCard(int index, Card card) {
        this.hand[index] = card;
    }
    
    @Override
    public Ranking getRankingEnum() {
        return rankingEnum;
    }
    
    @Override
    public List<Card> getRankingList() {
        return rankingList;
    }
    
    @Override
    public Card getHighCard() {
        return highCard;
    }
    
    @Override
    public void setRankingEnum(Ranking rankingEnum) {
        this.rankingEnum = rankingEnum;
    }
    
    @Override
    public void setRankingList(List<Card> rankingList) {
        this.rankingList = rankingList;
    }
    
    @Override
    public void setHighCard(Card highCard) {
        this.highCard = highCard;
    }
    
    /**
     * 홀카드를 에이전트에게 설정
     */
    public void setHoleCards(Card[] holeCards) {
        this.hand = holeCards;
        this.agent.setHoleCards(holeCards);
    }
    
    /**
     * 에이전트의 의사결정을 가져옴
     */
    public ActionDecision decide(IGameState gameState) {
        return agent.decide(gameState);
    }
}
