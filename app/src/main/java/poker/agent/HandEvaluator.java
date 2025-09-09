package poker.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import poker.core.Card;
import poker.core.CardRank;
import poker.core.Ranking;

/**
 * 에이전트 전용 카드 패 평가 클래스
 * 홀카드와 커뮤니티 카드를 받아서 패의 강도를 0.0~1.0 범위로 평가
 */
public class HandEvaluator {
    
    /**
     * 카드 패의 가치를 평가합니다 (0.0 ~ 1.0)
     * @param holeCards 홀카드 2장
     * @param communityCards 커뮤니티 카드들
     * @return 0.0: 매우 약한 패, 1.0: 매우 강한 패
     */
    public static double evaluateHandStrength(Card[] holeCards, List<Card> communityCards) {
        // 홀카드가 없거나 커뮤니티 카드가 없으면 기본값 반환
        if (holeCards == null || holeCards.length < 2 || communityCards == null) {
            return 0.5; // 중간값 반환
        }
        
        // 모든 카드를 합쳐서 분석
        List<Card> allCards = new ArrayList<>();
        allCards.add(holeCards[0]);
        allCards.add(holeCards[1]);
        allCards.addAll(communityCards);
        
        // 패의 랭킹 확인
        Ranking ranking = evaluateRanking(allCards);
        
        // 랭킹에 따른 기본 강도 계산
        double baseStrength = getBaseStrengthFromRanking(ranking);
        
        // 홀카드의 강도에 따른 조정
        double holeCardAdjustment = getHoleCardAdjustment(holeCards);
        
        // 커뮤니티 카드 수에 따른 조정 (더 많은 카드일수록 더 정확한 평가)
        double communityAdjustment = Math.min(1.0, communityCards.size() / 5.0);
        
        // 조정된 강도 계산
        double adjustedStrength = baseStrength * 0.7 + holeCardAdjustment * 0.3;
        adjustedStrength *= communityAdjustment;
        
        return Math.max(0.0, Math.min(1.0, adjustedStrength)); // 0.0~1.0 범위로 제한
    }
    
    /**
     * 랭킹에 따른 기본 강도를 반환합니다
     */
    private static double getBaseStrengthFromRanking(Ranking ranking) {
        switch (ranking) {
            case ROYAL_FLUSH: return 1.0;
            case STRAIGHT_FLUSH: return 0.95;
            case FOUR_OF_A_KIND: return 0.9;
            case FULL_HOUSE: return 0.8;
            case FLUSH: return 0.7;
            case STRAIGHT: return 0.6;
            case THREE_OF_A_KIND: return 0.5;
            case TWO_PAIR: return 0.4;
            case ONE_PAIR: return 0.3;
            case HIGH_CARD: return 0.1;
            default: return 0.5;
        }
    }
    
    /**
     * 카드 리스트에서 최고 랭킹을 평가합니다
     */
    private static Ranking evaluateRanking(List<Card> cards) {
        if (cards.size() < 5) {
            return Ranking.HIGH_CARD; // 카드가 부족하면 하이카드
        }
        
        // 로얄 플러시 확인
        if (isRoyalFlush(cards)) return Ranking.ROYAL_FLUSH;
        
        // 스트레이트 플러시 확인
        if (isStraightFlush(cards)) return Ranking.STRAIGHT_FLUSH;
        
        // 포카드 확인
        if (isFourOfAKind(cards)) return Ranking.FOUR_OF_A_KIND;
        
        // 풀하우스 확인
        if (isFullHouse(cards)) return Ranking.FULL_HOUSE;
        
        // 플러시 확인
        if (isFlush(cards)) return Ranking.FLUSH;
        
        // 스트레이트 확인
        if (isStraight(cards)) return Ranking.STRAIGHT;
        
        // 트리플 확인
        if (isThreeOfAKind(cards)) return Ranking.THREE_OF_A_KIND;
        
        // 투페어 확인
        if (isTwoPair(cards)) return Ranking.TWO_PAIR;
        
        // 원페어 확인
        if (isOnePair(cards)) return Ranking.ONE_PAIR;
        
        return Ranking.HIGH_CARD;
    }
    
    /**
     * 홀카드의 강도를 평가합니다
     */
    private static double getHoleCardAdjustment(Card[] holeCards) {
        if (holeCards == null || holeCards.length < 2) return 0.5;
        
        Card card1 = holeCards[0];
        Card card2 = holeCards[1];
        
        // 같은 수트인지 확인
        boolean sameSuit = card1.getSuit().equals(card2.getSuit());
        
        // 페어인지 확인
        boolean isPair = card1.getRank().equals(card2.getRank());
        
        // 연속된 숫자인지 확인
        boolean isConnected = Math.abs(card1.getRankToInt() - card2.getRankToInt()) == 1;
        
        // 높은 카드인지 확인 (J, Q, K, A)
        boolean hasHighCard = card1.getRankToInt() >= 11 || card2.getRankToInt() >= 11;
        
        double strength = 0.3; // 기본값
        
        if (isPair) {
            strength += 0.4; // 페어는 강함
        }
        if (sameSuit) {
            strength += 0.2; // 같은 수트는 좋음
        }
        if (isConnected) {
            strength += 0.2; // 연속된 숫자는 좋음
        }
        if (hasHighCard) {
            strength += 0.1; // 높은 카드는 좋음
        }
        
        return Math.min(1.0, strength);
    }
    
