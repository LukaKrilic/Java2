package hr.algebra.brassbirmingham.engine;

import hr.algebra.brassbirmingham.model.City;

public final class GameRules {
    public static int STARTING_MONEY = 30;
    public static int ROUNDS = 10;
    public static int LOAN_AMOUNT = 30;
    public static int LOAN_INCOME_PENALTY = 1;
    public static int MARKET_COAL_PRICE = 3;
    public static int CANAL_COST = 3;
    public static int LINK_VP = 1;
    public static int MONEY_PER_VP = 5;
    public static final City MARKET_CITY = City.BIRMINGHAM;

    private GameRules() {
    }
}
