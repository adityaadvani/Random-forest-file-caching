/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package serverMachine;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Aditya Advani
 */
public interface ServerIF extends Remote {

    void getClientRequest(String file_in, String level_in, String node_in, String clientAddress_in, String Path, String Response) throws RemoteException;

    void getFileFromParent(String file, String fileContents, int level, int node) throws RemoteException;
}
