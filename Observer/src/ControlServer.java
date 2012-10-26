import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import cz.cuni.amis.pogamut.base.agent.IAgent;
import cz.cuni.amis.pogamut.base.agent.IAgentId;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.command.IAct;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnection;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.base.communication.translator.IWorldMessageTranslator;
import cz.cuni.amis.pogamut.base.communication.worldview.IWorldView;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEventListener;
import cz.cuni.amis.pogamut.base.component.bus.IComponentBus;
import cz.cuni.amis.pogamut.base.component.controller.ComponentDependencies;
import cz.cuni.amis.pogamut.base.server.IWorldServer;
import cz.cuni.amis.pogamut.base.utils.logging.IAgentLogger;
import cz.cuni.amis.pogamut.base3d.worldview.IVisionWorldView;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.AddBot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Kick;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Trace;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AliveMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.TraceResponse;
import cz.cuni.amis.pogamut.ut2004.communication.translator.server.ServerFSM;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.UT2004WorldView;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerFactory;

import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerModule;
import cz.cuni.amis.pogamut.ut2004.server.IUT2004Server;
import cz.cuni.amis.pogamut.ut2004.server.impl.UT2004Server;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Control server connected to UT environment. Through this connection we get information
 * for the {@link WorldScene} visualizator.
 *
 * @author Knight
 */
public class ControlServer extends UT2004Server implements IUT2004Server {

    private double currentUTTime;
    IWorldObjectEventListener myAliveListener = new IWorldObjectEventListener() {

        public void notify(Object event) {
            AliveMessage alive = (AliveMessage) event;
            currentUTTime = alive.getTime();
            System.out.println("ALIVE: " + event.toString());
        }
    };

    
    @Inject
    public ControlServer(UT2004AgentParameters params, IAgentLogger agentLogger, IComponentBus bus, SocketConnection connection, UT2004WorldView worldView, IAct act) {
        super(params, agentLogger, bus, connection, worldView, act);
    }

    

    public void addBot(int skill){
        getAct().act(new AddBot().setSkill(skill));
    }

    public void addBot(int skill, String name){
        getAct().act(new AddBot().setName(name).setSkill(skill));
    }

    public void kick(String id){
        getAct().act(new Kick(UnrealId.get(id)));
    }

    public void playerIds(){
        for(Player p : getPlayers()){
            System.out.println(p.getName()+"\t"+p.getId().getStringId());
        }
    }

    public String[][] playerNameAndIds(){
        String[][] ids = new String[getPlayers().size()][2];
        int i=0;
        for(Player p : getPlayers()){
            ids[i++] = new String[]{p.getName(), p.getId().getStringId()};
        }
        return ids;
    }

    public Player getPlayer(String id){
        for(Player p : getPlayers()){
            if(p.getId().getStringId().equals(id))
                return p;
        }
        return null;
    }

    public void end(){
        stop();
        kill();
    }

    /**
     * Creates control server, connect it to the environment and returns the instance.
     *
     * @return StoryControlServer instance
     * @throws PogamutException
     */
    public static ControlServer createServer() throws PogamutException {
        UT2004ServerModule module = new TestControlServerModule();
        UT2004ServerFactory factory = new UT2004ServerFactory(module);

        IAgentId agentID = new AgentId("TestControlServer");
        ControlServer server = (ControlServer) factory.newAgent(new UT2004AgentParameters().setAgentId(agentID).setWorldAddress(new SocketConnectionAddress("127.0.0.1", 3001)));
        server.start();

        return server;
    }





    ////BULENT - APPRENTICESHIP LEARNING PHI DATA EXTRACTION
    PrintWriter rayFile;
    private void startTraceData(){
        getWorldView().addEventListener(TraceResponse.class, traceResponseListener);
        
        FileWriter rayFileWriter;
        try {
            rayFileWriter = new FileWriter("DM-Awkward3_rays.txt");
            rayFile = new PrintWriter(rayFileWriter);
            //writing the angle information as the first line
            for(int angle=0; angle<360; angle+=10){
                if(angle==350)
                    rayFile.print(angle);
                else
                    rayFile.print(angle+" ");
            }
            rayFile.println();
            rayFile.flush();

            //send rays for every cell
            for(int x=-2048,i=1; x<2048; x+=128,i++){
                for(int y=-2048,j=1; y<2048; y+=128,j++){
                    int index = (i-1)*32 + j;
                    Location from = new Location(x+64, y+64, -462);  //z axis is -462 when the user stands still
                    System.out.println(index+") "+(x+64)+", "+(y+64));
                    sendRays2(index,from);
                }
            }

        } catch (Exception ex) {
            System.err.println("File can't be set up");
        }
    }

    private void sendRays2(long gameTime, Location from){
        final int rayLength = 400;//(int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 1000);
        boolean traceActor = false;

        Trace trace = new Trace();
        trace.setFrom(from);
        trace.setTraceActors(traceActor);


        for(int angle=0; angle<360; angle+=10){

            double cos = Math.cos(Math.toRadians(angle));
            double sin = Math.sin(Math.toRadians(angle));
            Location to = from.add( new Location(rayLength*cos, rayLength*sin) );
            trace.setId(""+gameTime+"_"+angle);
            trace.setTo(to);
            getAct().act(trace);
        }
    }

    IWorldEventListener<TraceResponse> traceResponseListener = new IWorldEventListener<TraceResponse>() {
        @Override
        public void notify(TraceResponse event) {
            Location hitLoc = new Location(event.getHitLocation());
            if(event.isResult()){
                //System.out.println("Ray: " +event.getId()+" dist: "+Math.round(event.getFrom().getDistance(hitLoc)));
                rayFile.println(event.getId()+" 1");
                //rayFile.println(event.getId()+" "+Math.round(event.getFrom().getDistance(hitLoc)));
            } else{
                rayFile.println(event.getId()+" 0");
            }
            rayFile.flush();
        }
    };
    ////end BULENT





    /**
     * This method is called when the server is started either from IDE or from command line.
     * It connects the server to the game.
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
//        UT2004ServerModule module = new StoryControlServerModule();
//        UT2004ServerFactory factory = new UT2004ServerFactory(module);
//
//        IAgentId agentID = new AgentId("PogamutObserver");
//        StoryControlServer server = (StoryControlServer) factory.newAgent(new UT2004AgentParameters().setAgentId(agentID).setWorldAddress(new SocketConnectionAddress("127.0.0.1", 3001)));

//        server.start();
//        server.initialize();
        ControlServer server = createServer();
        System.out.println(server.getAgents().size());
        System.out.println(server.getPlayers().size());

        for(Player p : server.getPlayers()){
            System.out.println(p.getName()+" "+p.getId());
        }
        //server.kick("");
        //server.stop();

        server.startTraceData();
    }
}

class TestControlServerModule extends UT2004ServerModule {

	@Override
	protected void configureModules() {
		super.configureModules();
		addModule(new AbstractModule() {

			@Override
			public void configure() {
				bind(IWorldMessageTranslator.class).to(ServerFSM.class);
				bind(IWorldView.class).to(IVisionWorldView.class);
				bind(IVisionWorldView.class).to(UT2004WorldView.class);
				bind(ComponentDependencies.class).annotatedWith(Names.named(UT2004WorldView.WORLDVIEW_DEPENDENCY)).toProvider(worldViewDependenciesProvider);
				bind(IAgent.class).to(IWorldServer.class);
				bind(IWorldServer.class).to(IUT2004Server.class);
				bind(IUT2004Server.class).to(ControlServer.class); //Here we tell guice it should create our custom Control server class
			}

		});
	}
}