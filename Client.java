/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package clientMachine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Advani
 */
public class Client extends UnicastRemoteObject implements ClientIF, Serializable {

    static HashMap<String, String> MyFile = new HashMap<>();
    static String localmachineName;

    public Client() throws RemoteException {
        super();
    }

    @Override
    public void getServerResponse(boolean available, String file, String fileContent, String Path, String response) throws RemoteException, FileNotFoundException {
        if (available) {
            MyFile.put(file, fileContent);
            System.out.println("File added to client machine.\nPath is --> " + Path);
            try {
                System.out.println("File "+file+" has been retrieved from the server using above path.");
                PrintWriter out = new PrintWriter("/home/stu4/s1/aa5394/Documents/Courses/Distributed Systems/hw1randomforest/" + localmachineName + "/" + file);
                out.println(fileContent);
                out.flush();
		System.exit(0);
                
            } catch (FileNotFoundException ex) {
                
            } catch (IOException ex) {
                
            }
        } else {
            System.out.println("Search Path followed is: " + Path);
            System.out.println(response);
	    System.exit(0);
        }
    }

//create hardcoded server list
    static HashMap<Integer, String> serverMap = new HashMap<>();

    //method to populate the static server map
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

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException, IOException, FileNotFoundException {
        initializeServerMap(serverMap);
        Registry r = LocateRegistry.createRegistry(5550);
        r.rebind("Client", new Client());

        //get unique hashcode of current file request;
        //gather inputs
        int totalServers = serverMap.size();
        String level = "2";
        String file = args[0];
        String node = "" + ((int) (Math.random() * 3));

        //concatenate inputs and create bounded hashcode
        String complete = file + level + node;
        int hash = Math.abs(complete.hashCode());
        hash = hash % totalServers;
	System.out.println(hash);

        //retrieve the server address from the acquired hashcode
        String serverAddress = serverMap.get(hash);

        //connect to registry on server machine
        Registry reg = LocateRegistry.getRegistry(serverAddress, 5550);
        ServerIF server = (ServerIF) reg.lookup("Server");

        //get local ip for server reply
        InetAddress IP = InetAddress.getLocalHost();
        String clientAddress = IP.getHostAddress();
        localmachineName = IP.getHostName();
        String firstHopPath = localmachineName+" [client] --> ";

        File cdir = new File(localmachineName);
        if (!cdir.exists()) {
            try {
                cdir.mkdir();
            } catch (SecurityException se) {
            }
        } 

        server.getClientRequest(file, level, node, clientAddress, firstHopPath, "");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            
        }
    }
}
