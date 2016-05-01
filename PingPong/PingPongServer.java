package PingPong;


import rmi.RMIException;

public class PingPongServer implements PingPongInterface{

    @Override
    public String ping(int idNumber) throws RMIException {
        return "Pong" + Integer.toString(idNumber);
    }
}
