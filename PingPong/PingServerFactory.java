package PingPong;


import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

import java.net.InetSocketAddress;

public class PingServerFactory implements PingPongFactoryInterface{
    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress(Integer.parseInt(args[0]));
        PingServerFactory myFactory = new PingServerFactory();
        Skeleton<PingPongFactoryInterface> factorySkeleton = new Skeleton<PingPongFactoryInterface>(PingPongFactoryInterface.class, myFactory, address);
        try {
            factorySkeleton.start();
            System.out.println("Factory server running!");
        } catch (RMIException e) {
            e.printStackTrace();
        }

    }

    public PingPongInterface makePingServer() {
        PingPongServer server = new PingPongServer();
        Skeleton<PingPongInterface> skeleton = new Skeleton<PingPongInterface>(PingPongInterface.class, server);

        try {
            skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }

        PingPongInterface newStub = Stub.create(PingPongInterface.class, skeleton);
        return newStub;
    }
}
