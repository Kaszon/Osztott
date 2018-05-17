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
  public static int redAgentsNum;
  private static int timeOutLowerBound;
  private static int timeOutUpperBound;
  private static ArrayList<Integer> usedPorts;
  private static ArrayList<ServerThread> servers = new ArrayList<>();
  private static ArrayList<ClientThread> clients = new ArrayList<>();
  private static boolean end = false;

  
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
      ServerThread t;
      ClientThread t2 = null;
      t = new ServerThread(t2 ,String.valueOf(i),actualPort,timeOutUpperBound);
      t2 = new ClientThread(t, String.valueOf(i), actualPort, timeOutLowerBound, timeOutUpperBound);
      t.setClient(t2);
      servers.add(t);
      clients.add(t2);
    }
    for (int i = 0; i < servers.size(); i++) {
      servers.get(i).start();
      clients.get(i).start();
    }
    
    while (!end) {
      int sum = 0;
      for (int i = 0; i < blueAgentsNum; i++) {
        sum = sum + (servers.get(i).isAlive() ? 1 : 0);
      }
      if (sum == 0) {
        end = true;
        System.out.println("Piros Nyert");
        gameOver();
        break;
      }
      sum = 0;
      for (int i = blueAgentsNum; i < blueAgentsNum+redAgentsNum; i++) {
        sum = sum + (servers.get(i).isAlive() ? 1 : 0);
      }
      if (sum == 0) {
        end = true;
        System.out.println("Kék Nyert");
        gameOver();
      }
    }
  }
  
  public static void gameOver(){
    for (int i = 0; i < clients.size(); i++) {
      clients.get(i).arrasted();
      servers.get(i).arrasted();
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
