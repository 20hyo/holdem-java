package poker;

import poker.agent.RandomAgent;

/**
 * 홀덤 게임 테스트 클래스
 * 랜덤 에이전트들을 사용하여 완전한 홀덤 게임을 실행
 */
public class HoldemTest {
    public static void main(String[] args) {
        // RandomAgent들을 생성하여 게임에 설정
        RandomAgent[] agents = new RandomAgent[6];
        for (int i = 0; i < 6; i++) {
            agents[i] = new RandomAgent();
        }
        
        // 게임 인스턴스 생성 (RandomAgent들로 초기화)
        HoldemGame game = new HoldemGame(agents);
        
        game.playMultipleHands(3);
        // game.playHand();
    }
}
