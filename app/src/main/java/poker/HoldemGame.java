package poker;

import java.util.ArrayList;
import java.util.List;

import poker.agent.ActionDecision;
import poker.agent.AgentPlayer;
import poker.agent.HandEvaluator;
import poker.agent.IGameState;
import poker.agent.RandomAgent;
import poker.core.Action;
import poker.core.BettingConfig;
import poker.core.BettingEngine;
import poker.core.BettingState;
import poker.core.Card;
import poker.core.Deck;
import poker.core.GameLogger;
import poker.core.IDeck;
import poker.core.Ranking;

/**
 * 완전한 텍사스 홀덤 게임을 구현하는 클래스
 * 카드 분배, 베팅 라운드, 핸드 평가, 쇼다운을 모두 포함
 */
public class HoldemGame {
    private static final int NUM_PLAYERS = 6;
    private static final int INITIAL_STACK = 10000;
    private static final int SMALL_BLIND = 50;
    private static final int BIG_BLIND = 100;
    
    private AgentPlayer[] players;
    private IDeck deck;
    private List<Card> communityCards;
    private BettingState bettingState;
    private BettingEngine bettingEngine;
    private GameLogger logger;
    private int dealerPosition;
    private int smallBlindPosition;
    private int bigBlindPosition;
    private int currentPlayer;
    
    public HoldemGame() {
        initializeGame();
    }
    
    /**
     * 특정 에이전트들로 게임을 초기화하는 생성자
     * @param agents 사용할 에이전트 배열 (6개여야 함)
     */
    public HoldemGame(RandomAgent[] agents) {
        if (agents == null || agents.length != NUM_PLAYERS) {
            throw new IllegalArgumentException("에이전트 배열은 " + NUM_PLAYERS + "개여야 합니다.");
        }
        initializeGameWithAgents(agents);
    }
    
    private void initializeGame() {
        // 로거 초기화
        logger = new GameLogger();
        
        // 플레이어 초기화 - 모든 플레이어를 RandomAgent로 설정
        players = new AgentPlayer[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            RandomAgent agent = new RandomAgent();
            players[i] = new AgentPlayer(agent, INITIAL_STACK);
        }
        
        // 덱 초기화
        deck = new Deck();
        communityCards = new ArrayList<>();
        
        // 포지션 설정
        dealerPosition = 0;
        smallBlindPosition = 1;
        bigBlindPosition = 2;
        currentPlayer = 3; // UTG (Under The Gun)
        
        // 베팅 상태 초기화
        BettingConfig config = new BettingConfig(SMALL_BLIND, BIG_BLIND);
        int[] stacks = new int[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            stacks[i] = players[i].getStack();
        }
        
        bettingState = new BettingState(config, NUM_PLAYERS, dealerPosition, stacks);
        bettingEngine = new BettingEngine(bettingState);
    }
    
    private void initializeGameWithAgents(RandomAgent[] agents) {
        // 로거 초기화
        logger = new GameLogger();
        
        // 플레이어 초기화 - 지정된 에이전트들로 설정
        players = new AgentPlayer[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            players[i] = new AgentPlayer(agents[i], INITIAL_STACK);
        }
        
        // 덱 초기화
        deck = new Deck();
        communityCards = new ArrayList<>();
        
        // 포지션 설정
        dealerPosition = 0;
        smallBlindPosition = 1;
        bigBlindPosition = 2;
        currentPlayer = 3; // UTG (Under The Gun)
        
        // 베팅 상태 초기화
        BettingConfig config = new BettingConfig(SMALL_BLIND, BIG_BLIND);
        int[] stacks = new int[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            stacks[i] = players[i].getStack();
        }
        
        bettingState = new BettingState(config, NUM_PLAYERS, dealerPosition, stacks);
        bettingEngine = new BettingEngine(bettingState);
    }
    
