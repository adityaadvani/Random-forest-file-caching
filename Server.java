//package serverMachine;

//import dependency packages
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Class contains the complete business logic for running the server side
 * machines in the required Client-Server random forest distributed file storage
 * and transfer implementation.
 *
 * @author Aditya Advani
 */
public class Server extends UnicastRemoteObject implements ServerIF, Serializable {

    String fileResponse = "";

    /* static data structures to store file request count, a record of files on 
     the current machine and a list of all the participating machines in the 
     random forest implementation*/
    static HashMap<String, Integer> fileCount = new HashMap<>();
    static HashMap<String, String> MyFile = new HashMap<>();
    static HashMap<Integer, String> serverMap = new HashMap<>();

    /**
     * Default constructor method of the Server Class.
     *
     * @throws RemoteException
     */
    protected Server() throws RemoteException {
        super();
    }

    /**
     * this method populates the 'serverMap' HashMap that contains a list of all
     * the participating machines in the random forest implementation. to add
     * more machines in the system, simply create entries in the data structure
     * below. The hashCode range adapts dynamically. However, every file needs
     * to be rehashed to its new machine in the system if the number of machines
     * change.
     *
     * @param serverMap
     */
    public static void initializeServerMap(HashMap<Integer, String> serverMap) {
        serverMap.put(0, "129.21.37.49");
        serverMap.put(1, "129.21.37.16");
        serverMap.put(2, "129.21.37.23");
        serverMap.put(3, "129.21.37.1");
        serverMap.put(4, "129.21.37.7");
        serverMap.put(5, "129.21.37.20");
        serverMap.put(6, "129.21.37.9");
        serverMap.put(7, "129.21.37.6");
        serverMap.put(8, "129.21.37.22");
        serverMap.put(9, "129.21.37.10");
        serverMap.put(10, "129.21.37.19");
        serverMap.put(11, "129.21.37.24");
        serverMap.put(12, "129.21.37.15");
        serverMap.put(13, "129.21.37.11");
        serverMap.put(14, "129.21.37.21");
        serverMap.put(15, "129.21.37.25");
        serverMap.put(16, "129.21.37.8");
        serverMap.put(17, "129.21.37.2");
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * This method retrieves and processes the file request query from the
     * remote client. This method first establishes the position of the current
     * server machine in the tree structure for the file in question. It then
     * updates the current machines file request counter and returns the
     * requested file back to the client via a remote method invocation at the
     * client machine, provided that the file is present at the current machine.
     * If the file is not present, the current server machine passes the query
     * over to its parent, if any, in the tree structure. If a file has been
     * requested 5 times, it is deemed popular and is forwarded to the children
     * server machines, if any, of the current server machine in the tree
     * structure of the file in question.
     *
     * @param file
     * @param level
     * @param node
     * @param clientAddress
     * @param Path
     * @param Response
     * @throws RemoteException
     */
    @Override
    public void getClientRequest(String file, String level, String node, String clientAddress, String Path, String Response) throws RemoteException {

        try {
            InetAddress IP = InetAddress.getLocalHost();
            String localmachineName = IP.getHostName();
            System.out.println("findig file "+ file +" on " + localmachineName);
            Path = Path + localmachineName + "(" + level + "," + node + ")";

            String filePath = "/home/stu4/s1/aa5394/Documents/Courses/Distributed Systems/hw1randomforest/" + localmachineName + "/";
	    System.out.println("\nThe files in this server are:");
            File folder = new File(filePath);
            File[] listOfFiles = folder.listFiles();

            for (File f : listOfFiles) {
                if (f.isFile()) {

                    System.out.println(f.getName());
                    String content = readFile(filePath + "/" + f.getName(), Charset.defaultCharset());
                    
                }
            }
	    System.out.println("");

            if (level.equals("2")) {
                Path = Path + " [search began]";
            }

            //if server has the file
            if (MyFile.containsKey(file)) {

                if (fileCount.containsKey(file)) {

                    //increment file request count
                    int count = fileCount.get(file);
                    count++;

                    //if file is popular
                    if (count == 5) {

                        //forward files to children
                        if (level.equals("0") || level.equals("1")) {
                            //forward files to both children
                            int ChildLevel = Integer.parseInt(level) + 1;
                            int firstChildNode = Integer.parseInt(node) * 2;
                            int secondChildNode = (Integer.parseInt(node) * 2) + 1;
                            int totalServers = serverMap.size();

                            //get first child address
                            //concatenate inputs and create bounded hashcode
                            String complete = file + ChildLevel + firstChildNode;
                            int hash = Math.abs(complete.hashCode());
                            hash = hash % totalServers;
                            //retrieve the server address from the acquired hashcode
                            String firstChildAddress = serverMap.get(hash);

                            //get second child address
                            //concatenate inputs and create bounded hashcode
                            complete = file + ChildLevel + secondChildNode;
                            hash = Math.abs(complete.hashCode());
                            hash = hash % totalServers;
                            //retrieve the server address from the acquired hashcode
                            String secondChildAddress = serverMap.get(hash);
                            
                            System.out.println("Forwarding files to children\n1. "+firstChildAddress+"\n2. "+secondChildAddress);

                            //connect to registry on first child machine
                            Registry reg = LocateRegistry.getRegistry(firstChildAddress, 5550);

                            try {
                                ServerIF firstChild = (ServerIF) reg.lookup("Server");
                                //pass control over to child node
                                firstChild.getFileFromParent(file, MyFile.get(file), ChildLevel, firstChildNode);
                            } catch (NotBoundException ex) {
                                
                            }
                            System.out.println("Forwarded to first child");

                            //connect to registry on second child machine
                            reg = LocateRegistry.getRegistry(secondChildAddress, 5550);
                            try {
                                ServerIF secondChild = (ServerIF) reg.lookup("Server");
                                //pass control over to child node
                                secondChild.getFileFromParent(file, MyFile.get(file), ChildLevel, secondChildNode);
                            } catch (NotBoundException ex) {
                               
                            }
                            System.out.println("Forwarded to second child");
                        
                        } else {
                            //dont do anything
                        }
                    }

                    fileCount.put(file, count);
                } else {
                    //create entry
                    fileCount.put(file, 1);
                }

                Path = Path + " [file found]";

                Registry reg = LocateRegistry.getRegistry(clientAddress, 5550);
                try {
                    ClientIF client = (ClientIF) reg.lookup("Client");

                    //pass control over to parent node
		    System.out.println("Sent file "+file+" to client at"+ clientAddress);
                    client.getServerResponse(true, file, MyFile.get(file), Path, "Add nodes that contain file");
                } catch (NotBoundException ex) {
		    
                }

            } else {

                if (level.equals("1") || level.equals("2")) {
                    //forward request to parent
                    int totalServers = serverMap.size();
                    int parentLevel = Integer.parseInt(level) - 1;
                    int parentNode = (Integer.parseInt(node) / 2);

                    //concatenate inputs and create bounded hashcode
                    String complete = file + parentLevel + parentNode;
                    int hash = Math.abs(complete.hashCode());
                    hash = hash % totalServers;
		    System.out.println(hash);

                    //retrieve the server address from the acquired hashcode
                    String parentServerAddress = serverMap.get(hash);

                    Path = Path + " --> ";

                    //connect to registry on server machine
                    Registry reg = LocateRegistry.getRegistry(parentServerAddress, 5550);
                    try {
                        ServerIF ParentServer = (ServerIF) reg.lookup("Server");
                        String pLevel = "" + parentLevel;
                        String pNode = "" + parentNode;

                        System.out.println("Forwarding client request to parent");
                        System.out.println("parent ip and location: "+ parentServerAddress+" ("+pLevel+","+pNode+")");
                        System.out.println("Path so far: "+ Path+" [going to parent]");
                        //pass control over to parent node
                        ParentServer.getClientRequest(file, pLevel, pNode, clientAddress, Path, "");
                    } catch (NotBoundException ex) {
                        
                    }
                } else {

                    Path = Path + " [search ended]";

                    //connect to registry on server machine
                    Registry reg = LocateRegistry.getRegistry(clientAddress, 5550);
                    try {
                        ClientIF client = (ClientIF) reg.lookup("Client");

                        //pass control over to parent node
                        client.getServerResponse(false, null, null, Path, "File is not available in the system.");
                    } catch (NotBoundException ex) {
                        
                    }
                }
            }
        } catch (UnknownHostException ex) {
            
        } catch (IOException ex) {
            
        }
    }

    @Override
    public void getFileFromParent(String file, String fileContents, int level, int node) throws RemoteException {
        MyFile.put(file, fileContents);
        try {
            InetAddress IP = InetAddress.getLocalHost();
            String localmachineName = IP.getHostName();
            System.out.println("Replicating file "+file+" from parent");
            System.out.println("current server: "+localmachineName+" ("+level+","+node+")");
            PrintWriter out = new PrintWriter("/home/stu4/s1/aa5394/Documents/Courses/Distributed Systems/hw1randomforest/" + localmachineName + "/" + file);
            out.println(fileContents);
            out.flush();

        } catch (FileNotFoundException ex) {
            
        } catch (IOException ex) {
            
        }
    }

    public static void main(String[] args) throws RemoteException {
        try {
            initializeServerMap(serverMap);

            Registry r = LocateRegistry.createRegistry(5550);
            r.rebind("Server", new Server());
            InetAddress IP = InetAddress.getLocalHost();
            String localmachineName = IP.getHostName();
            System.out.println(localmachineName);
	    File cdir = new File(localmachineName);
            if (!cdir.exists()) {
                try {
                    cdir.mkdir();
                } catch (SecurityException se) {
                }
            } 
            String filePath = "/home/stu4/s1/aa5394/Documents/Courses/Distributed Systems/hw1randomforest/" + localmachineName + "/";
            File folder = new File(filePath);
            File[] listOfFiles = folder.listFiles();

            for (File f : listOfFiles) {
                if (f.isFile()) {
                    MyFile.put(f.getName(), (readFile(filePath + "/" + f.getName(), Charset.defaultCharset())));
                }
            }
        } catch (MalformedURLException ex) {
            System.out.println("exception: " + ex);
        } catch (UnknownHostException ex) {
            
        } catch (IOException ex) {
            
        }
    }
}
