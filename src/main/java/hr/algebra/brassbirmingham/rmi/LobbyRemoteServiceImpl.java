package hr.algebra.brassbirmingham.rmi;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyRemoteServiceImpl implements LobbyRemoteService {
    private final Map<String, Integer> availablePlayers = new ConcurrentHashMap<>();

    @Override
    public void register(String playerName, int port) throws RemoteException {
        availablePlayers.put(playerName, port);
    }

    @Override
    public void unregister(String playerName) throws RemoteException {
        availablePlayers.remove(playerName);
    }

    @Override
    public Map<String, Integer> getAvailablePlayers() throws RemoteException {
        return Map.copyOf(availablePlayers);
    }
}
