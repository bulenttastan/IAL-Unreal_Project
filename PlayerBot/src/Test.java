
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.agent.state.level1.IAgentStateUp;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ObserverFactory;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class Test implements Runnable {
    
    static String myId = null;//"DM-TrainingDay.ObservedPlayer16";
    static String myName = "Bulent";
    static String opponentId = null;//"DM-TrainingDay.GBxBot15";
    static String opponentName = "Azure";//"Jack";
    static long startTimeFrom = 0;


    String botId;
    PrintWriter myFile;
    final static long gameStart = System.currentTimeMillis();
    public Test(String id) {
        botId = id;
    }

    public void run() {

        UT2004ObserverFactory observerFactory = new UT2004ObserverFactory(new TestUT2004BotObserverModule());
        UT2004AgentParameters observerParameters = new UT2004AgentParameters()
                          .setAgentId(new AgentId("observer"))
                          .setWorldAddress(new SocketConnectionAddress("localhost", 3002));

        TestUT2004BotObserver observer = (TestUT2004BotObserver) observerFactory.newAgent(observerParameters);

        observer.start();
        try{
            FileWriter myFileWriter = new FileWriter(botId+"_"+gameStart+".txt");
            myFile = new PrintWriter(myFileWriter);

            observer.startObserving(botId, myFile);
            
            observer.hookListeners();
            System.out.println("Observer Started");

            try{Thread.sleep(180000);} catch(Exception ex){}

            observer.testMessages();
            observer.stop();
            System.out.println("Observer Stopped");
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if(observer.inState(IAgentStateUp.class)){
                observer.kill();
                System.out.println("Observer Killed bc it was in Up State");
                myFile.close();
            }
        }
        
    }

    public static void initializeServer(){
        TestControlServer server = TestControlServer.createServer();
        String[][] ids = server.playerNameAndIds();
        
        for(int i=0; i<ids.length; i++){
            System.out.println(ids[i][1]);
            if(ids[i][0].equals(myName))
                myId = ids[i][1];
            else if(ids[i][0].equals(opponentName)){
                opponentId = ids[i][1];
            } else{
                System.out.println("Other Player: "+ids[i][0]+" "+ids[i][1]);
            }
        }
        
        TestUT2004BotObserver.setNavPoints(server.getWorldView().getAll(NavPoint.class).values());

        System.out.println("My Bot: "+myId);
        System.out.println("Opponent: "+opponentId);
        //server.kick("DM-TrainingDay.GBxBot2");
        server.end();
    }

    public static long getGameTime(){
        return System.currentTimeMillis() - gameStart + startTimeFrom;
    }

    public static void showInitMessage(){
        DateFormat formatter = new SimpleDateFormat("hh:mm:ss:SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(gameStart);
        System.out.println("Observation started at "+formatter.format(calendar.getTime()));
    }
    public static void main(String[] args){
        initializeServer();
        showInitMessage();
        new Thread(new Test(myId)).start();
        //new Thread(new Test(opponentId)).start();
    }

}






///// The code when observation wasn't working properly,
///// I had to restart the observer again and again
//            long startTime = System.currentTimeMillis();
//            while(true){
//                if(observer.getState().getFlag().isState(AgentStateFailed.class)){//TestUT2004BotObserver.killed){
//                    observer.stop();
//                    observer.kill();
//                    //rehooking up the observer, since bot was dead
//                    observer = (TestUT2004BotObserver) observerFactory.newAgent(observerParameters);
//                    observer.start();
//                    observer.startObserving(botId);
//                    if(botId.equals(myBot)){ observer.ImOpponent = false;}
//                    else{ observer.ImOpponent = true;}
//                    observer.hookListeners();
//                    System.out.println("re-hooked up (state: "+observer.getState().getFlag().toString()+")");
//                }
//                //200ms is enough time to get failed message from server
//                try{Thread.sleep(200);} catch(Exception ex){}
//
//                long currentTime = System.currentTimeMillis();
//                if(currentTime-startTime > 60000) break;
//            }