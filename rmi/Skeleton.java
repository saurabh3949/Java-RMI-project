package rmi;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
/**
 * Created by saurabh on 17/04/16.
 */
/** RMI skeleton

 <p>
 A skeleton encapsulates a multithreaded TCP server. The server's clients are
 intended to be RMI stubs created using the <code>Stub</code> class.

 <p>
 The skeleton class is parametrized by a type variable. This type variable
 should be instantiated with an interface. The skeleton will accept from the
 stub requests for calls to the methods of this interface. It will then
 forward those requests to an object. The object is specified when the
 skeleton is constructed, and must implement the remote interface. Each
 method in the interface should be marked as throwing
 <code>RMIException</code>, in addition to any other exceptions that the user
 desires.

 <p>
 Exceptions may occur at the top level in the listening and service threads.
 The skeleton's response to these exceptions can be customized by deriving
 a class from <code>Skeleton</code> and overriding <code>listen_error</code>
 or <code>service_error</code>.
 */
public class Skeleton<T>
{
    private Class<T> classObject;
    private T serverObject;
    private InetSocketAddress socketAddress;
    private ListeningThread listeningThread;
    private ServerSocket serverListener;
    private final Set<ClientHandler> clientHandlers = new HashSet<>();
    /** Creates a <code>Skeleton</code> with no initial server address. The
     address will be determined by the system when <code>start</code> is
     called. Equivalent to using <code>Skeleton(null)</code>.

     <p>
     This constructor is for skeletons that will not be used for
     bootstrapping RMI - those that therefore do not require a well-known
     port.

     @param c An object representing the class of the interface for which the
     skeleton server is to handle method call requests.
     @param server An object implementing said interface. Requests for method
     calls are forwarded by the skeleton to this object.
     @throws Error If <code>c</code> does not represent a remote interface -
     an interface whose methods are all marked as throwing
     <code>RMIException</code>.
     @throws NullPointerException If either of <code>c</code> or
     <code>server</code> is <code>null</code>.
     */

//    public static void main(String[] args) {
//        InetSocketAddress address = new InetSocketAddress("localhost", 5000);
//        ServerImplementation server = new ServerImplementation();
//        Skeleton<Server> skeleton = new Skeleton(Server.class, server, address);
//        try {
//            skeleton.start();
//        } catch (RMIException e) {
//            e.printStackTrace();
//        }
//
//        Server serverStub = Stub.create(Server.class, address);
//        int answer = 0;
//        try {
//            answer = serverStub.addIntegers(10,5);
//        } catch (RMIException e) {
//            e.printStackTrace();
//        }
//        System.out.println(answer);
//
//
//        skeleton.stop();
//
//
//        try {
//            skeleton.start();
//        } catch (RMIException e) {
//            e.printStackTrace();
//        }
//        System.out.println(skeleton.getSocketAddress().getPort());
//
//    }

    public Skeleton(Class<T> c, T server)
    {
        checkInputs(c, server);
        this.classObject = c;
        this.serverObject = server;
    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

     <p>
     This constructor should be used when the port number is significant.

     @param c An object representing the class of the interface for which the
     skeleton server is to handle method call requests.
     @param server An object implementing said interface. Requests for method
     calls are forwarded by the skeleton to this object.
     @param address The address at which the skeleton is to run. If
     <code>null</code>, the address will be chosen by the
     system when <code>start</code> is called.
     @throws Error If <code>c</code> does not represent a remote interface -
     an interface whose methods are all marked as throwing
     <code>RMIException</code>.
     @throws NullPointerException If either of <code>c</code> or
     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        checkInputs(c, server);
        this.classObject = c;
        this.serverObject = server;
        this.socketAddress = address;

    }