    public void playHand() {
        logger.log("새로운 핸드 시작 - 딜러: " + getPlayerName(dealerPosition));
        
        // 1. 블라인드 포스팅
        postBlinds();
        
        // 2. 홀카드 분배
        dealHoleCards();
        
        // 3. 프리플랍 베팅
        playBettingRound("프리플랍");
        
        if (isHandOver()) {
            // 한 명만 남았어도 팟 분배는 해야 함
            distributePot();
            cleanupHand();
            movePositions();
            return;
        }
        
        // 4. 플랍 (3장)
        dealFlop();
        playBettingRound("플랍");
        
        if (isHandOver()) {
            distributePot();
            cleanupHand();
            movePositions();
            return;
        }
        
        // 5. 턴 (1장)
        dealTurn();
        playBettingRound("턴");
        
        if (isHandOver()) {
            distributePot();
            cleanupHand();
            movePositions();
            return;
        }
        
        // 6. 리버 (1장)
        dealRiver();
        playBettingRound("리버");
        
        // 7. 쇼다운
        if (!isHandOver()) {
            showDown();
        }
        
        // 8. 포트 분배 및 정리
        distributePot();
        cleanupHand();
        
        // 9. 포지션 이동
        movePositions();
    }
    
    private void postBlinds() {
        // 스몰 블라인드
        int smallBlindAmount = Math.min(SMALL_BLIND, players[smallBlindPosition].getStack());
        players[smallBlindPosition].subtractFromStack(smallBlindAmount);
        players[smallBlindPosition].addCommitted(smallBlindAmount);
        bettingState.commitDelta(smallBlindPosition, smallBlindAmount);
        
        // 빅 블라인드
        int bigBlindAmount = Math.min(BIG_BLIND, players[bigBlindPosition].getStack());
        players[bigBlindPosition].subtractFromStack(bigBlindAmount);
        players[bigBlindPosition].addCommitted(bigBlindAmount);
        bettingState.commitDelta(bigBlindPosition, bigBlindAmount);
        
        logger.logBlinds(smallBlindAmount, bigBlindAmount, 
            getPlayerName(smallBlindPosition), getPlayerName(bigBlindPosition));
        
        // 스택 업데이트
        updatePlayerStacks();
    }
    
