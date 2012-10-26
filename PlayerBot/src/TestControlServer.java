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
import cz.cuni.amis.pogamut.base3d.worldview.object.Rotation;
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

/**
 * Control server connected to UT environment. Through this connection we get information
 * for the {@link WorldScene} visualizator.
 *
 * @author Knight
 */
public class TestControlServer extends UT2004Server implements IUT2004Server {

    private double currentUTTime;
    IWorldObjectEventListener myAliveListener = new IWorldObjectEventListener() {

        public void notify(Object event) {
            AliveMessage alive = (AliveMessage) event;
            currentUTTime = alive.getTime();
            System.out.println("ALIVE: " + event.toString());
        }
    };

    
    @Inject
    public TestControlServer(UT2004AgentParameters params, IAgentLogger agentLogger, IComponentBus bus, SocketConnection connection, UT2004WorldView worldView, IAct act) {
        super(params, agentLogger, bus, connection, worldView, act);
    }

    public void initialize() {
        //getWorldView().addObjectListener(AliveMessage.class, WorldObjectUpdatedEvent.class, myAliveListener);
        System.out.println("TestControlServer initialized.");
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
    public static TestControlServer createServer() throws PogamutException {
        UT2004ServerModule module = new TestControlServerModule();
        UT2004ServerFactory factory = new UT2004ServerFactory(module);

        IAgentId agentID = new AgentId("TestControlServer");
        TestControlServer server = (TestControlServer) factory.newAgent(new UT2004AgentParameters().setAgentId(agentID).setWorldAddress(new SocketConnectionAddress("127.0.0.1", 3001)));
        server.start();
        server.initialize();

        return server;
    }




    ////////////////////////////////////////////////////////////////////////////
    //////////////////////// TESTING THE RAY TRACE /////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    Player player = null;
    IWorldEventListener<TraceResponse> traceResponseListener = new IWorldEventListener<TraceResponse>() {
        @Override
        public void notify(TraceResponse event) {
            System.out.println(""+System.currentTimeMillis());
            System.out.println("The response from trace: "+event.isResult());
        }
    };

    public void doRaytracing(){
        for(Player p : getPlayers()){
            if(p.getName().equalsIgnoreCase("bulent")){
                player = p;
                System.out.println("Player "+player.getName()+" is set.");
                break;
            }
        }
        if(player == null){
            System.out.println("Player is not set, connect the player first");
            return;
        }
        getWorldView().addEventListener(TraceResponse.class, traceResponseListener);

        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 100);
        boolean traceActor = false;
        Location loc = player.getLocation();
        Rotation rot = player.getRotation();
        System.out.println(loc);
        System.out.println(rot);

        
        Trace trace = new Trace("mytrace", player.getLocation(), new Location(loc.x+rayLength,loc.y,loc.z), traceActor);
        getAct().act(trace);
        trace.setTo(loc);
        getAct().act(trace);
        System.out.println(""+System.currentTimeMillis());
    }
    ////////////////////////////////////////////////////////////////////////////
    /////////////////// END OF TESTING THE RAY TRACE ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////



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
        TestControlServer server = createServer();
        System.out.println(server.getAgents().size());
        System.out.println(server.getPlayers().size());

        for(Player p : server.getPlayers()){
            System.out.println(p.getName()+" "+p.getId());
        }
        server.doRaytracing();
        //server.kick("");
        try{
        Thread.sleep(500);}catch(Exception ex){}
        server.stop();
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
				bind(IUT2004Server.class).to(TestControlServer.class); //Here we tell guice it should create our custom Control server class
			}

		});
	}
}