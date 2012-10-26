import com.google.inject.Inject;
import cz.cuni.amis.pogamut.base.agent.module.LogicModule;
import cz.cuni.amis.pogamut.base.agent.state.level1.IAgentStateUp;
import cz.cuni.amis.pogamut.base.communication.command.IAct;
import cz.cuni.amis.pogamut.base.communication.messages.CommandMessage;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectListener;
import cz.cuni.amis.pogamut.base.component.bus.IComponentBus;
import cz.cuni.amis.pogamut.base.utils.logging.IAgentLogger;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Rotation;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.ConfigurationObserver;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.DisconnectObserver;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Dodge;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.InitializeObserver;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Ping;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Trace;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BeginMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.EndMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearNoise;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearPickup;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.JumpPerformed;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Landed;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.MyInventory;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerJoinsGame;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerLeft;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerScore;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Pong;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ShootingStarted;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ShootingStopped;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Spawn;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.TraceResponse;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.WallCollision;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.UT2004WorldView;
import cz.cuni.amis.pogamut.ut2004.observer.impl.UT2004Observer;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public class UT2004BotObserver extends UT2004Observer {
    
    LogicModule logicModule;
    public String myName;
    public Self myself = null;
    public Player enemy = null;
    static public LinkedList<NavPoint> navPoints = new LinkedList<NavPoint>();
    PrintWriter myFile = null;
    PrintWriter rayFile = null;

    @Inject
    public UT2004BotObserver(UT2004AgentParameters params,
            IComponentBus bus, IAgentLogger agentLogger,
            UT2004WorldView worldView, IAct act) {
        super(params, bus, agentLogger, worldView, act);
    }

    public void hookListeners(){
        getWorldView().addObjectListener(GameInfo.class, gameInfoListener);
        getWorldView().addObjectListener(Self.class, selfListener);
        getWorldView().addObjectListener(NavPoint.class, navPointListener);
        getWorldView().addObjectListener(Item.class, itemListener);
        getWorldView().addObjectListener(Player.class, playerListener);
        getWorldView().addObjectListener(IncomingProjectile.class, incomingProjectileListener);
        getWorldView().addEventListener(BotDamaged.class, botDamagedListener);
        getWorldView().addEventListener(BeginMessage.class, beginMessageListener);
        getWorldView().addEventListener(EndMessage.class, endMessageListener);
        getWorldView().addEventListener(BotKilled.class, botKilledListener);
        getWorldView().addEventListener(Pong.class, pongListener);
        getWorldView().addEventListener(JumpPerformed.class, jumpListener);
        getWorldView().addEventListener(Landed.class, landedListener);
        getWorldView().addEventListener(PlayerJoinsGame.class, playerJoinsGameListener);
        getWorldView().addEventListener(PlayerKilled.class, playerKilledListener);
        getWorldView().addEventListener(PlayerLeft.class, playerLeftListener);
        getWorldView().addEventListener(PlayerScore.class, playerScoreListener);
        getWorldView().addEventListener(ShootingStarted.class, shootingStartedListener);
        getWorldView().addEventListener(ShootingStopped.class, shootingStoppedListener);
        getWorldView().addEventListener(Spawn.class, spawnListener);
        getWorldView().addEventListener(WallCollision.class, wallCollisionListener);
        getWorldView().addEventListener(HearNoise.class, hearNoiseListener);
        getWorldView().addEventListener(HearPickup.class, hearPickUpListener);
        getWorldView().addEventListener(ItemPickedUp.class, itemPickedUpListener);
        
//        cz.cuni.amis.pogamut.ut2004.communication.messages.
//        gbinfomessages.
    }



//    public static void setNavPoints(Collection<NavPoint> navs){
//        for(NavPoint nav : navs){
//            if(UT2004BotObserver.navPoints.indexOf(nav) < 0)
//                UT2004BotObserver.navPoints.add(nav);
//        }
//    }
//    public NavPoint findClosestNavPoint(Location loc, int range){
//        NavPoint closestNav = null;
//        double min = (range <=0 ? Double.MAX_VALUE : range);
//        for(NavPoint nav : navPoints){
//            double dist = loc.getDistance(nav.getLocation());
//            if(dist < min){
//                min = dist;
//                closestNav = nav;
//            }
//        }
//        return closestNav;
//    }
//
//    public NavPoint findClosestNavPoint(Location loc){
//        return findClosestNavPoint(loc, 0);
//    }

    public void disconnect(){
        act(new DisconnectObserver());
    }


    int healthPickedUp = 0;
    int weaponPickedUp = 0;
    private IWorldEventListener<ItemPickedUp> itemPickedUpListener = new IWorldEventListener<ItemPickedUp>() {

        @Override
        public void notify(ItemPickedUp event) {
            Category itCatg = event.getType().getCategory();

            //System.out.println("("+myself.getName()+") ITEM PICKED UP: "+ event.getType());
            try{
                Location loc = event.getLocation();
                if(itCatg == ItemType.Category.HEALTH){
                    myFile.println("HEALTH_PICKED at "+Observation.getGameTime()+" "+loc.x+" "+loc.y+" "+loc.z);
                    healthPickedUp = 1;
                }
                if(itCatg == ItemType.Category.WEAPON || itCatg == ItemType.Category.AMMO){
                    myFile.println("WEAPON_PICKED at "+Observation.getGameTime()+" "+loc.x+" "+loc.y+" "+loc.z);
                    weaponPickedUp = 1;
                }
                myFile.flush();
            } catch(Exception ex){}
        }
    };

    private IWorldEventListener<HearNoise> hearNoiseListener = new IWorldEventListener<HearNoise>() {
        
        @Override
        public void notify(HearNoise event) {
            //System.out.println("HEAR NOISE: " + event.getType());
        }
    };

    private IWorldEventListener<HearPickup> hearPickUpListener = new IWorldEventListener<HearPickup>() {

        @Override
        public void notify(HearPickup event) {
            //System.out.println("HEAR PICK UP: " + event.getType());
        }
    };

    private IWorldEventListener<WallCollision> wallCollisionListener = new IWorldEventListener<WallCollision>() {
        //DOESN'T WORK
        @Override
        public void notify(WallCollision event) {
            //System.out.println("WALL COLLISION: " + event.getId());
        }
    };

    private IWorldEventListener<Spawn> spawnListener = new IWorldEventListener<Spawn>() {
        //DOESN'T WORK
        @Override
        public void notify(Spawn event) {
            //System.out.println("SPAWN");
        }
    };

    private IWorldObjectListener<IncomingProjectile> incomingProjectileListener = new IWorldObjectListener<IncomingProjectile>() {

        @Override
        public void notify(IWorldObjectEvent<IncomingProjectile> event) {
            IncomingProjectile ip = event.getObject();
            String info = ""+ip.getLocation()+"  "+myself.getLocation();
            //System.out.println("PROJECTILE: "+info);
            //System.out.println(""+ip.getLocation());
        }
    };
    
    private IWorldEventListener<ShootingStarted> shootingStartedListener = new IWorldEventListener<ShootingStarted>() {

        @Override
        public void notify(ShootingStarted event) {
            //System.out.println("SHOOTING STARTED: "+event.toString());
        }
    };
    
    private IWorldEventListener<ShootingStopped> shootingStoppedListener = new IWorldEventListener<ShootingStopped>() {

        @Override
        public void notify(ShootingStopped event) {
            //System.out.println("SHOOTING STOPPED: ");
        }
    };

    private IWorldEventListener<PlayerScore> playerScoreListener = new IWorldEventListener<PlayerScore>() {
        //DOESN'T WORK
        @Override
        public void notify(PlayerScore event) {
            //System.out.println("PLAYER SCORE: "+event.getDeaths());
        }
    };

    private IWorldEventListener<PlayerLeft> playerLeftListener = new IWorldEventListener<PlayerLeft>() {

        @Override
        public void notify(PlayerLeft event) {
            //System.out.println("PLAYER LEFT: "+event.getName());
        }
    };
    private IWorldEventListener<PlayerKilled> playerKilledListener = new IWorldEventListener<PlayerKilled>() {

        @Override
        public void notify(PlayerKilled event) {
            //System.out.println("PLAYER KILLED: "+event.getId().getStringId()+" killed by "+event.getKiller().getStringId());
        }
    };

    private IWorldEventListener<PlayerJoinsGame> playerJoinsGameListener = new IWorldEventListener<PlayerJoinsGame>() {

        @Override
        public void notify(PlayerJoinsGame event) {
            //System.out.println("PLAYER JOINS: "+event.getName());
        }
    };

    public void getPlayerIds(){
        for(Player p : getWorldView().getAll(Player.class).values())
            System.out.println(p.getName()+"\t"+p.getId().getStringId());
    }

    public void askInventory(){
        for(MyInventory inv : getWorldView().getAll(MyInventory.class).values())
            System.out.println("MY INVENTORY: "+inv.getType().getName()+"  "+inv.getAmount());
    }


    
    boolean isjumping = false;
    private IWorldEventListener<JumpPerformed> jumpListener = new IWorldEventListener<JumpPerformed>() {

        @Override
        public void notify(JumpPerformed event) {
            isjumping = true;
            //System.out.println("PLAYER JUMPED");
        }
    };
    private IWorldEventListener<Landed> landedListener = new IWorldEventListener<Landed>() {

        @Override
        public void notify(Landed event) {
            isjumping = false;
            //System.out.println("PLAYER LANDED");
        }
    };

    public void ping(){
        act(new Ping());
    }
    private IWorldEventListener<Pong> pongListener = new IWorldEventListener<Pong>() {

        @Override
        public void notify(Pong event) {
            System.out.println("!! PONG !!");
        }
    };


    protected boolean botKilledReceived = false;
    private IWorldEventListener<BotKilled> botKilledListener = new IWorldEventListener<BotKilled>() {

        @Override
        public void notify(BotKilled event) {
            try{
                myFile.println("BOT_KILLED at "+Observation.getGameTime());
                myFile.flush();
            } catch(Exception ex){}
            botKilledReceived = true;
        }
    };

    private boolean gameInfoReceived = false;
    private IWorldObjectListener<GameInfo> gameInfoListener = new IWorldObjectListener<GameInfo>() {

        @Override
        public void notify(IWorldObjectEvent<GameInfo> event) {
            GameInfo gi = (GameInfo) event;
            System.out.println("Game Type: "+gi.getGametype());
            gameInfoReceived = true;
        }
    };



    IWorldEventListener<TraceResponse> traceResponseListener = new IWorldEventListener<TraceResponse>() {
        @Override
        public void notify(TraceResponse event) {
            Location hitLoc = new Location(event.getHitLocation());
            //System.out.println("Ray: " +event.getId()+" dist: "+Math.round(event.getFrom().getDistance(hitLoc)));
            rayFile.println(event.getId()+" "+Math.round(event.getFrom().getDistance(hitLoc)));
            rayFile.flush();
        }
    };


    public static final double UT_ANGLE_TO_DEG =  65535.0 / 360;
    public static double unrealDegreeToDegree(double unrealDegrees) {
        return unrealDegrees / UT_ANGLE_TO_DEG;
    }
    public void sendRays(long gameTime){
        
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 1000);
        boolean traceActor = false;
        Location from = myself.getLocation();
        //yaw is the angle on XY plane (top view)
        double yaw = myself.getRotation().yaw;
        int maxvalue = (Short.MAX_VALUE+1)*2-1; //it's nothing but 65535
        
        Trace trace = new Trace();
        trace.setFrom(from);
        trace.setTraceActors(traceActor);


        //generate rays, when i=0 the ray is in the front of the player.
        int min=-5, max=5, angleIncrement=30;
        for(int i=min; i<=max; i++){
            double angleInDegrees = 360*yaw/maxvalue + (i*angleIncrement);
            double angleInRadians = 2*Math.PI*yaw/maxvalue + (i*Math.PI/(180/angleIncrement));
            double cos = Math.cos(angleInRadians);
            double sin = Math.sin(angleInRadians);

            //for cases that we want to trace different depth, we increse maxz
            //the cases are -120,-30,30,120 degrees considering forward is 0 degrees
            int maxz=0;
            if(i==-4 || i==-1 || i==1 || i==4){
                maxz = 2;
            }

            for(int z=0; z<=maxz; z++){
                double sinz = Math.sin(-z*(angleIncrement/180.0)*Math.PI);
                //the extra line vector that holds the length and the direction of the ray
                Location tail = new Location(rayLength*cos, rayLength*sin, rayLength*sinz);

                Location to = from.add(tail);

                //the id mechanism is timestamp and the negative values for the rays on
                // the left and positive values for the rays on the right
                String id = ""+gameTime+"_"+i+"_"+z;
                trace.setId(id);
                trace.setTo(to);

                server.getAct().act(trace);

            }
        }
    }

    private void sendRays2(long gameTime){
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 1000);
        boolean traceActor = false;
        Location from = myself.getLocation();
        //yaw is the angle on XY plane (top view)
        double yaw = myself.getRotation().yaw;

        Trace trace = new Trace();
        trace.setFrom(from);
        trace.setTraceActors(traceActor);


        for(int angle=0; angle<360; angle+=10){

            double cos = Math.cos(Math.toRadians(angle));
            double sin = Math.sin(Math.toRadians(angle));
            Location to = from.add( new Location(rayLength*cos, rayLength*sin) );
            trace.setId(""+gameTime+"_"+angle);
            trace.setTo(to);
            server.getAct().act(trace);
        }
    }
    



    boolean done = false;
    long prevTime = System.currentTimeMillis();
    int counter = 0;
    protected boolean selfReceived = false;
    private IWorldObjectListener<Self> selfListener = new IWorldObjectListener<Self>() {

        @Override
        public void notify(IWorldObjectEvent<Self> event) {
            try{
                Self self = event.getObject();

                if(self.getHealth() <= 0)   return;
                if(self.isCrouched())   isjumping = false;

                if(myself == null){
                    myself = self;
                    try{
                        myFile.println("BotName "+myself.getName()+" Map "+server.getMapName());
                        myFile.println("Time LocX LocY LocZ Health Shoot "+
                                       "AltSh EnVis Jump Crouch RotPitch RotYaw "+
                                       "Weapon ELocX ELocY ELocZ "+
                                       "HVic WVic HTaken WTaken EName");
                        myFile.flush();

                        //writing the angle information as the first line
                        for(int angle=0; angle<360; angle+=10){
                            if(angle==350)
                                rayFile.print(angle);
                            else
                                rayFile.print(angle+" ");
                        }
                        rayFile.println();
                        rayFile.flush();
                    } catch(Exception ex){}
                }

                
                long currentTime = System.currentTimeMillis();
                if(currentTime - prevTime > 1000){
                    prevTime+=1000*((currentTime-prevTime)/1000);
                    System.out.println(self.getName()+" Self is running at " +counter+" fps");
                    counter = 1;
                } else
                    counter++;


                double min = Double.POSITIVE_INFINITY;
                for(Player p : getWorldView().getAllVisible(Player.class).values())
                {
                    if(!p.getName().equals(myself.getName()))
                    {
                        double dist = p.getLocation().getDistance(self.getLocation());
                        if(dist < min)
                            enemy = p;
                    }
                }


                
                Location loc = self.getLocation();

                // SHOOTING TRAINING
                Rotation rot = self.getRotation();
                String weaponStr = self.getWeapon().getStringId();
                weaponStr = weaponStr.substring(weaponStr.lastIndexOf(".")+1);
                // enemy location is only used for shooting training
                Location eLoc = new Location();
                if(enemy != null)   eLoc = enemy.getLocation();

                int healthVicinity = 0;
                int weaponVicinity = 0;
                int vicinityRange = 800;
                for(Item item : getWorldView().getAllVisible(Item.class).values()){
                    if(item.getLocation().getDistance2D(loc) < vicinityRange){
                        if(item.getType().getCategory()==ItemType.Category.HEALTH){
                            //System.out.println("Health at "+item.getType().getCategory()+" "+item.getLocation());
                            healthVicinity = 1;
                        }
                        if(item.getType().getCategory()==ItemType.Category.WEAPON || item.getType().getCategory()==ItemType.Category.AMMO){
                            //System.out.println("Weapon at "+item.getType().getCategory()+" "+item.getLocation());
                            weaponVicinity = 1;
                        }
                    }
                }


                long gameTime = Observation.getGameTime();

                ///BULENT - for botprize competition
//                String message = (isjumping?"JUMP ":"") + loc.x+","+loc.y+","+loc.z+" ";
//                try{
//                    myFile.print(message);
//                    myFile.flush();
//                } catch(Exception ex){ex.printStackTrace();}
                ///end BULENT

                
                sendRays2(gameTime);

                String info = gameTime+" "+loc.x+" "+loc.y+" "+loc.z+
                        " "+self.getHealth()+" "+(self.isShooting()?"1":"0")+
                        " "+(self.isAltFiring()?"1":"0")+" "+(enemy!=null && enemy.isVisible()?"1":"0")+
                        " "+(isjumping?"1":"0")+" "+(self.isCrouched()?"1":"0")+
                        " "+(int)rot.pitch+" "+(int)rot.yaw+" "+weaponStr+
                        " "+eLoc.x+" "+eLoc.y+" "+eLoc.z+" "+healthVicinity+
                        " "+weaponVicinity+" "+healthPickedUp+" "+weaponPickedUp+
                        " "+(enemy!=null?enemy.getName():"NoName");


                healthPickedUp = 0;
                weaponPickedUp = 0;


                try{
                    myFile.println(info);
                    myFile.flush();
                } catch(Exception ex){}

                


            } catch(Exception ex){
                ex.printStackTrace();
            }

            selfReceived = true;
        }
    };


    



    protected boolean navPointReceived = false;
    private IWorldObjectListener<NavPoint> navPointListener = new IWorldObjectListener<NavPoint>() {

        @Override
        public void notify(IWorldObjectEvent<NavPoint> event) {
            //System.out.println("NAV: "+event.getObject().isVisible());
            navPointReceived = true;
        }
    };



    private boolean itemReceived = false;
    private IWorldObjectListener<Item> itemListener = new IWorldObjectListener<Item>() {

        @Override
        public void notify(IWorldObjectEvent<Item> event) {
            itemReceived = true;
        }
    };



    ////////////////////////////////////
    // this is used only for EmptyBot //
    public boolean canSeeOpponent(){
        if(enemy != null)
            return enemy.isVisible();
        return false;
    }
    ////////////////////////////////////
    protected boolean playerReceived = false;
    private IWorldObjectListener<Player> playerListener = new IWorldObjectListener<Player>() {

        @Override
        public void notify(IWorldObjectEvent<Player> event) {
            Player player = event.getObject();
            //System.out.println("PLAYER : "+player.getName());
            playerReceived = true;
        }
    };



    protected boolean botDamagedReceived = false;
    private IWorldEventListener<BotDamaged> botDamagedListener = new IWorldEventListener<BotDamaged>() {

        @Override
        public void notify(BotDamaged event) {
            //System.out.println("BOT DAMAGED");
            botDamagedReceived = true;
        }
    };


    protected boolean beginMessageReceived = false;
    private IWorldEventListener<BeginMessage> beginMessageListener = new IWorldEventListener<BeginMessage>() {

        @Override
        public void notify(BeginMessage event) {
            //System.out.println("BEGIN");
            beginMessageReceived = true;
        }
    };


    protected boolean endMessageReceived = false;
    private IWorldEventListener<EndMessage> endMessageListener = new IWorldEventListener<EndMessage>() {

        @Override
        public void notify(EndMessage event) {
            //System.out.println("END");
            endMessageReceived = true;
        }
    };

    /**
     * Sends custom command to the GameBots.
     *
     * Shortcut for getAct().act(command).
     *
     * @param command
     */
    public void act(CommandMessage command) {
        getAct().act(command);
    }







    
    int freq = 4;
    // call after observer is starting
    public void startObserving(String observedBotName) throws Exception {
        myName = observedBotName;
        setupFile();
        String myId = setupServer();
        // initialize observer
        act(new InitializeObserver().setId(myId));
        act(new ConfigurationObserver().setUpdate(1.0/freq).setAll(true).setAsync(true).setGame(true).setSee(true).setSpecial(true).setSelf(true));
        
        hookListeners();
    }


    public void stopObserving(){
        stop();
        if(inState(IAgentStateUp.class)){
            kill();
            System.out.println("Observer Killed because it was in Up State");
        }
        //give half a second to collect all ray responses then stop server
        try{ Thread.sleep(200); }catch(Exception ex){ex.printStackTrace();}
        server.stop();
        myFile.close();
        rayFile.close();
    }
    

    private void setupFile(){
        FileWriter myFileWriter;
        FileWriter rayFileWriter;
        try {
            myFileWriter = new FileWriter(myName + "_" + Observation.gameStart + ".txt");
            rayFileWriter = new FileWriter(myName + "_"+Observation.gameStart+"_rays.txt");
            myFile = new PrintWriter(myFileWriter);
            rayFile = new PrintWriter(rayFileWriter);
        } catch (IOException ex) {
            System.err.println("File can't be set up");
        }
    }


    ControlServer server;
    private String setupServer() throws Exception{
        String myId = null;

        server = ControlServer.createServer();
        server.getWorldView().addEventListener(TraceResponse.class, traceResponseListener);

        String[][] ids = server.playerNameAndIds();

        for(int i=0; i<ids.length; i++){
            System.out.println(ids[i][1]);
            if(ids[i][0].equals(myName)){
                myId = ids[i][1];
                System.out.println("My Bot: "+myId);
            }
            else
                System.out.println("Other Player: "+ids[i][0]+" "+ids[i][1]);
        }

        if(myId == null){
            stopObserving();
            throw new Exception("Player "+myName+" is not connected to the game.");
        }

        return myId;
    }


