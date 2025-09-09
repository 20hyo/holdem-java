package poker.core;

public interface IDeck {
    void shuffle();
    Card drawCard();
    void reset();
}
