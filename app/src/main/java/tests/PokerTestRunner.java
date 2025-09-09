package tests;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import poker.HoldemGame;
import poker.agent.RandomAgent;
import poker.core.Ranking;

/**
 * 홀덤 게임의 다양한 시나리오를 테스트하는 클래스
 * 버그 발견을 위한 포괄적인 테스트 도구
 */
public class PokerTestRunner {
    
    private static final int DEFAULT_HANDS = 100;
    private static final int DEFAULT_PLAYERS = 6;
    
    private Map<String, Integer> rankingCounts = new HashMap<>();
    private Map<String, Integer> winnerCounts = new HashMap<>();
    private List<String> errorLogs = new ArrayList<>();
    private int totalHands = 0;
    private int totalGames = 0;
    private PrintWriter logWriter;
    
    public static void main(String[] args) {
        PokerTestRunner runner = new PokerTestRunner();
        
        try {
            runner.initializeLogging();
            runner.logMessage("=== 홀덤 게임 포괄적 테스트 시작 ===\n");
            
            // 다양한 테스트 시나리오 실행
            runner.runBasicTest();
            runner.runLongGameTest();
            runner.runMultipleGamesTest();
            runner.runEdgeCaseTest();
            runner.runStressTest();
            
            // 최종 결과 출력
            runner.printFinalStatistics();
        } catch (IOException e) {
            System.err.println("로그 파일 초기화 실패: " + e.getMessage());
        } finally {
            runner.closeLogging();
        }
    }
    
