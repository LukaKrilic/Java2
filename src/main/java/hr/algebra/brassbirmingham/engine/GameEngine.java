package hr.algebra.brassbirmingham.engine;

import hr.algebra.brassbirmingham.model.*;

import java.util.*;


public class GameEngine {
    private final GameState state;

    public GameEngine(GameState state) {
        this.state = state;
    }

    public GameState getState() { return state; }

    public ApplyResult apply(GameAction action) {
        Objects.requireNonNull(action, "action");

        String rejection = validate(action);
        if (rejection != null) {
            return ApplyResult.rejected(rejection);
        }
        String message = perform(action);
        state.getHistory().add(action);
        state.getLog().add(message);
        endTurn();
        return ApplyResult.ok(message);
    }

    // ---------- validation ----------

    private String validate(GameAction action) {
        if (state.getPhase() != GamePhase.IN_PROGRESS) {
            return "Igra je zavrsena.";
        }
        if (action.getPlayer() != state.getCurrentPlayer()) {
            return "Nije tvoj potez.";
        }
        return switch (action) {
            case BuildAction a -> validateBuild(a);
            case LinkAction a  -> validateLink(a);
            case SellAction a  -> validateSell(a);
            case LoanAction a  -> null;
            case PassAction a  -> null;
        };
    }

    private String validateBuild(BuildAction action) {
        Player player = action.getPlayer();
        Slot slot = action.getSlot();

        if (state.getBoard().containsKey(slot)) {
            return "Mjesto je vec zauzeto.";
        }
        if (!canBuildIn(player, slot.city())) {
            return "Grad nije u tvojoj mrezi.";
        }

        Industry industry = Industry.of(action.getIndustryType(), player, slot);
        int price = industry.cost() + coalPrice(industry, slot.city());
        if (player.getMoney() < price) {
            return "Nemas dovoljno novca.";
        }
        return null;
    }

    private String validateLink(LinkAction action) {
        Link link = action.getLink();

        if (state.getCanals().containsKey(link)) {
            return "Kanal je vec izgraden.";
        }
        Set<City> network = networkOf(action.getPlayer());
        if (!network.contains(link.from()) && !network.contains(link.to())) {
            return "Kanal ne dodiruje tvoju mrezu.";
        }
        if (action.getPlayer().getMoney() < GameRules.CANAL_COST) {
            return "Nemas dovoljno novca.";
        }
        return null;
    }

    private String validateSell(SellAction action) {
        Industry industry = state.getBoard().get(action.getSlot());

        if (industry == null) {
            return "Na tom mjestu nema industrije.";
        }
        if (industry.getOwner() != action.getPlayer()) {
            return "To nije tvoja industrija.";
        }
        if (!industry.sellable()) {
            return "Samo tvornica pamuka moze se prodati.";
        }
        if (industry.isFlipped()) {
            return "Industrija je vec prodana.";
        }
        if (!reachesMarket(action.getSlot().city())) {
            return "Grad nije povezan s trzistem.";
        }
        return null;
    }

    private int coalPrice(Industry industry, City city) {
        if (!industry.consumesCoal()) {
            return 0;
        }
        return findConnectedUnflippedMine(city).isPresent() ? 0 : GameRules.MARKET_COAL_PRICE;
    }

    // ---------- performing ----------

    private String perform(GameAction action) {
        return switch (action) {
            case BuildAction a -> performBuild(a);
            case LinkAction a  -> performLink(a);
            case SellAction a  -> performSell(a);
            case LoanAction a  -> performLoan(a);
            case PassAction a  -> performPass(a);
        };
    }

    private String performBuild(BuildAction action) {
        Player player = action.getPlayer();
        Slot slot = action.getSlot();
        Industry industry = Industry.of(action.getIndustryType(), player, slot);

        int coalCost = coalPrice(industry, slot.city());
        if (industry.consumesCoal()) {
            findConnectedUnflippedMine(slot.city()).ifPresent(this::flipAndCredit);
        }

        player.addMoney(-(industry.cost() + coalCost));
        state.getBoard().put(slot, industry);

        return player.getName() + " gradi " + industry.type().getDisplayName()
                + " u gradu " + slot.city().getDisplayName()
                + " (" + (industry.cost() + coalCost) + "£).";
    }

    private String performLink(LinkAction action) {
        action.getPlayer().addMoney(-GameRules.CANAL_COST);
        state.getCanals().put(action.getLink(), action.getPlayer());

        return action.getPlayer().getName() + " gradi kanal "
                + action.getLink().from().getDisplayName() + " - "
                + action.getLink().to().getDisplayName() + ".";
    }

