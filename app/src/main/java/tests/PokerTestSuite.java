package tests;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 모든 홀덤 게임 테스트를 통합 실행하는 클래스
 * PokerScenarioTester, PokerEdgeCaseTester, PokerTestRunner를 순차적으로 실행
 */
public class PokerTestSuite {
    
    private PrintWriter logWriter;
    
    public static void main(String[] args) {
        PokerTestSuite testSuite = new PokerTestSuite();
        
        try {
            testSuite.initializeLogging();
            testSuite.logMessage("=== 홀덤 게임 통합 테스트 스위트 시작 ===");
            testSuite.logMessage("시작 시간: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            testSuite.logMessage("=" + "=".repeat(60) + "\n");
            
            // 전체 테스트 실행
            testSuite.runAllTests();
            
            testSuite.logMessage("\n" + "=".repeat(60));
            testSuite.logMessage("=== 홀덤 게임 통합 테스트 스위트 완료 ===");
            testSuite.logMessage("완료 시간: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
        } catch (IOException e) {
            System.err.println("로그 파일 초기화 실패: " + e.getMessage());
        } finally {
            testSuite.closeLogging();
        }
    }
    
    /**
     * 로깅 초기화
     */
    private void initializeLogging() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String logFileName = "logs/test_suite_" + timestamp + ".log";
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
     * 모든 테스트 실행
     */
    public void runAllTests() {
        long startTime = System.currentTimeMillis();
        
        // 1. 시나리오 테스트 실행
        runScenarioTests();
        
        // 2. 엣지 케이스 테스트 실행
        runEdgeCaseTests();
        
        // 3. 포괄적 테스트 실행
        runComprehensiveTests();
        
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        
        logMessage("\n📊 전체 테스트 실행 시간: " + totalDuration + "ms (" + (totalDuration / 1000.0) + "초)");
    }
    
    /**
     * 시나리오 테스트 실행
     */
    private void runScenarioTests() {
        logMessage("🎯 1단계: 시나리오 테스트 실행");
        logMessage("-".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        try {
            PokerScenarioTester scenarioTester = new PokerScenarioTester();
            
            logMessage("빠른 폴드 시나리오 테스트...");
            scenarioTester.testQuickFoldScenario();
            
            logMessage("올인 시나리오 테스트...");
            scenarioTester.testAllInScenario();
            
            logMessage("긴 쇼다운 시나리오 테스트...");
            scenarioTester.testLongShowdownScenario();
            
            logMessage("블라인드 스틸 시나리오 테스트...");
            scenarioTester.testBlindStealScenario();
            
            logMessage("멀티웨이 팟 시나리오 테스트...");
            scenarioTester.testMultiWayPotScenario();
            
            logMessage("헤즈업 시나리오 테스트...");
            scenarioTester.testHeadsUpScenario();
            
            logMessage("스택 변동 시나리오 테스트...");
            scenarioTester.testStackVariationScenario();
            
            logMessage("연속 승리 시나리오 테스트...");
            scenarioTester.testConsecutiveWinsScenario();
            
            long endTime = System.currentTimeMillis();
            logMessage("✅ 시나리오 테스트 완료 (소요시간: " + (endTime - startTime) + "ms)\n");
            
        } catch (Exception e) {
            logMessage("❌ 시나리오 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 엣지 케이스 테스트 실행
     */
    private void runEdgeCaseTests() {
        logMessage("🔍 2단계: 엣지 케이스 테스트 실행");
        logMessage("-".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        try {
            PokerEdgeCaseTester edgeCaseTester = new PokerEdgeCaseTester();
            
            logMessage("단일 핸드 게임 테스트...");
            edgeCaseTester.testSingleHandGame();
            
            logMessage("매우 짧은 게임 테스트...");
            edgeCaseTester.testVeryShortGame();
            
            logMessage("매우 긴 게임 테스트...");
            edgeCaseTester.testVeryLongGame();
            
            logMessage("연속 게임 실행 테스트...");
            edgeCaseTester.testConsecutiveGames();
            
            logMessage("빠른 게임 실행 테스트...");
            edgeCaseTester.testRapidGameExecution();
            
            logMessage("메모리 스트레스 테스트...");
            edgeCaseTester.testMemoryStressTest();
            
            logMessage("예외 처리 테스트...");
            edgeCaseTester.testExceptionHandling();
            
            logMessage("경계 조건 테스트...");
            edgeCaseTester.testBoundaryConditions();
            
            logMessage("대규모 부하 테스트...");
            edgeCaseTester.testLargeScaleLoadTest();
            
            logMessage("동시 실행 테스트...");
            edgeCaseTester.testConcurrentExecution();
            
            logMessage("극한 부하 테스트...");
            edgeCaseTester.testExtremeLoadTest();
            
            long endTime = System.currentTimeMillis();
            logMessage("✅ 엣지 케이스 테스트 완료 (소요시간: " + (endTime - startTime) + "ms)\n");
            
        } catch (Exception e) {
            logMessage("❌ 엣지 케이스 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 포괄적 테스트 실행
     */
    private void runComprehensiveTests() {
        logMessage("📋 3단계: 포괄적 테스트 실행");
        logMessage("-".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        try {
            PokerTestRunner testRunner = new PokerTestRunner();
            
            logMessage("기본 테스트 실행...");
            testRunner.runBasicTest();
            
            logMessage("긴 게임 테스트 실행...");
            testRunner.runLongGameTest();
            
            logMessage("다중 게임 테스트 실행...");
            testRunner.runMultipleGamesTest();
            
            logMessage("엣지 케이스 테스트 실행...");
            testRunner.runEdgeCaseTest();
            
            logMessage("스트레스 테스트 실행...");
            testRunner.runStressTest();
            
            logMessage("랜덤 시나리오 테스트 실행...");
            testRunner.runRandomScenarioTest();
            
            long endTime = System.currentTimeMillis();
            logMessage("✅ 포괄적 테스트 완료 (소요시간: " + (endTime - startTime) + "ms)\n");
            
        } catch (Exception e) {
            logMessage("❌ 포괄적 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 특정 테스트만 실행하는 메서드
     */
    public void runSpecificTest(String testType) {
        logMessage("🎯 특정 테스트 실행: " + testType);
        logMessage("-".repeat(50));
        
        try {
            switch (testType.toLowerCase()) {
                case "scenario":
                    runScenarioTests();
                    break;
                case "edgecase":
                case "edge_case":
                    runEdgeCaseTests();
                    break;
                case "comprehensive":
                    runComprehensiveTests();
                    break;
                default:
                    logMessage("❌ 알 수 없는 테스트 타입: " + testType);
                    logMessage("사용 가능한 타입: scenario, edgecase, comprehensive");
            }
        } catch (Exception e) {
            logMessage("❌ " + testType + " 테스트 실행 실패: " + e.getMessage());
        }
    }
    
    /**
     * 빠른 테스트 실행 (핵심 테스트만)
     */
    public void runQuickTests() {
        logMessage("⚡ 빠른 테스트 실행");
        logMessage("-".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 기본적인 테스트들만 실행
            PokerScenarioTester scenarioTester = new PokerScenarioTester();
            scenarioTester.testQuickFoldScenario();
            scenarioTester.testAllInScenario();
            
            PokerEdgeCaseTester edgeCaseTester = new PokerEdgeCaseTester();
            edgeCaseTester.testSingleHandGame();
            edgeCaseTester.testVeryShortGame();
            
            PokerTestRunner testRunner = new PokerTestRunner();
            testRunner.runBasicTest();
            
            long endTime = System.currentTimeMillis();
            logMessage("✅ 빠른 테스트 완료 (소요시간: " + (endTime - startTime) + "ms)\n");
            
        } catch (Exception e) {
            logMessage("❌ 빠른 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 테스트 통계 출력
     */
    public void printTestStatistics() {
        logMessage("\n📊 테스트 통계");
        logMessage("-".repeat(50));
        logMessage("• 시나리오 테스트: 8개 시나리오");
        logMessage("• 엣지 케이스 테스트: 11개 케이스 (부하 테스트 포함)");
        logMessage("• 포괄적 테스트: 6개 테스트 그룹");
        logMessage("• 총 테스트 항목: 25개");
        logMessage("• 예상 실행 시간: 3-10분 (시스템 성능에 따라)");
        logMessage("• 부하 테스트: 최대 5000게임 동시 실행");
        logMessage("• 동시성 테스트: 10개 스레드 병렬 처리");
    }
}
