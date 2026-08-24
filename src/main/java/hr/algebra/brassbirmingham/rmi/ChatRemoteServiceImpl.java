package hr.algebra.brassbirmingham.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ChatRemoteServiceImpl implements ChatRemoteService {

    private List<String> chatMessages;

    public ChatRemoteServiceImpl() {
        chatMessages = new ArrayList<>();
    }

    @Override
    public void sendChatMessage(String message) throws RemoteException {
        chatMessages.add(message);
    }

    @Override
    public List<String> getAllMessages() throws RemoteException {
        return chatMessages;
    }
}
