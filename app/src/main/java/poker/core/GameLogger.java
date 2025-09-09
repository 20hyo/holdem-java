package poker.core;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 홀덤 게임 로그를 파일로 기록하는 클래스
 */
public class GameLogger {
    private static final String LOG_FILE = "holdem_game.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private FileWriter writer;
    private boolean isEnabled;
    
    public GameLogger() {
        this.isEnabled = true;
        try {
            this.writer = new FileWriter(LOG_FILE, true); // append mode
            log("=== 홀덤 게임 시작 ===");
        } catch (IOException e) {
            System.err.println("로그 파일 생성 실패: " + e.getMessage());
            this.isEnabled = false;
        }
    }
    
    public void log(String message) {
        if (!isEnabled) return;
        
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logEntry = String.format("[%s] %s%n", timestamp, message);
            writer.write(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("로그 기록 실패: " + e.getMessage());
        }
    }
    
    public void logHandStart(int handNumber, String dealer) {
        log(String.format("핸드 #%d 시작 - 딜러: %s", handNumber, dealer));
    }
    
    public void logBlinds(int smallBlind, int bigBlind, String smallBlindPlayer, String bigBlindPlayer) {
        log(String.format("블라인드: %s(SB: %d), %s(BB: %d)", 
            smallBlindPlayer, smallBlind, bigBlindPlayer, bigBlind));
    }
    
    public void logHoleCards(String player, String card1, String card2) {
        log(String.format("%s 홀카드: %s %s", player, card1, card2));
    }
    
    public void logAction(String player, String action) {
        log(String.format("%s: %s", player, action));
    }
    
    public void logCommunityCards(String street, String cards) {
        log(String.format("%s: %s", street, cards));
    }
    
    public void logPotDistribution(int potSize, String winner) {
        log(String.format("팟 분배: %d칩 → %s", potSize, winner));
    }
    
    public void logStackUpdate(String player, int stack) {
        log(String.format("%s 스택: %d칩", player, stack));
    }
    
    public void logShowdown(String player, String cards, String ranking, double strength) {
        log(String.format("%s: %s → %s (강도: %.2f)", player, cards, ranking, strength));
    }
    
    public void logWinner(String winner, String ranking) {
        log(String.format("승자: %s (%s)", winner, ranking));
    }
    
    public void logGameEnd() {
        log("=== 홀덤 게임 종료 ===");
        close();
    }
    
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("로그 파일 닫기 실패: " + e.getMessage());
            }
        }
    }
}
