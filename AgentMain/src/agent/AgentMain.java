package agent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

public class AgentMain {
  
  private static int blueAgentsNum;
  private static int redAgentsNum;
  private static int timeOutLowerBound;
  private static int timeOutUpperBound;
  private static ArrayList<Integer> usedPorts;

  
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
      Thread t = new Thread(() -> {
        System.out.println("Ügynök kész ");
        int port = getNewPort();
        System.out.println(port + " az én portom");
        
        lookForConnection(port);
          
      });
      t.start();
    }
    
  }
  
  private static void goAgents(int team, int numOfAgents){
    for (int i = 0; i < numOfAgents; i++) {
      Thread t = new Thread();
    }
  }
  
  private static void printErrorExit(String msg){
    System.err.println(msg);
    System.exit(1);
  }
  
  private static int getNewPort(){
    Random rnd = new Random();
    int result = rnd.nextInt(100)+20000;
    while (usedPorts.contains(result)) {
      System.out.println(result + " port foglalt");
      result = rnd.nextInt(100)+20000;
    }
    usedPorts.add(result);
    return result;
  }

  private static void lookForConnection(int port){
    try(ServerSocket ss = new ServerSocket(port);
        Socket s = ss.accept();) {
        ss.setSoTimeout(timeOutUpperBound);
    
    } catch (SocketException e){
        //throw new NewException();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
  
}
