package PingPong;

import rmi.RMIException;
import rmi.Stub;

import java.net.InetSocketAddress;

/**
 * Created by saurabh on 30/04/16.
 */
public class PingPongClient {
    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("Expected Address, Port");
        }
        InetSocketAddress address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        PingPongFactoryInterface factoryStub = Stub.create(PingPongFactoryInterface.class, address);
        PingPongInterface pingStub = null;
        try {
            pingStub = factoryStub.makePingServer();
            PingPongInterface pingStub2 = factoryStub.makePingServer();
            System.out.println(pingStub2.ping(3));

        } catch (RMIException e) {
            e.printStackTrace();
        }

        try {
            String answer = pingStub.ping(1);
            System.out.println(answer);
        } catch (RMIException e) {
            e.printStackTrace();
        }


    }
}
