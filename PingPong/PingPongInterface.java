package PingPong;

import rmi.RMIException;

public interface PingPongInterface {
    public String ping(int idNumber) throws RMIException;
}