    private void dealHoleCards() {
        deck.shuffle();
        
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < NUM_PLAYERS; j++) {
                if (players[j].isActive()) {
                    Card card = deck.drawCard();
                    players[j].setCard(i, card);
                }
            }
        }
        
        // 에이전트에게 홀카드 설정
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (players[i].isActive()) {
                players[i].setHoleCards(players[i].getCards());
                logger.logHoleCards(getPlayerName(i), 
                    players[i].getCards()[0].toString(), 
                    players[i].getCards()[1].toString());
            }
        }
    }
    
    private void dealFlop() {
        deck.drawCard(); // 번 카드
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.drawCard());
        }
        logger.logCommunityCards("플랍", communityCards.get(0) + " " + 
            communityCards.get(1) + " " + communityCards.get(2));
    }
    
    private void dealTurn() {
        deck.drawCard(); // 번 카드
        communityCards.add(deck.drawCard());
        logger.logCommunityCards("턴", communityCards.get(0) + " " + 
            communityCards.get(1) + " " + communityCards.get(2) + " " + communityCards.get(3));
    }
    
    private void dealRiver() {
        deck.drawCard(); // 번 카드
        communityCards.add(deck.drawCard());
        logger.logCommunityCards("리버", communityCards.get(0) + " " + 
            communityCards.get(1) + " " + communityCards.get(2) + " " + 
            communityCards.get(3) + " " + communityCards.get(4));
    }
    
    private void playBettingRound(String roundName) {
        int actionCount = 0;
        int maxActions = 50; // 무한 루프 방지
        IGameState.Street initialStreet = bettingState.getStreet();
        
        while (!bettingState.isStreetClosed() && 
               bettingState.getStreet() == initialStreet && 
               actionCount < maxActions) {
            int currentPlayerIndex = bettingState.getActorIndex();
            AgentPlayer player = players[currentPlayerIndex];
            
            if (!player.isActive()) {
                bettingEngine.apply(Action.FOLD, null);
                actionCount++;
                continue;
            }
            
            // 에이전트의 의사결정
            ActionDecision decision = player.decide(bettingState);
            Action action = decision.getAction();
            Integer amount = decision.getAmount();
            
            // 액션 로그
            String actionDesc = getActionDescription(action, amount);
            logger.logAction(getPlayerName(currentPlayerIndex), actionDesc);
            
            // 액션 적용 전 스택 상태 저장
            int stackBefore = player.getStack();
            
            // 액션 적용
            bettingEngine.apply(action, amount);
            
            // 플레이어 스택 업데이트 (베팅 엔진이 적용한 액션에 따라)
            updatePlayerStackAfterAction(player, action, amount, stackBefore);
            
            // 플레이어가 올인했는지 확인
            if (player.getStack() == 0 && stackBefore > 0) {
                logger.log(getPlayerName(currentPlayerIndex) + " 올인!");
            }
            
            actionCount++;
            
            // 게임 종료 조건 확인
            if (isHandOver()) {
                break;
            }
        }
        
        // 스택 업데이트
        updatePlayerStacks();
        
        if (actionCount >= maxActions) {
            logger.log("⚠️ 최대 액션 수에 도달하여 라운드를 종료합니다.");
        }
    }
    
    private void showDown() {
        // 활성 플레이어들의 핸드 평가
        List<AgentPlayer> activePlayers = new ArrayList<>();
        for (AgentPlayer player : players) {
            if (player.isActive()) {
                activePlayers.add(player);
            }
        }
        
        if (activePlayers.size() <= 1) {
            logger.log("쇼다운할 플레이어가 없습니다.");
            return;
        }
        
        // 각 플레이어의 핸드 평가
        for (AgentPlayer player : activePlayers) {
            // 간단한 핸드 평가 (실제로는 더 정교한 로직 필요)
            double handStrength = HandEvaluator.evaluateHandStrength(player.getCards(), communityCards);
            Ranking ranking = getRankingFromStrength(handStrength);
            player.setRankingEnum(ranking);
            
            logger.logShowdown(getPlayerName(getPlayerIndex(player)), 
                player.getCards()[0] + " " + player.getCards()[1], 
                ranking.toString(), handStrength);
        }
        
        // 승자 결정
        AgentPlayer winner = determineWinner(activePlayers);
        if (winner != null) {
            logger.logWinner(getPlayerName(getPlayerIndex(winner)), winner.getRankingEnum().toString());
        }
    }
    
    private Ranking getRankingFromStrength(double handStrength) {
        if (handStrength >= 0.9) return Ranking.ROYAL_FLUSH;
        if (handStrength >= 0.8) return Ranking.STRAIGHT_FLUSH;
        if (handStrength >= 0.7) return Ranking.FOUR_OF_A_KIND;
        if (handStrength >= 0.6) return Ranking.FULL_HOUSE;
        if (handStrength >= 0.5) return Ranking.FLUSH;
        if (handStrength >= 0.4) return Ranking.STRAIGHT;
        if (handStrength >= 0.3) return Ranking.THREE_OF_A_KIND;
        if (handStrength >= 0.2) return Ranking.TWO_PAIR;
        if (handStrength >= 0.1) return Ranking.ONE_PAIR;
        return Ranking.HIGH_CARD;
    }
    
    private AgentPlayer determineWinner(List<AgentPlayer> activePlayers) {
        if (activePlayers.isEmpty()) return null;
        if (activePlayers.size() == 1) return activePlayers.get(0);
        
        // 간단한 승자 결정 (실제로는 더 복잡한 로직 필요)
        AgentPlayer winner = activePlayers.get(0);
        for (AgentPlayer player : activePlayers) {
            if (player.getRankingEnum().ordinal() > winner.getRankingEnum().ordinal()) {
                winner = player;
            }
        }
        return winner;
    }
    
    private void distributePot() {
        int potSize = bettingState.getPotSize();
        if (potSize == 0) return;
        
        // 폴드하지 않은 플레이어 중 첫 번째에게 팟 지급
        boolean[] folded = bettingState.getFolded();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (!folded[i] && players[i].isActive()) {
                bettingState.addToStack(i, potSize);
                players[i].setStack(bettingState.getPlayerStacks()[i]);
                logger.logPotDistribution(potSize, getPlayerName(i));
                break;
            }
        }
    }
    
    private void cleanupHand() {
        // 커뮤니티 카드 초기화
        communityCards.clear();
        
        // 덱 리셋 (새로운 핸드를 위해)
        deck.reset();
        
        // 플레이어 상태 초기화
        for (int i = 0; i < NUM_PLAYERS; i++) {
            players[i].resetCommitted();
            // 스택이 0인 플레이어는 비활성화
            if (bettingState.getPlayerStacks()[i] == 0) {
                players[i].setActive(false);
                logger.log(getPlayerName(i) + "는 스택이 0이 되어 비활성화됨");
            }
            // AgentPlayer의 스택을 BettingState와 동기화
            players[i].setStack(bettingState.getPlayerStacks()[i]);
            // 홀카드는 다음 핸드에서 새로 받을 때까지 유지
        }
        
        // 베팅 상태 초기화
        bettingState = new BettingState(
            new BettingConfig(SMALL_BLIND, BIG_BLIND), 
            NUM_PLAYERS, 
            dealerPosition, 
            getCurrentStacks()
        );
        bettingEngine = new BettingEngine(bettingState);
    }
    
    private void movePositions() {
        dealerPosition = (dealerPosition + 1) % NUM_PLAYERS;
        smallBlindPosition = (smallBlindPosition + 1) % NUM_PLAYERS;
        bigBlindPosition = (bigBlindPosition + 1) % NUM_PLAYERS;
        currentPlayer = (currentPlayer + 1) % NUM_PLAYERS;
    }
    
    private boolean isHandOver() {
        boolean[] folded = bettingState.getFolded();
        int activePlayers = 0;
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (!folded[i] && players[i].isActive()) {
                activePlayers++;
            }
        }
        return activePlayers <= 1;
    }
    
    /**
     * 액션 적용 후 플레이어의 스택을 업데이트 (BettingState 사용)
     * BettingEngine.apply()가 이미 BettingState를 업데이트했으므로 동기화만 수행
     */
    private void updatePlayerStackAfterAction(AgentPlayer player, Action action, Integer amount, int stackBefore) {
        int playerIndex = getPlayerIndex(player);
        
        // BettingEngine.apply()가 이미 BettingState의 스택과 커밋을 업데이트했으므로
        // AgentPlayer의 정보만 BettingState와 동기화
        player.setStack(bettingState.getPlayerStacks()[playerIndex]);
        player.setCommittedThisStreet(bettingState.getCommittedThisStreet()[playerIndex]);
    }
    
    private void updatePlayerStacks() {
        // BettingState의 스택 정보를 로그로 출력
        int[] bettingStacks = bettingState.getPlayerStacks();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            logger.logStackUpdate(getPlayerName(i), bettingStacks[i]);
        }
    }
    
    private int[] getCurrentStacks() {
        // BettingState에서 현재 스택 정보를 가져옴
        return bettingState.getPlayerStacks();
    }
    
    private int getPlayerIndex(AgentPlayer player) {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (players[i] == player) {
                return i;
            }
        }
        return -1;
    }
    
    private String getPlayerName(int index) {
        String[] names = {"P1", "P2", "P3", "P4", "P5", "P6"};
        return names[index];
    }
    
    private String getActionDescription(Action action, Integer amount) {
        switch (action) {
            case FOLD: return "FOLD";
            case CHECK: return "CHECK";
            case CALL: return "CALL";
            case BET: return "BET " + amount;
            case RAISE: return "RAISE " + amount;
            case ALL_IN: return "ALL-IN";
            default: return action.toString();
        }
    }
    
    public void playMultipleHands(int numHands) {
        logger.log(String.format("%d핸드 홀덤 게임 시작 - 플레이어: %d명, 초기 스택: %d칩, 블라인드: %d/%d", 
            numHands, NUM_PLAYERS, INITIAL_STACK, SMALL_BLIND, BIG_BLIND));
        
        for (int hand = 1; hand <= numHands; hand++) {
            logger.logHandStart(hand, getPlayerName(dealerPosition));
            playHand();
            
            // 게임 종료 조건 확인
            int activePlayers = 0;
            for (AgentPlayer player : players) {
                if (player.isActive() && player.getStack() > 0) {
                    activePlayers++;
                }
            }
            
            if (activePlayers <= 1) {
                logger.log("🏁 게임 종료! 한 명의 플레이어만 남았습니다.");
                break;
            }
        }
        
        // 최종 결과 출력
        printFinalResults();
        
        // 로그 파일 닫기
        logger.logGameEnd();
    }
    
    private void printFinalResults() {
        logger.log("🏆 최종 결과");
        for (int i = 0; i < NUM_PLAYERS; i++) {
            String status = players[i].isActive() ? "활성" : "비활성";
            logger.log(getPlayerName(i) + ": " + players[i].getStack() + "칩 (" + status + ")");
        }
    }
}