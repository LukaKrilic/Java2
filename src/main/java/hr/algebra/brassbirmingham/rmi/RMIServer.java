package hr.algebra.brassbirmingham.rmi;

import hr.algebra.brassbirmingham.jndi.ConfigurationKey;
import hr.algebra.brassbirmingham.jndi.ConfigurationReader;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

public class RMIServer {
    public static final int RMI_PORT = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.RMI_SERVER_PORT);
    public static final String HOSTNAME = ConfigurationReader.getStringValueForKey(ConfigurationKey.HOST_NAME);
    private static final int RANDOM_PORT_HINT = 0;

    public static void start() {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            ChatRemoteService chatRemoteService = new ChatRemoteServiceImpl();
            ChatRemoteService skeleton = (ChatRemoteService) UnicastRemoteObject.exportObject(chatRemoteService, RANDOM_PORT_HINT);
            registry.rebind(ChatRemoteService.REMOTE_OBJECT_NAME, skeleton);
            Logger.getLogger(RMIServer.class.getName()).info("Object registered in RMI registry" );
        } catch (RemoteException e) {
            Logger.getLogger(RMIServer.class.getName())
                    .info("RMI registry already running, joining existing one");
        }
    }
    public static void main(String[] args) {
        start();
    }
}
