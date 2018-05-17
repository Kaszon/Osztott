package agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class ServerThread extends Thread implements Runnable{
    
    private String name;
    private int port;
    private int t2;
    private Socket s = null;
    private PrintWriter pw = null;
    private Scanner sc = null;
    private static int agentNumber = 1;
    private static boolean changed = false;
    private int agentId;
    private int agency;
    private HashSet<String> secretMessages = new HashSet<>();
    private HashSet<String> toldSecretMessages = new HashSet<>();
    private ArrayList<String> alias = new ArrayList<>();
    private Random rnd = new Random();
    private boolean over = false;
    private ClientThread client;

  public void setClient(ClientThread client) {
    this.client = client;
  }

    public ServerThread(ClientThread client,String name, int port, int t2) {
        this.client = client;
        this.name = name;
        this.port = port;
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
        agentId = agentNumber;
        System.out.println(name + " " + agentId);
        agentNumber++;
//        for (String a : alias) {
//          System.out.println(name + " = " + a);
//        }
    }

    @Override
    public void run() {
      while (!over) {
        try (ServerSocket ss = new ServerSocket(port);)
        {
          System.out.println(name + " = Port : " + port);
//          System.out.println(name + " = T2 : " + t2);
          ss.setSoTimeout(t2);
          s = ss.accept();
          sc = new Scanner(s.getInputStream());
          pw = new PrintWriter(s.getOutputStream());
          System.out.println(name + " = KAPCSOLAT!");
          
          pw.println(alias.get(rnd.nextInt(alias.size())));
          pw.flush();
          
          int clientAgencyGuess = Integer.parseInt(sc.nextLine());
          String tmp;
          int guess;
          if (agency == clientAgencyGuess){
            pw.println("OK");
            pw.flush();
            
            if (sc.hasNextLine()) {
              tmp = sc.nextLine();
              
              if (tmp.equals("OK")) {
              System.out.println("BARÁT");
              pw.println(getRandomSecretMsg());
              pw.flush();
              secretMessages.add(sc.nextLine());
              }
              
              if (tmp.equals("???")) {
                System.out.println("ELLENFÉL");
                guess = Integer.parseInt(sc.nextLine());
                if (guess == agentId) {
                  tmp = getRandomSecretMsg();
                  while (toldSecretMessages.contains(tmp)) {
                    tmp = getRandomSecretMsg();
                  }
                  toldSecretMessages.add(tmp);
                  if (secretMessages.equals(toldSecretMessages)) {
                    System.err.println("LE VAGYOK TARTÓZTATVA!!!!!!!!");
                    over=true;
                    client.arrasted();
                  }
                  pw.println(getRandomSecretMsg());
                  pw.flush();
                  
                }
                else {
                  s.close();
                }
              }
            }
            
          }
          else{
            s.close();
          }
        } catch (SocketTimeoutException ste) {
          System.out.println(name + " = lejárt");
        } catch (IOException e){
            
        }
        finally{
          AgentMain.releasePort(port);
          port = AgentMain.getNewPort();
            try {
              if (sc != null) {
                sc.close();
              }
              if (s != null) {
                s.close();                
              }
              if (pw != null) {
                pw.close();
              }
            } catch (IOException ex) {
            } 
        }
      }
   }

  public int getPort() {
    return port;
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

  public boolean isOver() {
    return over;
  }
  
}
