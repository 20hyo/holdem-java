package tests;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import poker.HoldemGame;
import poker.agent.RandomAgent;

/**
 * 홀덤 게임의 엣지 케이스와 극한 상황을 테스트하는 클래스
 * 버그 발견을 위한 특수한 상황들 테스트
 */
public class PokerEdgeCaseTester {
    
    private PrintWriter logWriter;
    
    public static void main(String[] args) {
        PokerEdgeCaseTester tester = new PokerEdgeCaseTester();
        
        try {
            tester.initializeLogging();
            tester.logMessage("=== 홀덤 게임 엣지 케이스 테스트 시작 ===\n");
            
            // 다양한 엣지 케이스 테스트
            tester.testSingleHandGame();
            tester.testVeryShortGame();
            tester.testVeryLongGame();
            tester.testConsecutiveGames();
            tester.testRapidGameExecution();
            tester.testMemoryStressTest();
            tester.testExceptionHandling();
            tester.testBoundaryConditions();
            
            tester.logMessage("=== 엣지 케이스 테스트 완료 ===");
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
        String logFileName = "logs/edge_case_test_" + timestamp + ".log";
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
     * 단일 핸드 게임 테스트
     */
    public void testSingleHandGame() {
        logMessage("🔍 단일 핸드 게임 테스트");
        logMessage("-".repeat(40));
        
        try {
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(1);
            logMessage("✅ 단일 핸드 게임 완료\n");
        } catch (Exception e) {
            logMessage("❌ 단일 핸드 게임 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 매우 짧은 게임 테스트 (2-3핸드)
     */
    public void testVeryShortGame() {
        logMessage("🔍 매우 짧은 게임 테스트");
        logMessage("-".repeat(40));
        
        for (int hands = 2; hands <= 3; hands++) {
            try {
                logMessage("  " + hands + "핸드 게임 테스트...");
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(hands);
                logMessage("  ✅ " + hands + "핸드 게임 완료");
            } catch (Exception e) {
                logMessage("  ❌ " + hands + "핸드 게임 실패: " + e.getMessage());
            }
        }
                logMessage("");
    }
    
    /**
     * 매우 긴 게임 테스트 (100+ 핸드)
     */
    public void testVeryLongGame() {
        logMessage("🔍 매우 긴 게임 테스트");
        logMessage("-".repeat(40));
        
        int[] longHands = {50, 100, 200};
        
        for (int hands : longHands) {
            try {
                logMessage("  " + hands + "핸드 게임 테스트...");
                long startTime = System.currentTimeMillis();
                
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(hands);
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                logMessage("  ✅ " + hands + "핸드 게임 완료 (소요시간: " + duration + "ms)");
            } catch (Exception e) {
                logMessage("  ❌ " + hands + "핸드 게임 실패: " + e.getMessage());
            }
        }
                logMessage("");
    }
    
    /**
     * 연속 게임 실행 테스트
     */
    public void testConsecutiveGames() {
        logMessage("🔍 연속 게임 실행 테스트");
        logMessage("-".repeat(40));
        
        try {
            logMessage("  연속으로 20개 게임 실행...");
            long startTime = System.currentTimeMillis();
            
            for (int i = 1; i <= 20; i++) {
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(3);
                
                if (i % 5 == 0) {
                    logMessage("    진행률: " + i + "/20");
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            logMessage("  ✅ 연속 게임 실행 완료 (총 소요시간: " + duration + "ms)");
            logMessage("  평균 게임 시간: " + (duration / 20.0) + "ms\n");
        } catch (Exception e) {
            logMessage("  ❌ 연속 게임 실행 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 빠른 게임 실행 테스트
     */
    public void testRapidGameExecution() {
        logMessage("🔍 빠른 게임 실행 테스트");
        logMessage("-".repeat(40));
        
        try {
            logMessage("  빠른 연속 실행 테스트...");
            long startTime = System.currentTimeMillis();
            
            // 빠르게 연속 실행
            for (int i = 0; i < 50; i++) {
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(1);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            logMessage("  ✅ 빠른 게임 실행 완료 (50게임, " + duration + "ms)");
            logMessage("  평균 게임 시간: " + (duration / 50.0) + "ms\n");
        } catch (Exception e) {
            logMessage("  ❌ 빠른 게임 실행 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 메모리 스트레스 테스트
     */
    public void testMemoryStressTest() {
        logMessage("🔍 메모리 스트레스 테스트");
        logMessage("-".repeat(40));
        
        try {
            logMessage("  메모리 사용량 테스트...");
            
            // 가비지 컬렉션 실행
            System.gc();
            Thread.sleep(100); // GC 완료 대기
            long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            
            // 많은 게임 실행
            for (int i = 0; i < 100; i++) {
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(2);
                
                if (i % 20 == 0) {
                    System.gc();
                    Thread.sleep(50); // GC 완료 대기
                    long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    long memoryDiff = currentMemory - initialMemory;
                    logMessage("    게임 " + i + ": 메모리 사용량 " + formatBytes(memoryDiff));
                }
            }
            
            System.gc();
            Thread.sleep(100); // GC 완료 대기
            long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = finalMemory - initialMemory;
            
            logMessage("  ✅ 메모리 스트레스 테스트 완료");
            logMessage("  총 메모리 사용량: " + formatBytes(memoryUsed) + "\n");
        } catch (Exception e) {
            logMessage("  ❌ 메모리 스트레스 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 예외 처리 테스트
     */
    public void testExceptionHandling() {
        logMessage("🔍 예외 처리 테스트");
        logMessage("-".repeat(40));
        
        // null 에이전트 배열 테스트
        try {
            logMessage("  null 에이전트 배열 테스트...");
            HoldemGame game = new HoldemGame(null);
            logMessage("  ❌ null 에이전트 배열이 예외를 발생시키지 않음");
        } catch (Exception e) {
            logMessage("  ✅ null 에이전트 배열 예외 처리 정상: " + e.getClass().getSimpleName());
        }
        
        // 잘못된 크기의 에이전트 배열 테스트
        try {
            logMessage("  잘못된 크기 에이전트 배열 테스트...");
            RandomAgent[] wrongSizeAgents = new RandomAgent[3]; // 6개가 아닌 3개
            HoldemGame game = new HoldemGame(wrongSizeAgents);
            logMessage("  ❌ 잘못된 크기 에이전트 배열이 예외를 발생시키지 않음");
        } catch (Exception e) {
            logMessage("  ✅ 잘못된 크기 에이전트 배열 예외 처리 정상: " + e.getClass().getSimpleName());
        }
        
                logMessage("");
    }
    
    /**
     * 경계 조건 테스트
     */
    public void testBoundaryConditions() {
        logMessage("🔍 경계 조건 테스트");
        logMessage("-".repeat(40));
        
        // 0핸드 게임 테스트
        try {
            logMessage("  0핸드 게임 테스트...");
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(0);
            logMessage("  ✅ 0핸드 게임 처리 정상");
        } catch (Exception e) {
            logMessage("  ❌ 0핸드 게임 실패: " + e.getMessage());
        }
        
        // 음수 핸드 게임 테스트
        try {
            logMessage("  음수 핸드 게임 테스트...");
            HoldemGame game = new HoldemGame();
            game.playMultipleHands(-1);
            logMessage("  ❌ 음수 핸드 게임이 예외를 발생시키지 않음");
        } catch (Exception e) {
            logMessage("  ✅ 음수 핸드 게임 예외 처리 정상: " + e.getClass().getSimpleName());
        }
        
                logMessage("");
    }
    
    /**
     * 특수한 에이전트 조합 테스트
     */
    public void testSpecialAgentCombinations() {
        logMessage("🔍 특수한 에이전트 조합 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 모든 에이전트가 동일한 인스턴스인 경우
            logMessage("  동일한 에이전트 인스턴스 테스트...");
            RandomAgent sharedAgent = new RandomAgent();
            RandomAgent[] sameAgents = new RandomAgent[6];
            for (int i = 0; i < 6; i++) {
                sameAgents[i] = sharedAgent;
            }
            
            HoldemGame game = new HoldemGame(sameAgents);
            game.playMultipleHands(5);
            logMessage("  ✅ 동일한 에이전트 인스턴스 테스트 완료");
            
        } catch (Exception e) {
            logMessage("  ❌ 동일한 에이전트 인스턴스 테스트 실패: " + e.getMessage());
        }
        
                logMessage("");
    }
    
    /**
     * 통합 엣지 케이스 테스트
     */
    public void runComprehensiveEdgeCaseTest() {
        logMessage("🔍 통합 엣지 케이스 테스트");
        logMessage("=".repeat(50));
        
        List<String> testResults = new ArrayList<>();
        
        // 모든 엣지 케이스 테스트 실행
        try {
            testSingleHandGame();
            testResults.add("단일 핸드: ✅");
        } catch (Exception e) {
            testResults.add("단일 핸드: ❌ " + e.getMessage());
        }
        
        try {
            testVeryShortGame();
            testResults.add("짧은 게임: ✅");
        } catch (Exception e) {
            testResults.add("짧은 게임: ❌ " + e.getMessage());
        }
        
        try {
            testConsecutiveGames();
            testResults.add("연속 게임: ✅");
        } catch (Exception e) {
            testResults.add("연속 게임: ❌ " + e.getMessage());
        }
        
        try {
            testExceptionHandling();
            testResults.add("예외 처리: ✅");
        } catch (Exception e) {
            testResults.add("예외 처리: ❌ " + e.getMessage());
        }
        
        // 결과 출력
        logMessage("📊 통합 테스트 결과:");
        for (String result : testResults) {
            logMessage("  " + result);
        }
        
        logMessage("\n✅ 통합 엣지 케이스 테스트 완료");
    }
    
    /**
     * 바이트를 읽기 쉬운 형태로 포맷팅
     */
    private String formatBytes(long bytes) {
        if (bytes < 0) {
            return "GC로 인한 메모리 해제: " + formatBytes(-bytes);
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * 대규모 부하 테스트
     */
    public void testLargeScaleLoadTest() {
        logMessage("🚀 대규모 부하 테스트");
        logMessage("-".repeat(40));
        
        try {
            // 메모리 상태 초기화
            System.gc();
            Thread.sleep(200);
            long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long startTime = System.currentTimeMillis();
            
            logMessage("  1000게임 대규모 테스트 시작...");
            for (int i = 0; i < 1000; i++) {
                HoldemGame game = new HoldemGame();
                game.playMultipleHands(5); // 각 게임당 5핸드
                
                if (i % 100 == 0 && i > 0) {
                    System.gc();
                    Thread.sleep(50);
                    long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    long memoryUsed = currentMemory - startMemory;
                    long elapsed = System.currentTimeMillis() - startTime;
                    double gamesPerSecond = (i * 1000.0) / elapsed;
                    
                    logMessage("    진행률: " + i + "/1000 게임");
                    logMessage("    메모리 사용량: " + formatBytes(memoryUsed));
                    logMessage("    처리 속도: " + String.format("%.1f", gamesPerSecond) + " 게임/초");
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.gc();
            Thread.sleep(200);
            long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long totalMemoryUsed = endMemory - startMemory;
            
            logMessage("  ✅ 대규모 부하 테스트 완료");
            logMessage("  총 실행 시간: " + (totalTime / 1000.0) + "초");
            logMessage("  평균 게임 시간: " + (totalTime / 1000.0) + "ms");
            logMessage("  총 메모리 사용량: " + formatBytes(totalMemoryUsed));
            logMessage("  평균 처리 속도: " + String.format("%.1f", 1000.0 * 1000 / totalTime) + " 게임/초\n");
            
        } catch (Exception e) {
            logMessage("  ❌ 대규모 부하 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 동시 실행 테스트
     */
    public void testConcurrentExecution() {
        logMessage("⚡ 동시 실행 테스트");
        logMessage("-".repeat(40));
        
        try {
            int threadCount = 10;
            int gamesPerThread = 50;
            
            logMessage("  " + threadCount + "개 스레드로 동시 실행 테스트...");
            logMessage("  각 스레드당 " + gamesPerThread + "게임 실행");
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<Long>> futures = new ArrayList<>();
            
            long startTime = System.currentTimeMillis();
            
            // 각 스레드에서 게임 실행
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                Future<Long> future = executor.submit(() -> {
                    long threadStartTime = System.currentTimeMillis();
                    try {
                        for (int j = 0; j < gamesPerThread; j++) {
                            HoldemGame game = new HoldemGame();
                            game.playMultipleHands(3); // 각 게임당 3핸드
                        }
                        return System.currentTimeMillis() - threadStartTime;
                    } catch (Exception e) {
                        logMessage("    스레드 " + threadId + " 실패: " + e.getMessage());
                        return -1L;
                    }
                });
                futures.add(future);
            }
            
            // 모든 스레드 완료 대기
            executor.shutdown();
            boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);
            
            if (!finished) {
                logMessage("  ❌ 일부 스레드가 60초 내에 완료되지 않음");
                executor.shutdownNow();
                return;
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // 결과 수집
            int successfulThreads = 0;
            long totalThreadTime = 0;
            for (Future<Long> future : futures) {
                Long threadTime = future.get();
                if (threadTime > 0) {
                    successfulThreads++;
                    totalThreadTime += threadTime;
                }
            }
            
            int totalGames = successfulThreads * gamesPerThread;
            double avgThreadTime = totalThreadTime / (double) successfulThreads;
            
            logMessage("  ✅ 동시 실행 테스트 완료");
            logMessage("  성공한 스레드: " + successfulThreads + "/" + threadCount);
            logMessage("  총 실행 시간: " + (totalTime / 1000.0) + "초");
            logMessage("  평균 스레드 시간: " + (avgThreadTime / 1000.0) + "초");
            logMessage("  총 게임 수: " + totalGames);
            logMessage("  전체 처리 속도: " + String.format("%.1f", totalGames * 1000.0 / totalTime) + " 게임/초");
            logMessage("  스레드당 평균 속도: " + String.format("%.1f", gamesPerThread * 1000.0 / avgThreadTime) + " 게임/초\n");
            
        } catch (Exception e) {
            logMessage("  ❌ 동시 실행 테스트 실패: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 극한 부하 테스트
     */
    public void testExtremeLoadTest() {
        logMessage("🔥 극한 부하 테스트");
        logMessage("-".repeat(40));
        
        try {
            logMessage("  극한 상황 테스트 시작...");
            
            // 메모리 상태 초기화
            System.gc();
            Thread.sleep(200);
            long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long startTime = System.currentTimeMillis();
            
            // 매우 많은 게임을 빠르게 실행
            int totalGames = 5000;
            int batchSize = 100;
            
            for (int batch = 0; batch < totalGames / batchSize; batch++) {
                for (int i = 0; i < batchSize; i++) {
                    HoldemGame game = new HoldemGame();
                    game.playMultipleHands(1); // 각 게임당 1핸드만
                }
                
                if (batch % 5 == 0) {
                    System.gc();
                    Thread.sleep(10);
                    long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    long memoryUsed = currentMemory - startMemory;
                    long elapsed = System.currentTimeMillis() - startTime;
                    int completedGames = (batch + 1) * batchSize;
                    double gamesPerSecond = (completedGames * 1000.0) / elapsed;
                    
                    logMessage("    배치 " + (batch + 1) + ": " + completedGames + "/" + totalGames + " 게임 완료");
                    logMessage("    메모리 사용량: " + formatBytes(memoryUsed));
                    logMessage("    처리 속도: " + String.format("%.1f", gamesPerSecond) + " 게임/초");
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.gc();
            Thread.sleep(200);
            long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long totalMemoryUsed = endMemory - startMemory;
            
            logMessage("  ✅ 극한 부하 테스트 완료");
            logMessage("  총 게임 수: " + totalGames);
            logMessage("  총 실행 시간: " + (totalTime / 1000.0) + "초");
            logMessage("  평균 게임 시간: " + (totalTime / (double) totalGames) + "ms");
            logMessage("  최종 처리 속도: " + String.format("%.1f", totalGames * 1000.0 / totalTime) + " 게임/초");
            logMessage("  총 메모리 사용량: " + formatBytes(totalMemoryUsed) + "\n");
            
        } catch (Exception e) {
            logMessage("  ❌ 극한 부하 테스트 실패: " + e.getMessage() + "\n");
        }
    }
}
