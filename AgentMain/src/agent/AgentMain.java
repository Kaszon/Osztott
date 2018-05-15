package agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentMain {
  
  public static int blueAgentsNum;
  private static int redAgentsNum;
  private static int timeOutLowerBound;
  private static int timeOutUpperBound;
  private static ArrayList<Integer> usedPorts;
  private static ArrayList<ServerThread> servers = new ArrayList<>();
  private static ArrayList<ClientThread> clients = new ArrayList<>();

  
  public static void main(String[] args) {
    usedPorts = new ArrayList<>();
    if (args.length < 4) {
      printErrorExit("Nincs elég paraméter. 4 darab kell!");
    }
    try{
      blueAgentsNum = Integer.parseInt(args[0]);
      redAgentsNum = Integer.parseInt(args[1]);
      timeOutLowerBound = Integer.parseInt(args[2]);
      timeOutUpperBound = Integer.parseInt(args[3]);
    }
    catch(NumberFormatException e){
      e.printStackTrace();
      printErrorExit("Mind a 4 paraméternek számnak kell lennie!");
    }
    
    for (int i = 0; i < blueAgentsNum + redAgentsNum; i++) {
      int actualPort = getNewPort();
      ServerThread t = new ServerThread(String.valueOf(i),actualPort,timeOutUpperBound);
      ClientThread t2 = new ClientThread(t, String.valueOf(i), actualPort, timeOutLowerBound, timeOutUpperBound);
      servers.add(t);
      clients.add(t2);
    }
    for (int i = 0; i < servers.size(); i++) {
      servers.get(i).start();
      clients.get(i).start();
    }
  }
  
  private static void printErrorExit(String msg){
    System.err.println(msg);
    System.exit(1);
  }
  
  public static void releasePort(int port){
    usedPorts.remove(new Integer(port));
  }
  
  public static int getNewPort(){
    Random rnd = new Random();
    int result = rnd.nextInt(10)+20000;
    while (usedPorts.contains(result)) {
      System.out.println(result + " port foglalt");
      result = rnd.nextInt(10)+20000;
    }
    usedPorts.add(result);
    return result;
  }
  


  
}