    /** Called when the listening thread exits.

     <p>
     The listening thread may exit due to a top-level exception, or due to a
     call to <code>stop</code>.

     <p>
     When this method is called, the calling thread owns the lock on the
     <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
     calling <code>start</code> or <code>stop</code> from different threads
     during this call.

     <p>
     The default implementation does nothing.

     @param cause The exception that stopped the skeleton, or
     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
        while (!clientHandlers.isEmpty()) {
            try {
                Thread t;
                synchronized (clientHandlers) {
                    if (!clientHandlers.isEmpty()) {
                        t = clientHandlers.iterator().next();
                    } else {
                        return;
                    }
                }
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Called when an exception occurs at the top level in the listening
     thread.

     <p>
     The intent of this method is to allow the user to report exceptions in
     the listening thread to another thread, by a mechanism of the user's
     choosing. The user may also ignore the exceptions. The default
     implementation simply stops the server. The user should not use this
     method to stop the skeleton. The exception will again be provided as the
     argument to <code>stopped</code>, which will be called later.

     @param exception The exception that occurred.
     @return <code>true</code> if the server is to resume accepting
     connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

     <p>
     The default implementation does nothing.

     @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
    }

    /** Starts the skeleton server.

     <p>
     A thread is created to listen for connection requests, and the method
     returns immediately. Additional threads are created when connections are
     accepted. The network address used for the server is determined by which
     constructor was used to create the <code>Skeleton</code> object.

     @throws RMIException When the listening socket cannot be created or
     bound, when the listening thread cannot be created,
     or when the server has already been started and has
     not since stopped.
     */
    public synchronized void start() throws RMIException
    {
        if (listeningThread!=null && listeningThread.isAlive()){
            throw new RMIException("Listening server is already running!\n");
        }


        if (socketAddress == null) {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                try {
                    serverListener = new ServerSocket(0);
                    socketAddress = new InetSocketAddress(ip, serverListener.getLocalPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                serverListener = new ServerSocket(socketAddress.getPort());
            } catch (IOException e) {
                throw new RMIException("Cannot create listening socket!\n");
            }
        }

        // If serverListener is still null:
        if (serverListener==null) throw new RMIException("Cannot create listening socket!\n");
        listeningThread = new ListeningThread();
        listeningThread.start();

    }

    /** Stops the skeleton server, if it is already running.

     <p>
     The listening thread terminates. Threads created to service connections
     may continue running until their invocations of the <code>service</code>
     method return. The server stops at some later time; the method
     <code>stopped</code> is called at that point. The server may then be
     restarted.
     */
    public synchronized void stop()  {
//        System.out.println("Stop function is called!");
        if (listeningThread.isAlive()) listeningThread.interrupt = true;

        try {
            serverListener.close();
            try {
                listeningThread.join();
                stopped(null); // Wait for closing all the clientHandlers
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void checkInputs(Class<T> c, T server){
        if (c==null) throw new NullPointerException("Class is null.\n");

        if (server==null) throw new NullPointerException("Server object is null.\n");


        if (!checkInterface(c)){
            throw new Error("The class does not represent a remote interface - an interface whose methods are all marked as throwing RMIException.\n");
        }
    }

    private boolean checkInterface(Class<T> c){
        if (!c.isInterface()) return false;
        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods){
            Class<?>[] exceptions = method.getExceptionTypes();
            if (!Arrays.asList(exceptions).contains(RMIException.class)) return false;
        }
        return true;
    }

    public InetSocketAddress getSocketAddress(){
        return this.socketAddress;
    }

    private class ListeningThread extends Thread{
        private volatile boolean interrupt = false;
        public void run() {
            while (!interrupt) {
                try {
                    Socket s = serverListener.accept();
                    (new ClientHandler(s)).start();
                } catch (IOException e) {
                    if (interrupt){
                        Thread.currentThread().interrupt();
                        return;
                    } else {
                        if (listen_error(e)){
                            continue;
                        } else {
                            stopped(e);
                            try{
                                serverListener.close();
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                            return;
                        }
                    }
                }
            }
        }
    }


    private class ClientHandler extends Thread{
        private Socket socket;
        public ClientHandler(Socket socket){
            this.socket = socket;
            clientHandlers.add(this);
        }
        @Override
        public void run() {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                Object[] objects = (Object[]) in.readObject();
                String methodName = (String) objects[0];
                Object[] args = (Object[]) objects[1];
                Class params[] = (Class[]) objects[2];
                Method method = null;
                method = classObject.getMethod(methodName, params);
                Object result = null;
                try {
                    result = method.invoke(serverObject, args);
                    out.writeObject(true);
                    Class returnType = method.getReturnType();
                    if (!returnType.equals(Void.TYPE)){
                        if (!checkInterface(returnType)){
                            out.writeObject(result);
                        } else {
                            Skeleton newSkeleton = new Skeleton(returnType, result);
                            newSkeleton.start();
                            out.writeObject(Stub.create(returnType, newSkeleton.getSocketAddress()));
                        }
                    }
                }
                catch (InvocationTargetException e){
                    out.writeObject(false);
                    out.writeObject(e.getTargetException());
                }
            }
            catch (Exception e){
                service_error(new RMIException(e));
            }
            finally {
                clientHandlers.remove(this);
                try {
                    if (out!=null) {
                        out.flush();
                        out.close();
                    }
                    if (in != null) in.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




}
