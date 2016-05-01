package PingPong;

import rmi.RMIException;

/**
 * Created by saurabh on 30/04/16.
 */
public interface PingPongFactoryInterface {
    public PingPongInterface makePingServer() throws RMIException;
}
