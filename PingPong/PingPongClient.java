package PingPong;
import rmi.RMIException;
import rmi.Stub;
import java.net.InetSocketAddress;

/**
 * Created by saurabh on 30/04/16.
 */
public class PingPongClient {
    public static void main(String[] args) throws RMIException {
        if (args.length < 2) {
            System.out.println("Expected Address, Port");
        }
        InetSocketAddress address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        PingPongFactoryInterface factoryStub = Stub.create(PingPongFactoryInterface.class, address);
        PingPongInterface pingStub = null;
        try {
            pingStub = factoryStub.makePingServer();
        } catch (RMIException e) {
            e.printStackTrace();
        }
        int total = 0;
        int fail = 0;
        for (int i = 1; i <= 4; i++) {
            total++;
            String result = pingStub.ping(i);
            System.out.println(result);
            if (!result.equals("Pong" + i)) {
                fail++;
            }

        }
        System.out.println(total +" Tests Completed, " + fail + " Tests Failed");
    }
}