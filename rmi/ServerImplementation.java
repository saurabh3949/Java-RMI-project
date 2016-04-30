package rmi;

/**
 * Created by saurabh on 27/04/16.
 */

public class ServerImplementation implements Server {

    @Override
    public int addIntegers(int a, int b) throws RMIException{
        return a+b;
    }
}
