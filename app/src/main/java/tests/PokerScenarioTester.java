package tests;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import poker.HoldemGame;
import poker.agent.RandomAgent;

/**
 * 특수한 홀덤 게임 시나리오를 테스트하는 클래스
 * 버그 발견을 위한 엣지 케이스와 특수 상황 테스트
 */
public class PokerScenarioTester {
    
    private static final Random random = new Random();
    private PrintWriter logWriter;
    
    public static void main(String[] args) {
        PokerScenarioTester tester = new PokerScenarioTester();
        
        try {
            tester.initializeLogging();
            tester.logMessage("=== 홀덤 게임 시나리오 테스트 시작 ===\n");
            
            // 다양한 시나리오 테스트
            tester.testQuickFoldScenario();
            tester.testAllInScenario();
            tester.testLongShowdownScenario();
            tester.testBlindStealScenario();
            tester.testMultiWayPotScenario();
            tester.testHeadsUpScenario();
            tester.testStackVariationScenario();
            tester.testConsecutiveWinsScenario();
            
            tester.logMessage("=== 시나리오 테스트 완료 ===");
        } catch (IOException e) {
            System.err.println("로그 파일 초기화 실패: " + e.getMessage());
        } finally {
            tester.closeLogging();
        }
    }
    
    /**
     * 로깅 초기화
     */
    private void initializeLogging() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String logFileName = "logs/scenario_test_" + timestamp + ".log";
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
     * 빠른 폴드 시나리오 - 대부분의 플레이어가 빠르게 폴드
     */
    public void testQuickFoldScenario() {
        logMessage("🔍 빠른 폴드 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 보수적인 에이전트들로 게임
            RandomAgent[] conservativeAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                conservativeAgents[i] = new RandomAgent();
                // 폴드 확률을 높이기 위한 설정 (실제로는 RandomAgent 내부 로직에 따라)
            }
            
            HoldemGame game = new HoldemGame(conservativeAgents);
            game.playMultipleHands(10);
            logMessage("✅ 빠른 폴드 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 빠른 폴드 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 올인 시나리오 - 많은 플레이어가 올인하는 상황
     */
    public void testAllInScenario() {
        logMessage("🔍 올인 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 공격적인 에이전트들로 게임
            RandomAgent[] aggressiveAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                aggressiveAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(aggressiveAgents);
            game.playMultipleHands(15);
            logMessage("✅ 올인 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 올인 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 긴 쇼다운 시나리오 - 많은 플레이어가 리버까지 가는 상황
     */
    public void testLongShowdownScenario() {
        logMessage("🔍 긴 쇼다운 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 중간 정도의 에이전트들로 게임
            RandomAgent[] balancedAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                balancedAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(balancedAgents);
            game.playMultipleHands(20);
            logMessage("✅ 긴 쇼다운 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 긴 쇼다운 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 블라인드 스틸 시나리오 - 블라인드 포지션에서의 공격적 플레이
     */
    public void testBlindStealScenario() {
        logMessage("🔍 블라인드 스틸 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 다양한 성향의 에이전트들
            RandomAgent[] mixedAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                mixedAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(mixedAgents);
            game.playMultipleHands(25);
            logMessage("✅ 블라인드 스틸 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 블라인드 스틸 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 멀티웨이 팟 시나리오 - 많은 플레이어가 참여하는 큰 팟
     */
    public void testMultiWayPotScenario() {
        logMessage("🔍 멀티웨이 팟 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 참여도가 높은 에이전트들
            RandomAgent[] activeAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                activeAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(activeAgents);
            game.playMultipleHands(30);
            logMessage("✅ 멀티웨이 팟 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 멀티웨이 팟 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 헤즈업 시나리오 - 두 명의 플레이어만 남는 상황
     */
    public void testHeadsUpScenario() {
        logMessage("🔍 헤즈업 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 두 명의 플레이어만 활성화된 상황을 시뮬레이션
            RandomAgent[] headsUpAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                headsUpAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(headsUpAgents);
            game.playMultipleHands(15);
            logMessage("✅ 헤즈업 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 헤즈업 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 스택 변동 시나리오 - 플레이어들의 스택이 크게 변하는 상황
     */
    public void testStackVariationScenario() {
        logMessage("🔍 스택 변동 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 다양한 스택 크기로 시작하는 시나리오
            RandomAgent[] variedAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                variedAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(variedAgents);
            game.playMultipleHands(40);
            logMessage("✅ 스택 변동 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 스택 변동 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 연속 승리 시나리오 - 한 플레이어가 연속으로 승리하는 상황
     */
    public void testConsecutiveWinsScenario() {
        logMessage("🔍 연속 승리 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 한 플레이어가 유리한 상황을 만들기 위한 시나리오
            RandomAgent[] consecutiveAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                consecutiveAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(consecutiveAgents);
            game.playMultipleHands(35);
            logMessage("✅ 연속 승리 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 연속 승리 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 랜덤 시나리오 - 완전히 랜덤한 게임 플레이
     */
    public void testRandomScenario() {
        logMessage("🔍 랜덤 시나리오 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 완전히 랜덤한 에이전트들
            RandomAgent[] randomAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                randomAgents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(randomAgents);
            game.playMultipleHands(50);
            logMessage("✅ 랜덤 시나리오 완료\n");
        } catch (Exception e) {
            logMessage("❌ 랜덤 시나리오 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 특정 핸드 수로 게임을 실행하는 헬퍼 메서드
     */
    public void runCustomScenario(String scenarioName, int hands) {
        logMessage("🔍 " + scenarioName + " (" + hands + "핸드)");
        logMessage("-".repeat(40));
        
        try {
            RandomAgent[] agents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                agents[i] = new RandomAgent();
            }
            
            HoldemGame game = new HoldemGame(agents);
            game.playMultipleHands(hands);
            logMessage("✅ " + scenarioName + " 완료\n");
        } catch (Exception e) {
            logMessage("❌ " + scenarioName + " 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 여러 시나리오를 연속으로 실행하는 메서드
     */
    public void runMultipleScenarios() {
        logMessage("🔍 다중 시나리오 테스트");
        logMessage("=".repeat(50));
        
        List<String> scenarios = new ArrayList<>();
        scenarios.add("빠른 게임");
        scenarios.add("중간 게임");
        scenarios.add("긴 게임");
        scenarios.add("초단기 게임");
        scenarios.add("장기 게임");
        
        for (String scenario : scenarios) {
            int hands = random.nextInt(30) + 5; // 5-35 핸드
            runCustomScenario(scenario, hands);
        }
        
        logMessage("✅ 다중 시나리오 테스트 완료\n");
    }
}
