
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ObserverFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class Observation implements Runnable {
    
    long secondsToRun = 120;
    static String firstPlayer = "Bulent";
    static long startTimeFrom = 0;


    
    String botName;
    public Observation(String name) {
        botName = name;
    }

    @Override
    public void run() {

        UT2004ObserverFactory observerFactory = new UT2004ObserverFactory(new UT2004BotObserverModule());
        UT2004AgentParameters observerParameters = new UT2004AgentParameters()
                          .setAgentId(new AgentId("observer"))
                          .setWorldAddress(new SocketConnectionAddress("localhost", 3002));

        UT2004BotObserver observer = (UT2004BotObserver) observerFactory.newAgent(observerParameters);

        observer.start();
        try{
            

            observer.startObserving(botName);
            
            System.out.println("Observer Started");

            try{Thread.sleep(secondsToRun*1000);} catch(Exception ex){}

            observer.stopObserving();
            System.out.println("Observer Stopped");
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }



    final static long gameStart = System.currentTimeMillis();
    public static long getGameTime(){
        return System.currentTimeMillis() - gameStart + startTimeFrom;
    }

    public static void showInitMessage(){
        DateFormat formatter = new SimpleDateFormat("hh:mm ss:SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(gameStart);
        System.out.println("Observation started at "+formatter.format(calendar.getTime()));
    }
    public static void main(String[] args){
        showInitMessage();
        new Thread(new Observation(firstPlayer)).start();
    }

}
