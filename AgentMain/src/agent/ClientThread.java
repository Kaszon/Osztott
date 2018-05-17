package agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
  private static boolean changed = false;
  private int agency;
  private HashSet<String> secretMessages = new HashSet<>();
  private HashSet<String> enemySecretMessages = new HashSet<>();
  private ArrayList<String> alias = new ArrayList<>();
  private ServerThread server;
  private HashMap<String, Integer> aliasIdValid = new HashMap<>();
  private HashMap<String, ArrayList<Integer>> aliasIdAvaible = new HashMap<>();
  private static ArrayList<Integer> usedPorts = new ArrayList<>();
  private boolean over = false;

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
      if (!changed) {
        agentNumber = 1;
        changed = true;
      }    
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
    while (!over) {
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
          System.out.println(name + " én = " + agency + " = Tipp : " + choice);
          
          pw.println(choice);
          pw.flush();
          ArrayList<Integer> tmpArrayList = new ArrayList<>();
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
          if (serverAgency == 1) {
            for (int i = 0; i < AgentMain.blueAgentsNum; i++) {
              tmpArrayList.add(i+1);
            }
          }
          else{
            for (int i = 0; i < AgentMain.redAgentsNum; i++) {
              tmpArrayList.add(i+1);
            }
          }
          aliasIdAvaible.put(response, tmpArrayList);
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
            int n;
            System.out.println("???");
            pw.println("???");
            if (aliasIdValid.containsKey(response)) {
              n = aliasIdValid.get(response);
            }
            else if(aliasIdAvaible.containsKey(response)){
              n = aliasIdAvaible.get(response).get(rnd.nextInt(aliasIdAvaible.get(response).size()));
              aliasIdAvaible.get(response).remove(new Integer(n));
            }
            else{
              if (agency == 1) {
                n = rnd.nextInt(AgentMain.redAgentsNum)+1;
              }
              else{
                n = rnd.nextInt(AgentMain.blueAgentsNum)+1;
              }
            }
            pw.println(n);
            pw.flush();
            
            if (sc.hasNextLine()) {
              String secret;
              secret = sc.nextLine();
              enemySecretMessages.add(secret);
              System.out.println(name + " = MEGTUDTAM EGY TITKOT! A TITOK = " + secret);
            }
          }


          for (String sM : enemySecretMessages) {
            System.out.println(" - Ismert ellenséges titkok - ");
            System.out.println(sM);
          }
          if (enemySecretMessages.size() == 2) {
            System.err.println("VÉGE! - Nyert a " + (agency==1 ? "Kék" : "Piros") + "csapat");
            AgentMain.gameOver();
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
  
  public void arrasted(){
    over = true;
  }
  

    
}
