/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package clientMachine;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Aditya Advani
 */
public interface ClientIF extends Remote {

    void getServerResponse(boolean available, String file, String fileContents, String Path, String response) throws RemoteException,FileNotFoundException;
}