//// after some time, call
    /**
     * This method will check which messages we have received from observed bot...
     */
    public boolean testMessages() {
        StringBuilder sb = new StringBuilder();
        sb.append("ERRORS ");


        //////////
        System.out.println("G: "+gameInfoReceived);
        System.out.println("S: "+selfReceived);
        System.out.println("N: "+navPointReceived);
        System.out.println("I: "+itemReceived);
        System.out.println("P: "+playerReceived);
        System.out.println("B: "+botDamagedReceived);
        System.out.println("K: "+botKilledReceived);
        System.out.println("B: "+beginMessageReceived);
        System.out.println("E: "+endMessageReceived);
        //////////



//        if (!gameInfoReceived) {
//            sb.append("| GAME INFO NOT RECEIVED ");
//        }
        if (!selfReceived) {
            sb.append("| SELF NOT RECEIVED ");
        }
        if (!navPointReceived) {
            sb.append("| NAVPOINT NOT RECEIVED ");
        }
        if (!itemReceived) {
            sb.append("| ITEMS NOT RECEIVED ");
        }
        if (!playerReceived) {
            sb.append("| PLAYER NOT RECEIVED ");
        }
        if (!botDamagedReceived) {
            sb.append("| BOT DAMAGED NOT RECEIVED ");
        }
        if (!botKilledReceived) {
            sb.append("| BOT KILLED NOT RECEIVED ");
        }
        if (!beginMessageReceived) {
            sb.append("| BEGIN MESSAGE NOT RECEIVED ");
        }
        if (!endMessageReceived) {
            sb.append("| END MESSAGE NOT RECEIVED ");
        }

        if (!sb.toString().equals("ERRORS ")) {
            System.out.println("!!!!! " + sb.toString());
            //throw new PogamutException(sb.toString(), this);
        }

        return true;
    }
}