    // ========== 패턴 확인 메서드들 ==========
    
    /**
     * 로얄 플러시 확인 (A, K, Q, J, 10 모두 같은 수트)
     */
    private static boolean isRoyalFlush(List<Card> cards) {
        if (!isFlush(cards)) return false;
        
        List<CardRank> ranks = new ArrayList<>();
        for (Card card : cards) {
            ranks.add(card.getRank());
        }
        
        return ranks.contains(CardRank.ACE) &&
               ranks.contains(CardRank.KING) &&
               ranks.contains(CardRank.QUEEN) &&
               ranks.contains(CardRank.JACK) &&
               ranks.contains(CardRank.TEN);
    }
    
    /**
     * 스트레이트 플러시 확인 (5장 연속, 같은 수트)
     */
    private static boolean isStraightFlush(List<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }
    
    /**
     * 포카드 확인 (같은 숫자 4장)
     */
    private static boolean isFourOfAKind(List<Card> cards) {
        return hasNOfAKind(cards, 4);
    }
    
    /**
     * 풀하우스 확인 (트리플 + 페어)
     */
    private static boolean isFullHouse(List<Card> cards) {
        return hasNOfAKind(cards, 3) && hasNOfAKind(cards, 2);
    }
    
    /**
     * 플러시 확인 (같은 수트 5장)
     */
    private static boolean isFlush(List<Card> cards) {
        if (cards.size() < 5) return false;
        
        // 각 수트별로 카드 수 세기
        int[] suitCounts = new int[4]; // SPADES, HEARTS, DIAMONDS, CLUBS
        
        for (Card card : cards) {
            switch (card.getSuit()) {
                case SPADES: suitCounts[0]++; break;
                case HEARTS: suitCounts[1]++; break;
                case DIAMONDS: suitCounts[2]++; break;
                case CLUBS: suitCounts[3]++; break;
            }
        }
        
        // 5장 이상인 수트가 있는지 확인
        for (int count : suitCounts) {
            if (count >= 5) return true;
        }
        
        return false;
    }
    
    /**
     * 스트레이트 확인 (5장 연속)
     */
    private static boolean isStraight(List<Card> cards) {
        if (cards.size() < 5) return false;
        
        // 카드를 숫자 순으로 정렬
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, new Comparator<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                return Integer.compare(c1.getRankToInt(), c2.getRankToInt());
            }
        });
        
        // 연속된 5장이 있는지 확인
        for (int i = 0; i <= sortedCards.size() - 5; i++) {
            boolean isConsecutive = true;
            for (int j = 1; j < 5; j++) {
                if (sortedCards.get(i + j).getRankToInt() - sortedCards.get(i + j - 1).getRankToInt() != 1) {
                    isConsecutive = false;
                    break;
                }
            }
            if (isConsecutive) return true;
        }
        
        return false;
    }
    
    /**
     * 트리플 확인 (같은 숫자 3장)
     */
    private static boolean isThreeOfAKind(List<Card> cards) {
        return hasNOfAKind(cards, 3);
    }
    
    /**
     * 투페어 확인 (같은 숫자 2장씩 2개)
     */
    private static boolean isTwoPair(List<Card> cards) {
        int pairCount = 0;
        int[] rankCounts = new int[14]; // A=1, 2-10, J=11, Q=12, K=13
        
        for (Card card : cards) {
            rankCounts[card.getRankToInt()]++;
        }
        
        for (int count : rankCounts) {
            if (count >= 2) pairCount++;
        }
        
        return pairCount >= 2;
    }
    
    /**
     * 원페어 확인 (같은 숫자 2장)
     */
    private static boolean isOnePair(List<Card> cards) {
        return hasNOfAKind(cards, 2);
    }
    
    /**
     * N장의 같은 숫자가 있는지 확인하는 헬퍼 메서드
     */
    private static boolean hasNOfAKind(List<Card> cards, int n) {
        int[] rankCounts = new int[14]; // A=1, 2-10, J=11, Q=12, K=13
        
        for (Card card : cards) {
            rankCounts[card.getRankToInt()]++;
        }
        
        for (int count : rankCounts) {
            if (count >= n) return true;
        }
        
        return false;
    }
}