    private String performSell(SellAction action) {
        Industry industry = state.getBoard().get(action.getSlot());
        flipAndCredit(industry);

        return action.getPlayer().getName() + " prodaje u gradu "
                + action.getSlot().city().getDisplayName() + ".";
    }

    private String performLoan(LoanAction action) {
        action.getPlayer().addMoney(GameRules.LOAN_AMOUNT);
        action.getPlayer().addIncome(-GameRules.LOAN_INCOME_PENALTY);

        return action.getPlayer().getName() + " uzima zajam.";
    }

    private String performPass(PassAction action) {
        return action.getPlayer().getName() + " preskace potez.";
    }

    private void flipAndCredit(Industry industry) {
        industry.flip();
        industry.getOwner().addIncome(industry.income());
    }

    // ---------- turn and round ----------

    private void endTurn() {
        if (isDoublePass()) {
            state.setPhase(GamePhase.FINISHED);
            return;
        }

        int next = (state.getPlayers().indexOf(state.getCurrentPlayer()) + 1)
                % state.getPlayers().size();
        state.setCurrentPlayerIndex(next);

        if (next == 0) {
            collectIncome();
            state.setRound(state.getRound() + 1);
            if (state.getRound() > GameRules.ROUNDS) {
                state.setPhase(GamePhase.FINISHED);
            }
        }
    }

    private boolean isDoublePass() {
        List<GameAction> history = state.getHistory();
        return history.size() >= 2
                && history.getLast() instanceof PassAction
                && history.get(history.size() - 2) instanceof PassAction;
    }

    private void collectIncome() {
        for (Player player : state.getPlayers()) {
            player.addMoney(player.getIncome());
        }
    }

    // ---------- network and reachability ----------

    private boolean canBuildIn(Player player, City city) {
        Set<City> network = networkOf(player);
        return network.isEmpty() || network.contains(city);
    }

    private Set<City> networkOf(Player player) {
        Set<City> network = new HashSet<>();
        for (Map.Entry<Slot, Industry> entry : state.getBoard().entrySet()) {
            if (entry.getValue().getOwner() == player) {
                network.add(entry.getKey().city());
            }
        }

        boolean grew = true;
        while (grew) {
            grew = false;
            for (Map.Entry<Link, Player> entry : state.getCanals().entrySet()) {
                if (entry.getValue() != player) {
                    continue;
                }
                Link link = entry.getKey();
                if (network.contains(link.from()) && network.add(link.to())) {
                    grew = true;
                }
                if (network.contains(link.to()) && network.add(link.from())) {
                    grew = true;
                }
            }
        }
        return network;
    }

    private Optional<Industry> findConnectedUnflippedMine(City start) {
        Set<City> visited = new HashSet<>();
        Deque<City> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            City city = queue.poll();

            for (Map.Entry<Slot, Industry> entry : state.getBoard().entrySet()) {
                if (entry.getKey().city() == city
                        && entry.getValue() instanceof CoalMine mine
                        && !mine.isFlipped()) {
                    return Optional.of(mine);
                }
            }
            for (City neighbour : neighboursOf(city)) {
                if (visited.add(neighbour)) {
                    queue.add(neighbour);
                }
            }
        }
        return Optional.empty();
    }

    private boolean reachesMarket(City start) {
        Set<City> visited = new HashSet<>();
        Deque<City> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            City city = queue.poll();
            if (city == GameRules.MARKET_CITY) {
                return true;
            }
            for (City neighbour : neighboursOf(city)) {
                if (visited.add(neighbour)) {
                    queue.add(neighbour);
                }
            }
        }
        return false;
    }

    private List<City> neighboursOf(City city) {
        List<City> neighbours = new ArrayList<>();
        for (Link link : state.getCanals().keySet()) {
            if (link.from() == city) {
                neighbours.add(link.to());
            } else if (link.to() == city) {
                neighbours.add(link.from());
            }
        }
        return neighbours;
    }

    public int finalScore(Player player) {
        int score = 0;
        for (Industry industry : state.getBoard().values()) {
            if (industry.getOwner() == player && industry.isFlipped()) {
                score += industry.victoryPoints();
            }
        }
        for (Player owner : state.getCanals().values()) {
            if (owner == player) {
                score += GameRules.LINK_VP;
            }
        }
        return score + player.getMoney() / GameRules.MONEY_PER_VP;
    }

}
