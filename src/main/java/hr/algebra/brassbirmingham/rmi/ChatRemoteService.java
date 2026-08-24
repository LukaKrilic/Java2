package hr.algebra.brassbirmingham.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatRemoteService extends Remote {
    String REMOTE_OBJECT_NAME = "hr.algebra.brassbirmingham.hr.algebra.brassbirmingham.hr.algebra.brassbirmingham.rmi.service";
    void sendChatMessage(String message) throws RemoteException;
    List<String> getAllMessages() throws RemoteException;
}