    /**
     * 로깅 초기화
     */
    private void initializeLogging() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String logFileName = "logs/test_runner_" + timestamp + ".log";
        logWriter = new PrintWriter(new FileWriter(logFileName, true));
    }
    
    /**
     * 로그 메시지 출력
     */
    private void logMessage(String message) {
        if (logWriter != null) {
            logWriter.println(message);
            logWriter.flush();
        }
        System.out.println(message);
    }
    
    /**
     * 로깅 종료
     */
    private void closeLogging() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
    
    /**
     * 기본 테스트 - 3핸드 게임
     */
    public void runBasicTest() {
        logMessage("🔍 기본 테스트 (3핸드 게임)");
        logMessage("=" + "=".repeat(50));
        
        try {
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(3);
            totalGames++;
            logMessage("✅ 기본 테스트 완료\n");
        } catch (Exception e) {
            errorLogs.add("기본 테스트 오류: " + e.getMessage());
            logMessage("❌ 기본 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 긴 게임 테스트 - 50핸드 게임
     */
    public void runLongGameTest() {
        logMessage("🔍 긴 게임 테스트 (50핸드 게임)");
        logMessage("=" + "=".repeat(50));
        
        try {
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(50);
            totalGames++;
            logMessage("✅ 긴 게임 테스트 완료\n");
        } catch (Exception e) {
            errorLogs.add("긴 게임 테스트 오류: " + e.getMessage());
            logMessage("❌ 긴 게임 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 다중 게임 테스트 - 여러 개의 짧은 게임
     */
    public void runMultipleGamesTest() {
        logMessage("🔍 다중 게임 테스트 (10개 게임, 각 5핸드)");
        logMessage("=" + "=".repeat(50));
        
        for (int i = 1; i <= 10; i++) {
            try {
                logMessage("게임 " + i + "/10 실행 중...");
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(5);
                totalGames++;
            } catch (Exception e) {
                errorLogs.add("다중 게임 테스트 " + i + " 오류: " + e.getMessage());
                logMessage("❌ 게임 " + i + " 실패: " + e.getMessage());
            }
        }
        logMessage("✅ 다중 게임 테스트 완료\n");
    }
    
    /**
     * 엣지 케이스 테스트 - 특수한 상황들
     */
    public void runEdgeCaseTest() {
        logMessage("🔍 엣지 케이스 테스트");
        logMessage("=" + "=".repeat(50));
        
        // 1. 매우 짧은 게임 (1핸드)
        testSingleHandGame();
        
        // 2. 다양한 에이전트 조합
        testDifferentAgentCombinations();
        
        // 3. 연속 게임 실행
        testConsecutiveGames();
        
        logMessage("✅ 엣지 케이스 테스트 완료\n");
    }
    
    /**
     * 스트레스 테스트 - 대량의 게임 실행
     */
    public void runStressTest() {
        logMessage("🔍 스트레스 테스트 (100개 게임, 각 3핸드)");
        logMessage("=" + "=".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= 100; i++) {
            try {
                if (i % 20 == 0) {
                    logMessage("진행률: " + i + "/100");
                }
                
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(3);
                totalGames++;
                
            } catch (Exception e) {
                errorLogs.add("스트레스 테스트 " + i + " 오류: " + e.getMessage());
                logMessage("❌ 게임 " + i + " 실패: " + e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        logMessage("✅ 스트레스 테스트 완료");
        logMessage("총 실행 시간: " + duration + "ms");
        logMessage("평균 게임 시간: " + (duration / 100.0) + "ms\n");
    }
    
    /**
     * 단일 핸드 게임 테스트
     */
    private void testSingleHandGame() {
        logMessage("  - 단일 핸드 게임 테스트");
        try {
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(1);
            totalGames++;
        } catch (Exception e) {
            errorLogs.add("단일 핸드 게임 오류: " + e.getMessage());
            logMessage("    ❌ 단일 핸드 게임 실패: " + e.getMessage());
        }
    }
    
    /**
     * 다양한 에이전트 조합 테스트
     */
    private void testDifferentAgentCombinations() {
        logMessage("  - 다양한 에이전트 조합 테스트");
        
        // 동일한 에이전트로 게임
        try {
            RandomAgent[] agents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                agents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(agents);
            game.playMultipleHands(3);
            totalGames++;
        } catch (Exception e) {
            errorLogs.add("에이전트 조합 테스트 오류: " + e.getMessage());
            logMessage("    ❌ 에이전트 조합 테스트 실패: " + e.getMessage());
        }
    }
    
    /**
     * 연속 게임 실행 테스트
     */
    private void testConsecutiveGames() {
        logMessage("  - 연속 게임 실행 테스트");
        
        try {
            // 연속으로 5개 게임 실행
            for (int i = 0; i < 5; i++) {
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(2);
                totalGames++;
            }
        } catch (Exception e) {
            errorLogs.add("연속 게임 테스트 오류: " + e.getMessage());
            logMessage("    ❌ 연속 게임 테스트 실패: " + e.getMessage());
        }
    }
    
    /**
     * 랭킹 통계 수집
     */
    public void collectRankingStatistics(Ranking ranking) {
        String rankingName = ranking.toString();
        rankingCounts.put(rankingName, rankingCounts.getOrDefault(rankingName, 0) + 1);
    }
    
    /**
     * 승자 통계 수집
     */
    public void collectWinnerStatistics(String winnerName) {
        winnerCounts.put(winnerName, winnerCounts.getOrDefault(winnerName, 0) + 1);
    }
    
    /**
     * 최종 통계 출력
     */
    public void printFinalStatistics() {
        logMessage("📊 최종 테스트 결과");
        logMessage("=" + "=".repeat(50));
        
        logMessage("총 실행된 게임 수: " + totalGames);
        logMessage("총 실행된 핸드 수: " + totalHands);
        logMessage("발견된 오류 수: " + errorLogs.size());
        
        if (!errorLogs.isEmpty()) {
            logMessage("\n❌ 발견된 오류들:");
            for (int i = 0; i < errorLogs.size(); i++) {
                logMessage("  " + (i + 1) + ". " + errorLogs.get(i));
            }
        } else {
            logMessage("\n✅ 모든 테스트가 성공적으로 완료되었습니다!");
        }
        
        // 랭킹 통계
        if (!rankingCounts.isEmpty()) {
            logMessage("\n📈 랭킹 통계:");
            rankingCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue() + "회"));
        }
        
        // 승자 통계
        if (!winnerCounts.isEmpty()) {
            logMessage("\n🏆 승자 통계:");
            winnerCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue() + "승"));
        }
        
        logMessage("\n=== 테스트 완료 ===");
    }
    
    /**
     * 특정 시나리오만 테스트하는 메서드
     */
    public void runSpecificTest(String testType, int hands) {
        logMessage("🔍 " + testType + " 테스트 (" + hands + "핸드)");
        logMessage("=" + "=".repeat(50));
        
        try {
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(hands);
            totalGames++;
            logMessage("✅ " + testType + " 테스트 완료\n");
        } catch (Exception e) {
            errorLogs.add(testType + " 테스트 오류: " + e.getMessage());
            logMessage("❌ " + testType + " 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 랜덤 시나리오 테스트
     */
    public void runRandomScenarioTest() {
        logMessage("🔍 랜덤 시나리오 테스트");
        logMessage("=" + "=".repeat(50));
        
        Random random = new Random();
        
        for (int i = 1; i <= 20; i++) {
            int hands = random.nextInt(20) + 1; // 1-20 핸드
            logMessage("랜덤 게임 " + i + "/20: " + hands + "핸드");
            
            try {
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(hands);
                totalGames++;
            } catch (Exception e) {
                errorLogs.add("랜덤 시나리오 " + i + " 오류: " + e.getMessage());
                logMessage("❌ 랜덤 게임 " + i + " 실패: " + e.getMessage());
            }
        }
        
        logMessage("✅ 랜덤 시나리오 테스트 완료\n");
    }
}
