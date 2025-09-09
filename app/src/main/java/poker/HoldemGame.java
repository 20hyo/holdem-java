package poker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import poker.core.RankingUtil;

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
    
    // 통계 수집용 변수들
    private Map<String, Integer> rankingCounts = new HashMap<>();
    private Map<String, Integer> winnerCounts = new HashMap<>();
    private int totalHandsPlayed = 0;
    private int totalShowdowns = 0;
    
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
        totalHandsPlayed++;
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
        logger.log("리버 베팅 라운드 종료 - 쇼다운 확인 중...");
        logger.log("isHandOver(): " + isHandOver());
        if (!isHandOver()) {
            logger.log("쇼다운을 시작합니다.");
            showDown();
        } else {
            logger.log("핸드가 종료되어 쇼다운을 건너뜁니다.");
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
        // 활성 플레이어들 중 폴드하지 않은 플레이어들의 핸드 평가
        List<AgentPlayer> showdownPlayers = new ArrayList<>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (players[i].isActive() && !bettingState.getFolded()[i]) {
                showdownPlayers.add(players[i]);
            }
        }
        
        if (showdownPlayers.size() <= 1) {
            logger.log("쇼다운할 플레이어가 없습니다.");
            return;
        }
        
        // 각 플레이어의 핸드 평가
        totalShowdowns++;
        logger.log("=== 쇼다운 시작 ===");
        logger.log("커뮤니티 카드: " + communityCards.toString());
        
        for (AgentPlayer player : showdownPlayers) {
            int playerIndex = getPlayerIndex(player);
            String playerName = getPlayerName(playerIndex);
            
            logger.log("--- " + playerName + " 핸드 평가 ---");
            logger.log("홀카드: " + player.getCards()[0] + " " + player.getCards()[1]);
            
            // RankingUtil을 사용하여 정확한 핸드 평가
            RankingUtil.checkRanking(player, communityCards);
            Ranking ranking = player.getRankingEnum();
            
            // 통계 수집
            String rankingName = ranking.toString();
            rankingCounts.put(rankingName, rankingCounts.getOrDefault(rankingName, 0) + 1);
            
            // HandEvaluator로 강도도 계산 (로깅용)
            double handStrength = HandEvaluator.evaluateHandStrength(player.getCards(), communityCards);
            
            logger.log(playerName + " 최종 랭킹: " + ranking.toString() + " (ordinal: " + ranking.ordinal() + ")");
            logger.log(playerName + " 핸드 강도: " + String.format("%.2f", handStrength));
            
            if (player.getRankingList() != null && !player.getRankingList().isEmpty()) {
                logger.log(playerName + " 랭킹 카드들: " + player.getRankingList().toString());
            }
            
            logger.logShowdown(playerName, 
                player.getCards()[0] + " " + player.getCards()[1], 
                ranking.toString(), handStrength);
        }
        
        // 승자 결정
        logger.log("=== 승자 결정 과정 ===");
        AgentPlayer winner = determineWinner(showdownPlayers);
        if (winner != null) {
            String winnerName = getPlayerName(getPlayerIndex(winner));
            logger.logWinner(winnerName, winner.getRankingEnum().toString());
            
            // 승자 통계 수집
            winnerCounts.put(winnerName, winnerCounts.getOrDefault(winnerName, 0) + 1);
        }
        logger.log("=== 쇼다운 종료 ===");
    }
    
    
    private AgentPlayer determineWinner(List<AgentPlayer> activePlayers) {
        if (activePlayers.isEmpty()) return null;
        if (activePlayers.size() == 1) return activePlayers.get(0);
        
        logger.log("승자 비교 시작 - " + activePlayers.size() + "명의 플레이어");
        
        // Ranking enum에서 낮은 ordinal 값이 더 높은 랭킹을 의미
        AgentPlayer winner = activePlayers.get(0);
        String winnerName = getPlayerName(getPlayerIndex(winner));
        logger.log("초기 승자: " + winnerName + " (" + winner.getRankingEnum().toString() + ", ordinal: " + winner.getRankingEnum().ordinal() + ")");
        
        for (int i = 1; i < activePlayers.size(); i++) {
            AgentPlayer player = activePlayers.get(i);
            String playerName = getPlayerName(getPlayerIndex(player));
            Ranking playerRanking = player.getRankingEnum();
            Ranking winnerRanking = winner.getRankingEnum();
            
            logger.log("비교: " + playerName + " (" + playerRanking.toString() + ", ordinal: " + playerRanking.ordinal() + 
                      ") vs " + winnerName + " (" + winnerRanking.toString() + ", ordinal: " + winnerRanking.ordinal() + ")");
            
            if (playerRanking.ordinal() < winnerRanking.ordinal()) {
                winner = player;
                winnerName = playerName;
                logger.log("새로운 승자: " + winnerName + " (" + playerRanking.toString() + ")");
            } else {
                logger.log("현재 승자 유지: " + winnerName + " (" + winnerRanking.toString() + ")");
            }
        }
        
        logger.log("최종 승자: " + winnerName + " (" + winner.getRankingEnum().toString() + ")");
        return winner;
    }
    
    private void distributePot() {
        int potSize = bettingState.getPotSize();
        if (potSize == 0) return;
        
        // 활성 플레이어들 중에서 승자 찾기
        List<AgentPlayer> activePlayers = new ArrayList<>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (players[i].isActive() && !bettingState.getFolded()[i]) {
                activePlayers.add(players[i]);
            }
        }
        
        if (activePlayers.isEmpty()) {
            logger.log("활성 플레이어가 없어 팟을 분배할 수 없습니다.");
            return;
        }
        
        if (activePlayers.size() == 1) {
            // 한 명만 남은 경우
            AgentPlayer winner = activePlayers.get(0);
            int winnerIndex = getPlayerIndex(winner);
            bettingState.addToStack(winnerIndex, potSize);
            players[winnerIndex].setStack(bettingState.getPlayerStacks()[winnerIndex]);
            logger.logPotDistribution(potSize, getPlayerName(winnerIndex));
        } else {
            // 여러 명이 남은 경우 쇼다운으로 승자 결정
            AgentPlayer winner = determineWinner(activePlayers);
            if (winner != null) {
                int winnerIndex = getPlayerIndex(winner);
                bettingState.addToStack(winnerIndex, potSize);
                players[winnerIndex].setStack(bettingState.getPlayerStacks()[winnerIndex]);
                logger.logPotDistribution(potSize, getPlayerName(winnerIndex));
            } else {
                logger.log("승자를 결정할 수 없어 팟을 분배할 수 없습니다.");
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
        logger.log("=== isHandOver() 체크 ===");
        for (int i = 0; i < NUM_PLAYERS; i++) {
            boolean isFolded = folded[i];
            boolean isActive = players[i].isActive();
            logger.log("P" + (i+1) + ": folded=" + isFolded + ", active=" + isActive);
            if (!folded[i] && players[i].isActive()) {
                activePlayers++;
            }
        }
        logger.log("활성 플레이어 수: " + activePlayers);
        boolean handOver = activePlayers <= 1;
        logger.log("핸드 종료 여부: " + handOver);
        return handOver;
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
        if (numHands < 0) {
            throw new IllegalArgumentException("핸드 수는 음수일 수 없습니다: " + numHands);
        }
        if (numHands == 0) {
            logger.log("0핸드 게임 - 게임을 종료합니다.");
            return;
        }
        
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
        
        // 게임 통계 출력
        printGameStatistics();
    }
    
    /**
     * 게임 통계를 출력하는 메서드
     */
    public void printGameStatistics() {
        logger.log("📊 게임 통계");
        logger.log("총 핸드 수: " + totalHandsPlayed);
        logger.log("총 쇼다운 수: " + totalShowdowns);
        
        if (!rankingCounts.isEmpty()) {
            logger.log("📈 랭킹 통계:");
            rankingCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.log("  " + entry.getKey() + ": " + entry.getValue() + "회"));
        }
        
        if (!winnerCounts.isEmpty()) {
            logger.log("🏆 승자 통계:");
            winnerCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.log("  " + entry.getKey() + ": " + entry.getValue() + "승"));
        }
    }
    
    /**
     * 통계 데이터를 가져오는 메서드들
     */
    public Map<String, Integer> getRankingCounts() {
        return new HashMap<>(rankingCounts);
    }
    
    public Map<String, Integer> getWinnerCounts() {
        return new HashMap<>(winnerCounts);
    }
    
    public int getTotalHandsPlayed() {
        return totalHandsPlayed;
    }
    
    public int getTotalShowdowns() {
        return totalShowdowns;
    }
    
    private void printFinalResults() {
        logger.log("🏆 최종 결과");
        for (int i = 0; i < NUM_PLAYERS; i++) {
            String status = players[i].isActive() ? "활성" : "비활성";
            logger.log(getPlayerName(i) + ": " + players[i].getStack() + "칩 (" + status + ")");
        }
    }
}