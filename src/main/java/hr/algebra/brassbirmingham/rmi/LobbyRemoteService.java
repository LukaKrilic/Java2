package hr.algebra.brassbirmingham.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface LobbyRemoteService extends Remote {
    String REMOTE_OBJECT_NAME = "hr.algebra.brassbirmingham.rmi.lobby";

    void register(String playerName, int port) throws RemoteException;

    void unregister(String playerName) throws RemoteException;

    Map<String, Integer> getAvailablePlayers() throws RemoteException;
}
