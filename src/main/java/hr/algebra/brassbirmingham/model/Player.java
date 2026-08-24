package hr.algebra.brassbirmingham.model;

import java.io.Serial;
import java.io.Serializable;

public class Player implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private int money;
    private int income;

    public Player(String name, int money) {
        this.name = name;
        this.money = money;
    }

    public String getName()   { return name; }
    public int getMoney()     { return money; }
    public int getIncome()    { return income; }

    public void addMoney(int amount) {
        money += amount;
    }

    public void addIncome(int amount) {
        income += amount;
    }

}
