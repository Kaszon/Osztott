package agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public class ClientThread extends Thread implements Runnable{
  
  private String name;
  private int port;
  private int t1;
  private int t2;
  private Socket s = null;
  private HashSet<String> blueAlias = new HashSet<>();
  private HashSet<String> redAlias = new HashSet<>();
  private Random rnd = new Random();
  private static int agentNumber = 1;
  private int agency;
  private HashSet<String> secretMessages = new HashSet<>();
  private ArrayList<String> alias = new ArrayList<>();
  private ServerThread server;
  private static ArrayList<Integer> usedPorts = new ArrayList<>();

  public ClientThread(ServerThread server, String name, int port, int t1, int t2) {
    this.server = server;
    this.name = name;
    this.port = port;
    this.t1 = t1;
    this.t2 = t2;
    if (Integer.parseInt(name) < AgentMain.blueAgentsNum) {
      agency = 1;
    }
    else{
      agency = 2;
      agentNumber = 1;
    }
    readFromFile(agency, agentNumber);
    agentNumber++;
  }
  
  private int getServerPort(){
    int result = rnd.nextInt(10)+20000;
    while (usedPorts.contains(result) || result == server.getPort()) {
      result = rnd.nextInt(10)+20000;
    }
    usedPorts.add(result);
    return result;
  }
  
  private int getTime() {
    Random r = new Random();        
    int waitTime = r.nextInt(t2 - t1 + 1) + t1;
    return waitTime;
  }

  @Override
  public void run() {
    while (true) {
        String response = "";
        int choice;
        int serverPort = getServerPort();
        int serverAgency = 0;
        
        try (Socket s = new Socket("localhost",serverPort);
            Scanner sc = new Scanner(s.getInputStream());
            PrintWriter pw = new PrintWriter(s.getOutputStream());)
        {
          s.setSoTimeout(getTime());
          System.out.println(name + " = kapcsolat a " + serverPort + " porton");
//          try{
            if (sc.hasNextLine()) {
              response = sc.nextLine();              
            }
//          }catch (NoSuchElementException e){
//            for (Integer uP : usedPorts) {
//              System.err.println("HIBA = " + uP);
//            }
//            System.exit(1);
//          }
          
          choice = agencyChoice(response);
          System.out.println(name + " = Alias : " + response);
          System.out.println(name + " = Tipp : " + choice);
          
          pw.println(choice);
          pw.flush();
          
          if (sc.hasNextLine() && sc.nextLine().equals("OK")) {
            switch (choice) {
              case 1:
                blueAlias.add(response);
                serverAgency = 1;
                break;
              case 2:
                redAlias.add(response);
                serverAgency = 2;
              default:
            }
          }
          else{ //ROSSZ A TIPP, FORDÍTVA VAN!
            switch (choice) {
              case 2:
                blueAlias.add(response);
                serverAgency = 1;
                break;
              case 1:
                redAlias.add(response);
                serverAgency = 2;
              default:
            }            
          }
          System.out.println(" - Blue - ");
          for (String bA : blueAlias) {
            System.out.println(bA);
          }
          System.out.println(" - Red - ");
          for (String rA : redAlias) {
            System.out.println(rA);
          }
          
          if (agency == serverAgency) {
            pw.println("OK");
            pw.println(getRandomSecretMsg());
            pw.flush();
            if (sc.hasNextLine()) {
              secretMessages.add(sc.nextLine());
            }
          }
          else{
            pw.println("???");
            pw.flush();
          }
          
          
          System.out.println(" - Ismert üzenetek - ");
          for (String sM : secretMessages) {
            System.out.println(sM);
          }
          
//          System.out.println(name + " = T2 : " + t2);
        } catch (IOException e){
            
        }
        //port = AgentMain.getNewPort();
        usedPorts.remove(new Integer(serverPort));
      }
  }

  private int agencyChoice(String response) {
    int choice;
    if (blueAlias.contains(response)) {
      choice = 1;
    }
    else if (redAlias.contains(response)) {
      choice = 2;
    }
    else{
      choice = rnd.nextInt(2)+1;
    }
    return choice;
  }
  
  private void readFromFile(int agency, int agent){
    String tmp = "";
    try (Scanner sc = new Scanner(new File("./files/agent" + agency + "-" + agent + ".txt"));){
      tmp = sc.nextLine();
      secretMessages.add(sc.nextLine());
      alias.addAll(Arrays.asList(tmp.split(" ")));
//      for (String s : tmp.split(" ")) {
//        alias.add(s);
//      }
    } catch (FileNotFoundException ex) {
    }
  }

  private String getRandomSecretMsg() {
    int size = secretMessages.size();
    int index = rnd.nextInt(size);
    int i = 0;
    for (String sM : secretMessages) {
      if (i == index) {
        return sM;
      }
      i++;
    }
    return "";
  }
  

    
}
