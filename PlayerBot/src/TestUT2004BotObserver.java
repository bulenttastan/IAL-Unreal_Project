import com.google.inject.Inject;
import cz.cuni.amis.pogamut.base.agent.module.LogicModule;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.ConfigurationObserver;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.DisconnectObserver;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.InitializeObserver;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Ping;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.WallCollision;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.UT2004WorldView;
import cz.cuni.amis.pogamut.ut2004.observer.impl.UT2004Observer;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

public class TestUT2004BotObserver extends UT2004Observer {
    
    LogicModule logicModule;
    public String botId;
    public Self myself = null;
    public Player enemy = null;
    static public LinkedList<NavPoint> navPoints = new LinkedList<NavPoint>();
    PrintWriter myFile = null;

    @Inject
    public TestUT2004BotObserver(UT2004AgentParameters params,
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



    public static void setNavPoints(Collection<NavPoint> navs){
        for(NavPoint nav : navs){
            if(TestUT2004BotObserver.navPoints.indexOf(nav) < 0)
                TestUT2004BotObserver.navPoints.add(nav);
        }
    }
    public NavPoint findClosestNavPoint(Location loc, int range){
        NavPoint closestNav = null;
        double min = (range <=0 ? Double.MAX_VALUE : range);
        for(NavPoint nav : navPoints){
            double dist = loc.getDistance(nav.getLocation());
            if(dist < min){
                min = dist;
                closestNav = nav;
            }
        }
        return closestNav;
    }

    public NavPoint findClosestNavPoint(Location loc){
        return findClosestNavPoint(loc, 0);
    }

    public void disconnect(){
        act(new DisconnectObserver());
    }

    private IWorldEventListener<ItemPickedUp> itemPickedUpListener = new IWorldEventListener<ItemPickedUp>() {

        public void notify(ItemPickedUp event) {
            //System.out.println("("+myself.getName()+") ITEM PICKED UP: "+ event.getType());
        }
    };

    private IWorldEventListener<HearNoise> hearNoiseListener = new IWorldEventListener<HearNoise>() {
        
        public void notify(HearNoise event) {
            //System.out.println("HEAR NOISE: " + event.getType());
        }
    };

    private IWorldEventListener<HearPickup> hearPickUpListener = new IWorldEventListener<HearPickup>() {

        public void notify(HearPickup event) {
            //System.out.println("HEAR PICK UP: " + event.getType());
        }
    };

    private IWorldEventListener<WallCollision> wallCollisionListener = new IWorldEventListener<WallCollision>() {
        //DOESN'T WORK
        public void notify(WallCollision event) {
            //System.out.println("WALL COLLISION: " + event.getId());
        }
    };

    private IWorldEventListener<Spawn> spawnListener = new IWorldEventListener<Spawn>() {
        //DOESN'T WORK
        public void notify(Spawn event) {
            //System.out.println("SPAWN");
        }
    };

    private IWorldObjectListener<IncomingProjectile> incomingProjectileListener = new IWorldObjectListener<IncomingProjectile>() {

        public void notify(IWorldObjectEvent<IncomingProjectile> event) {
            IncomingProjectile ip = event.getObject();
            String info = ""+ip.getLocation()+"  "+myself.getLocation();
            //System.out.println("PROJECTILE: "+info);
            //System.out.println(""+ip.getLocation());
        }
    };
    
    private IWorldEventListener<ShootingStarted> shootingStartedListener = new IWorldEventListener<ShootingStarted>() {

        public void notify(ShootingStarted event) {
            //System.out.println("SHOOTING STARTED: "+event.toString());
        }
    };
    
    private IWorldEventListener<ShootingStopped> shootingStoppedListener = new IWorldEventListener<ShootingStopped>() {

        public void notify(ShootingStopped event) {
            //System.out.println("SHOOTING STOPPED: ");
        }
    };

    private IWorldEventListener<PlayerScore> playerScoreListener = new IWorldEventListener<PlayerScore>() {
        //DOESN'T WORK
        public void notify(PlayerScore event) {
            //System.out.println("PLAYER SCORE: "+event.getDeaths());
        }
    };

    private IWorldEventListener<PlayerLeft> playerLeftListener = new IWorldEventListener<PlayerLeft>() {

        public void notify(PlayerLeft event) {
            //System.out.println("PLAYER LEFT: "+event.getName());
        }
    };
    private IWorldEventListener<PlayerKilled> playerKilledListener = new IWorldEventListener<PlayerKilled>() {

        public void notify(PlayerKilled event) {
            //System.out.println("PLAYER KILLED: "+event.getId().getStringId()+" killed by "+event.getKiller().getStringId());
        }
    };

    private IWorldEventListener<PlayerJoinsGame> playerJoinsGameListener = new IWorldEventListener<PlayerJoinsGame>() {

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

        public void notify(JumpPerformed event) {
            isjumping = true;
            //System.out.println("PLAYER JUMPED");
        }
    };
    private IWorldEventListener<Landed> landedListener = new IWorldEventListener<Landed>() {

        public void notify(Landed event) {
            isjumping = false;
            //System.out.println("PLAYER LANDED");
        }
    };

    public void ping(){
        act(new Ping());
    }
    private IWorldEventListener<Pong> pongListener = new IWorldEventListener<Pong>() {

        public void notify(Pong event) {
            System.out.println("!! PONG !!");
        }
    };


    protected boolean botKilledReceived = false;
    private IWorldEventListener<BotKilled> botKilledListener = new IWorldEventListener<BotKilled>() {

        public void notify(BotKilled event) {
            try{
                myFile.println("BOT_KILLED at "+Test.getGameTime());
            } catch(Exception ex){}
            botKilledReceived = true;
        }
    };

    private boolean gameInfoReceived = false;
    private IWorldObjectListener<GameInfo> gameInfoListener = new IWorldObjectListener<GameInfo>() {

        public void notify(IWorldObjectEvent<GameInfo> event) {
            GameInfo gi = (GameInfo) event;
            System.out.println("Game Type: "+gi.getGametype());
            gameInfoReceived = true;
        }
    };


    boolean done = false;
    long prevTime = System.currentTimeMillis();
    int counter = 0;
    protected boolean selfReceived = false;
    private IWorldObjectListener<Self> selfListener = new IWorldObjectListener<Self>() {

        public void notify(IWorldObjectEvent<Self> event) {
            try{
                Self self = event.getObject();

                if(self.getHealth() <= 0)   return;
                if(self.isCrouched())   isjumping = false;

                if(myself == null){
                    myself = self;
                    try{
                        myFile.println("BotName "+myself.getName());
                        myFile.println("Time LocX LocY LocZ Health Shooting "+
                                       "AltShooting EnemyVisibility Jumping Crouching "+
                                       "RotPitch RotYaw Weapon ELocX ELocY ELocZ");
                    } catch(Exception ex){}
                }
                if(enemy == null){
                    for(Player p : getWorldView().getAll(Player.class).values()){
                        if(!p.getName().equals(myself.getName())){
                            enemy = p;
                        }
                    }
                }
                long currentTime = System.currentTimeMillis();
                if(currentTime - prevTime > 1000){
                    prevTime+=1000*(currentTime-prevTime)/1000;
                    System.out.println(self.getName()+" Self is running at " +counter+" fps");
                    counter = 1;
                } else
                    counter++;




                if(myself != null && enemy != null){
                    Location loc = self.getLocation();

                    String info = Test.getGameTime()+" "+loc.x+" "+loc.y+" "+loc.z+
                            " "+self.getHealth()+" "+(self.isShooting()?"1":"0")+
                            " "+(self.isAltFiring()?"1":"0")+" "+(enemy.isVisible()?"1":"0")+
                            " "+(isjumping?"1":"0")+" "+(self.isCrouched()?"1":"0");

                    // SHOOTING TRAINING
                    Rotation rot = self.getRotation();
                    String weaponStr = self.getWeapon().getStringId();
                    weaponStr = weaponStr.substring(weaponStr.lastIndexOf(".")+1);
                    // enemy location is only used for shooting training
                    Location eLoc = enemy.getLocation();

                    info = Test.getGameTime()+" "+loc.x+" "+loc.y+" "+loc.z+
                            " "+self.getHealth()+" "+(self.isShooting()?"1":"0")+
                            " "+(self.isAltFiring()?"1":"0")+" "+(enemy.isVisible()?"1":"0")+
                            " "+(isjumping?"1":"0")+" "+(self.isCrouched()?"1":"0")+
                            " "+(int)rot.pitch+" "+(int)rot.yaw+" "+weaponStr+
                            " "+eLoc.x+" "+eLoc.y+" "+eLoc.z;

                    
                    try{
                        myFile.println(info);
                    } catch(Exception ex){}

                }


            } catch(Exception ex){
                ex.printStackTrace();
            }

            selfReceived = true;
        }
    };


    



    protected boolean navPointReceived = false;
    private IWorldObjectListener<NavPoint> navPointListener = new IWorldObjectListener<NavPoint>() {

        public void notify(IWorldObjectEvent<NavPoint> event) {
            //System.out.println("NAV: "+event.getObject().isVisible());
            navPointReceived = true;
        }
    };



    private boolean itemReceived = false;
    private IWorldObjectListener<Item> itemListener = new IWorldObjectListener<Item>() {

        public void notify(IWorldObjectEvent<Item> event) {
            //System.out.println("ITEM");
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

        public void notify(IWorldObjectEvent<Player> event) {
            Player player = event.getObject();
            //System.out.println("PLAYER : "+player.getName());
            playerReceived = true;
        }
    };



    protected boolean botDamagedReceived = false;
    private IWorldEventListener<BotDamaged> botDamagedListener = new IWorldEventListener<BotDamaged>() {

        public void notify(BotDamaged event) {
            //System.out.println("BOT DAMAGED");
            botDamagedReceived = true;
        }
    };


    protected boolean beginMessageReceived = false;
    private IWorldEventListener<BeginMessage> beginMessageListener = new IWorldEventListener<BeginMessage>() {

        public void notify(BeginMessage event) {
            //System.out.println("BEGIN");
            beginMessageReceived = true;
        }
    };


    protected boolean endMessageReceived = false;
    private IWorldEventListener<EndMessage> endMessageListener = new IWorldEventListener<EndMessage>() {

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

    // call after observer is starting
    public void startObserving(String observedBotId, PrintWriter file) {
        botId = observedBotId;
        myFile = file;
        // initialize observer
        act(new InitializeObserver().setId(observedBotId));
        act(new ConfigurationObserver().setUpdate(0.1).setAll(true).setAsync(true).setGame(true).setSee(true).setSpecial(true).setSelf(true));
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
            throw new PogamutException(sb.toString(), this);
        }

        return true;
    }
}
