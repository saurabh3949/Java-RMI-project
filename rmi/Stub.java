package rmi;
import rmi.RMIException;
import rmi.Skeleton;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Saurabh and Siddhartha on 17/04/16.
 */
public class Stub {

    //CASE 1  class object and address
    public static<T> T create(Class<T> classObject, InetSocketAddress address){

        if(classObject==null || address==null)
        {
            throw new NullPointerException("Server Create Error: Either Object or Socket Address is null");
        }

        validateClassObject(classObject); //would throw error for imporper interface defination

        try
        {
            InvocationHandler handler = new MyInvocationHandler(address, classObject);
            T proxy = (T) Proxy.newProxyInstance(
                    classObject.getClassLoader(),
                    new Class[]{classObject},
                    handler
            );
            return proxy;
        }
        catch(Exception e)
        {
            throw new Error("Error creating Stub for Remote Interface "+ classObject.getCanonicalName() + ": " + e);
        }
    }

    //CASE 2 class object and address
    public static<T> T create(Class<T> classObject, Skeleton<T> skeleton){

        if(classObject==null || skeleton==null)
        {
            throw new NullPointerException("Server Create Error: Either Object or Skeleton is null");
        }

        validateClassObject(classObject); //would throw error for imporper interface defination

        if(skeleton.getSocketAddress() == null)
        {
            throw new IllegalStateException("Stub Create Error: skeleton uninitialized");
        }

        try
        {
            InvocationHandler handler = new MyInvocationHandler(skeleton.getSocketAddress(),classObject);
            T proxy = (T) Proxy.newProxyInstance(
                    classObject.getClassLoader(),
                    new Class[]{classObject},
                    handler
            );
            return proxy;
        }
        catch(Exception e)
        {
            throw new Error("Error creating Stub for Remote Interface "+ classObject.getCanonicalName() + ": " + e);
        }
    }

    //CASE 3 class object and address
    public static<T> T create(Class<T> classObject, Skeleton<T> skeleton, String hostname){

        if(classObject==null || skeleton==null || hostname==null || hostname=="")
        {
            throw new NullPointerException("Server Create Error: Object or Skeleton or Hostname is null");
        }

        validateClassObject(classObject); //would throw error for imporper interface defination

        if(skeleton.getSocketAddress() == null || skeleton.getSocketAddress().getPort() == 0)
        {
            throw new IllegalStateException("Stub Create Error: skeleton unintialized");
        }

        try
        {
            InetSocketAddress address = new InetSocketAddress(hostname, skeleton.getSocketAddress().getPort());
            InvocationHandler handler = new MyInvocationHandler(address,classObject);
            T proxy = (T) Proxy.newProxyInstance(
                    classObject.getClassLoader(),
                    new Class[]{classObject},
                    handler
            );
            return proxy;
        }
        catch(Exception e)
        {
            throw new Error("Error creating Stub for Remote Interface "+ classObject.getCanonicalName() + ": " + e);
        }
    }
    //Validate if all functions of class throws RMIException
    public static <T> boolean validateClassObject(Class<T> classObject)
    {
        for(Method m: classObject.getMethods())
        {
            if(!Arrays.asList(m.getExceptionTypes()).contains(RMIException.class))
            {
                throw new Error("Method "+ m.getName() + " of the class " + classObject.getCanonicalName()
                        + " doesn't throw RMIException. (Requirement of a remote interface)");
            }
        }
        return true;
    }
/*
    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("localhost", 5000);
        Server server = Stub.create(Server.class, address);
        int answer = server.addIntegers(5,5);
        System.out.println(answer);
    }
*/

    private static class MyInvocationHandler<T> implements InvocationHandler, Serializable {
        private InetSocketAddress address;
        private Class<T> implementationClass;

        public MyInvocationHandler(InetSocketAddress address,Class implementationClass){
            this.address = address;
            this.implementationClass = implementationClass;
        }

        public InetSocketAddress getImplementationAddress()
        {
            return address;
        }

        public Class getImplementationClass()
        {
            return implementationClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            //check if the function called was equal
            if(method.equals(Object.class.getMethod("equals", Object.class)))
            {
                if (args[0] instanceof Proxy) {
                    MyInvocationHandler second_handler = (MyInvocationHandler) Proxy.getInvocationHandler(args[0]);
                    return (implementationClass.equals(second_handler.getImplementationClass()) && address.equals(second_handler.getImplementationAddress()));
                }
                return false;
            }

            //Overiding hashCode fucntion of OBJeCT classes
            if(method.equals(Object.class.getMethod("hashCode")))
            {
                return implementationClass.hashCode() * address.hashCode();
            }

            if(method.equals(Object.class.getMethod("toString")))
            {
                return implementationClass.getCanonicalName() + " " + address.toString();
            }

            try
            {
                Socket socket = new Socket(address.getHostName(), address.getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                Class params[] = method.getParameterTypes();
                Object[] objects = new Object[]{method.getName(), args, params};
                out.writeObject(objects);
                out.flush();

                Object result = null;

//                if(!method.getReturnType().equals(Void.TYPE))
//                {
                result = in.readObject();
//                }

                in.close();
                out.close();
                socket.close();

                if(result != null && result instanceof Exception) {
                    if (method.getReturnType().isAssignableFrom(Exception.class)) return result;
                    throw (Exception) result;
                }
                else
                {
                    return result;
                }
            }
            catch (Exception e)
            {
                if(Arrays.asList(method.getExceptionTypes()).contains(e.getClass()))
                {
                    throw e;
                }
                throw new RMIException(e);
            }

        }
    }
}
