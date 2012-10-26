
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.base.communication.worldview.IWorldView;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionHistoryStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.analyzer.IUT2004Analyzer;
import cz.cuni.amis.pogamut.ut2004.analyzer.UT2004AnalyzerModule;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Bumped;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.map.UT2004Map;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.map.Waylink;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004AnalyzerFactory;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004AnalyzerRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.*;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEvent;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearNoise;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ObserverFactory;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;



/**
 * Pogamut's "Hello world!" example showing few extra things such as introspection
 * and various bot-initializing methods.
 *
 * @author Michal Bida aka Knight
 * @author Rudolf Kadlec aka ik
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
public class EmptyBot extends UT2004BotModuleController {


    @JProp
    static public String botName = "Vortex";


    
    J2DFrame frame;
    /**
     * Initialize all necessary variables here, before the bot actually receives anything
     * from the environment.
     */
    @Override
    public void prepareBot(UT2004Bot bot) {}

    
    
    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        String[] skins = new String[]{"HumanMaleA.MercMaleA","HumanMaleA.MercMaleB",
                                      "HumanMaleA.MercMaleC","HumanMaleA.MercMaleD",
        //"HumanMaleA.EgyptMaleA","HumanMaleA.EgyptMaleB","HumanMaleA.SkeletonMale",
        "HumanFemaleA.MercFemaleA","HumanFemaleA.MercFemaleB","HumanFemaleA.MercFemaleC"};
        int randomSkinIndex =(int)Math.floor(skins.length*Math.random());
        return new Initialize().setName(botName).setSkin(skins[randomSkinIndex]);
    }



    /**
     * Handshake with GameBots2004 is over - bot has information about the map in its world view.
     * Many agent modules are usable since this method is called.
     * @param gameInfo information about the game type
     * @param config information about configuration
     * @param init information about configuration
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        frame = new J2DFrame();
        frame.showArrows = true;

        prepareNavList();

        if(myattackmode)
            setup();

        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3.0));
        pathExecutor.addStuckDetector(new UT2004PositionHistoryStuckDetector(bot));
        pathExecutor.getState().addStrongListener(new FlagListener<IPathExecutorState>() {
            @Override
            public void flagChanged(IPathExecutorState changedValue) {
                switch (changedValue.getState()) {
                    case PATH_COMPUTED:
                        break;
                    case PATH_COMPUTATION_FAILED:
                        // if path computation fails to whatever reason, just try another navpoint
                        System.out.println("PATH COMPUTATION FAILED");
                        break;
                    case TARGET_REACHED:
                        // most of the time the execution will go this way
                        if(myState == MyState.EXPLORE){
                            getHealthPacks();
                            checkItemsInSight();
                        }
                        break;

                    case STUCK:
                        // the bot has stuck! ... target nav point is unavailable currently
                        if(myattackmode)
                            stuckReset();
                        break;
                }
            }
        });
        
    }

    /**
     * The bot is initialized in the environment - a physical representation of the bot is present in the game.
     * @param gameInfo information about the game type
     * @param config information about configuration
     * @param init information about configuration
     * @param self information about the agent
     */
    @Override
    public void botSpawned(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
        myState = MyState.EXPLORE;
    }



    public static final double MAX_DISTANCE = 500;
    public boolean canSee(ILocated object) {
        if (info.getLocation() != null && info.getLocation().getDistance(object.getLocation()) < MAX_DISTANCE)
            return true;
        return false;
    }

    private void changePolicy(){
        Location curLoc = bot.getLocation();
        int[] ind = frame.getGridIndex(curLoc);
        int i=ind[0], j=ind[1];
        int min = Integer.MAX_VALUE;
        int minstart = 0;
        
        for(int s=0; s<start.length; s++){
            int distance = Math.abs(start[s][0]-i) + Math.abs(start[s][1]-j);
            if(distance < min){
                min = distance;
                minstart = s;
            }
        }

        //70% choose minimum, 30% choose randomly
        if(Math.random() < 0.7)
            curpolicy = minstart;
        else
            curpolicy = (int) Math.floor(Math.random()*start.length);
        
        policy = policies[curpolicy];

        frame.repaint();
        System.out.println("Current Policy: "+curpolicy);
    }

    private void getHealthPacks(){
        double min = Double.MAX_VALUE;
        NavPoint closest = null;
        for(Item myitem : bot.getWorldView().getAllVisible(Item.class).values()){
            if(myitem.getType() == ItemType.MINI_HEALTH_PACK){
                double distance = myitem.getLocation().getDistance(bot.getLocation());
                if(distance < min){
                    min = distance;
                    closest = myitem.getNavPoint();
                }
            }
        }
        if(closest != null && !pathExecutor.isExecuting()){
            //System.out.println("Going to health pack");
            IPathFuture<ILocated> pathHandle = (IPathFuture<ILocated>)pathPlanner.computePath(bot, closest);
            pathExecutor.followPath(pathHandle);
        }
    }


    private void checkItemsInSight(){
        double min = Double.MAX_VALUE;
        NavPoint closest = null;
        for(Item myitem : bot.getWorldView().getAllVisible(Item.class).values()){
            if(myitem.getType().getCategory()==ItemType.Category.WEAPON ||
                    (ItemType.Category.AMMO==myitem.getType().getCategory() && !weaponry.isLoaded(myitem.getType())) ||
                    (myitem.getType()==ItemType.HEALTH_PACK && info.getHealth() < 90) ||
                    myitem.getType() == ItemType.MINI_HEALTH_PACK){
                double distance = myitem.getLocation().getDistance(bot.getLocation());
                if(distance < min){
                    min = distance;
                    closest = myitem.getNavPoint();
                }
            }
        }
        if(closest != null && !pathExecutor.isExecuting()){
            //System.out.println("Going to health pack");
            IPathFuture<ILocated> pathHandle = (IPathFuture<ILocated>)pathPlanner.computePath(bot, closest);
            pathExecutor.followPath(pathHandle);
        }
    }

    private void arepoliciesequal(){
        int pn=2;
        for(int p1=0; p1<4; p1++){
            for(int p2=p1+1; p2<5; p2++){
                boolean equal = true;
                int[][] pol1 = policies[p1];
                int[][] pol2 = policies[p2];
                for(int i=0; i<pol1.length; i++){
                    if(!Arrays.equals(pol1[i], pol2[i])){
                        equal = false;
                        break;
                    }
                }

                System.out.println(""+p1+" and "+p2+" are equal: "+equal);
            }
        }
    }


    @JProp
    int healthItemCount = 0;
    @EventListener(eventClass=ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event){
        if(event.getType() == ItemType.MINI_HEALTH_PACK){
            healthItemCount++;
        }
    }

    
    @EventListener(eventClass = Bumped.class)
    protected void bumped(Bumped event){}



    /////////////////////////////////
    //// MY ATTACK MODE FUNCTIONS ///
    /////////////////////////////////
    protected Player enemy = null;
    public int frags = 0;
    public int enemyFrags = 0;

    
    @EventListener(eventClass=PlayerKilled.class)
    public void playerKilled(PlayerKilled event){
        if (event.getKiller().equals(info.getId())) {
            ++frags;
        } else {
            ++enemyFrags;
        }
    }
    
    long lastIncomingProjectile = 0;
    @ObjectClassListener(objectClass=IncomingProjectile.class)
    public void incomingProjectile(IWorldObjectEvent<IncomingProjectile> object){
        IncomingProjectile ip =  object.getObject();
        lastIncomingProjectile = System.currentTimeMillis();
        lastImpact = lastIncomingProjectile;
//        System.out.println("Incoming Projectile: "+);
    }

    long lastNoiseHeard = 0;
    @EventListener(eventClass=HearNoise.class)
    public void hearNoise(HearNoise event){
        //System.out.println("NOISE: "+event.getRotation());
        //if(event.getType().startsWith("XWeapons")){
        lastNoiseHeard = System.currentTimeMillis();
        lastImpact = lastNoiseHeard;
        //}
    }

    long lastBotDamaged = 0;
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event){
        //System.out.println("Bot Damaged: "+event.toString());
        lastBotDamaged = System.currentTimeMillis();
        lastImpact = lastBotDamaged;
    }

    

    private void stuckReset(){
        pathExecutor.stop();
    }

    String opponentId = null;
    boolean opponentIsUTbot = false;
    Player enemyFromServer;
    TestUT2004BotObserver observer;
    private void setup(){
        TestControlServer server = TestControlServer.createServer();
        String[][] ids = server.playerNameAndIds();
        for(int i=0; i<ids.length; i++){
            System.out.println(ids[i][0]+": "+ids[i][1]);
            //logical statement below should select only GBxBot if exists, or my player
            if(!ids[i][0].equals(bot.getName()) && (ids.length==2 || !ids[i][0].equals("Bulent")))
                opponentId = ids[i][1];
        }
        System.out.println("OBSERVING "+opponentId);

        if(opponentId.contains("GBxBot")){
            opponentIsUTbot = true;
            enemyFromServer = server.getPlayer(opponentId);
            while(enemyFromServer == null){
                try{Thread.sleep(50);}catch(Exception ex){}
            }
        } else{
            //setup observer for only player bot
            UT2004ObserverFactory observerFactory = new UT2004ObserverFactory(new TestUT2004BotObserverModule());
            UT2004AgentParameters observerParameters = new UT2004AgentParameters()
                              .setAgentId(new AgentId("observer"))
                              .setWorldAddress(new SocketConnectionAddress("localhost", 3002));
            observer = (TestUT2004BotObserver) observerFactory.newAgent(observerParameters);
            observer.start();
            try{
                observer.startObserving(opponentId, null);
                observer.hookListeners();
            } catch(Exception ex) {
                ex.printStackTrace();
                observer.stop();
                observer.kill();
            }
            //make sure observer gets self info
            while(observer.myself == null){
                //System.out.println("WAITING OBSERVER...");
                try{Thread.sleep(50);}catch(Exception ex){}
            }
        }
        server.end();
        System.out.println("Setup Complete");
    }
    private void checkEnemy(){
        //make sure enemy is set
        while(enemy == null){
            for(Player p : world.getAll(Player.class).values()){
                if(!p.getName().equals(bot.getName())){
                    enemy = p;
                    break;
                }
            }
            //System.out.println("WAITING ENEMY...");
        }
    }


    long lingerTime = 1000;
    long lastImpact = 0;
    private boolean canSeeEachOther(){
        checkEnemy();
        if(opponentIsUTbot){
            //add other indicators, like if bot got projectile, turn that direction
            if(lastImpact+lingerTime > System.currentTimeMillis()){
                //System.out.println("ENEMY VISIBLE");
                return true;
            }
            return enemy.isVisible();
        } else{
            //System.out.println("Vis: "+enemy+ "   "+observer.canSeeOpponent());
            return enemy.isVisible() || observer.canSeeOpponent();
        }
    }

    private Location getEnemyLocation(){
        if(opponentIsUTbot){ //needs care
            return enemyFromServer.getLocation();
        } else{
            return observer.myself.getLocation();
        }
    }

    private void checkWeapon(){
        // changing weapon
        if(weaponry.getCurrentPrimaryAmmo() == 0 && weaponry.hasLoadedWeapon()){
            Weapon weapon = weaponry.getLoadedWeapons().values().iterator().next();
            //System.err.println("Ammo finished, changing weapon");
            weaponry.changeWeapon(weapon);
        } else {
            Weapon currentWeapon = weaponry.getCurrentWeapon();
            ItemType switchWeapon = null;
            if(weaponry.hasWeapon(ItemType.Group.FLAK_CANNON) && weaponry.isLoaded(ItemType.Group.FLAK_CANNON))
                switchWeapon = ItemType.FLAK_CANNON;
            else if(weaponry.hasWeapon(ItemType.Group.MINIGUN) && weaponry.isLoaded(ItemType.Group.MINIGUN))
                switchWeapon = ItemType.MINIGUN;
            else if(weaponry.hasWeapon(ItemType.Group.LINK_GUN) && weaponry.isLoaded(ItemType.Group.LINK_GUN))
                switchWeapon = ItemType.LINK_GUN;
            else if(weaponry.hasWeapon(ItemType.Group.SHOCK_RIFLE) && weaponry.isLoaded(ItemType.Group.SHOCK_RIFLE))
                switchWeapon = ItemType.SHOCK_RIFLE;
            else if(weaponry.hasWeapon(ItemType.Group.ASSAULT_RIFLE) && weaponry.isLoaded(ItemType.Group.ASSAULT_RIFLE))
                switchWeapon = ItemType.ASSAULT_RIFLE;
            
            if(switchWeapon != null &&  switchWeapon != currentWeapon.getType()){
                //System.out.println("Switching to a better weapon: "+switchWeapon);
                weaponry.changeWeapon(switchWeapon);
            }
        }
    }

    private double[] getShootingProperties(double dist){
        return null;
    }

    private void performAttack() throws Exception{
        // Shooting direction calculation
        int[] distboundary = new int[]{400,700};
        Location enemyLoc = getEnemyLocation();
        Location botLoc = bot.getLocation();
        double dist = botLoc.getDistance2D(enemyLoc);
        double m=0, s=0; //mu, sigma in radians

        if(dist < distboundary[0]){
            m = -0.007514408064394;
            s = 0.164121018774717;
        } else if(dist < distboundary[1]){
            m = 0.009115581583839;
            s = 0.094721876681183;
        } else {
            m = 0.000858899664894;
            s = 0.066811159195360;
        }

        double phi = random.nextGaussian() * s + m;
        double length = dist * Math.tan(phi);
        double slope = -(enemyLoc.x - botLoc.x) / (enemyLoc.y - botLoc.y);
        double theta = Math.atan(slope);
        double x = length * Math.cos(theta);
        double y = length * Math.sin(theta);
        double z = 0;
        double targetX = enemyLoc.x - Math.signum(phi)*x;
        double targetY = enemyLoc.y - Math.signum(phi)*y;
        double targetZ = enemyLoc.z - Math.signum(phi)*z;

        Location target = new Location(targetX, targetY, targetZ);

        //System.out.println("ANGLE: " + phi*180/Math.PI);
        shoot.shootPrimary(target);
    }

    private void performMove() throws Exception{
        // if you're far get close, then make circle
        Location enemyLoc = getEnemyLocation();
        double dist = bot.getLocation().getDistance2D(enemyLoc);
        
        //System.out.println("Moving  (dist: "+dist+")");
        if(dist > 800 && !pathExecutor.isExecuting()){
            if(!pathExecutor.isExecuting())
                pathExecutor.followPath(pathPlanner.computePath(bot,
                        findClosestNavPointDistanceApart(enemyLoc, 300)));
            //System.out.print("   :  Planning path");
        } else{
            // DO THE CIRCULAR MOVE
            Location botLoc = bot.getLocation();
            //i want 310 distance from enemy to be center of circle to move around
            //and the diameter of circle is 300, so the closest point is 160 away
            //from the enemy
            double percent = 310/dist;
            
            Location interLoc = Location.interpolate(enemyLoc, botLoc, percent);
            NavPoint nav = findClosestNavPoint(interLoc);
            //System.out.println(inter);

            //cutrange is size of grid cell in actual UT map unit
            double radius = cutrange/1.2;
            double speed = 1.2;
            double speedScale = (0.001*2*Math.PI)/speed;
            double angle = System.currentTimeMillis() * speedScale;
            double x = nav.getLocation().x + Math.sin(angle)*radius;
            double y = nav.getLocation().y + Math.cos(angle)*radius;
            Location loc = new Location(x, y);

            // 17.22% jumping from Matlab measurements
            if(random.nextDouble() < .1722)
                move.jump();
            
            move.moveTo(loc);
            move.turnTo(enemy);

            // Show the circle on the frame
            //frame.drawBubble(loc);
            Graphics2D g2 = (Graphics2D) frame.getGraphics();
            //scale loc from nav point, and radius for the gui map
            loc = frame.scale(nav.getLocation());
            radius = radius / frame.scaledown;
            g2.drawOval((int)(loc.x-radius), 
                        (int)(loc.y-radius),
                        (int)(radius*2), (int)(radius*2));

        }
    }


    private MyState myState = MyState.EXPLORE;
    private enum MyState{
        ATTACK, EXPLORE
    }

    // returns the closest navpoint between the bot and location by keeping
    // a certain distance apart from the location
    private NavPoint findClosestNavPointDistanceApart(Location loc, int apart){
        NavPoint closestNav = null;
        double min = Double.MAX_VALUE;
        for(NavPoint nav : navlist){
            double dist = loc.getDistance(nav.getLocation());
            if(dist < min && dist >= apart){
                min = dist;
                closestNav = nav;
            }
        }
        return closestNav;
    }
    /////////////////////////////////////
    // END of MY ATTACK MODE FUNCTIONS //
    /////////////////////////////////////

    boolean rungrid = true;
    boolean myattackmode = true;
    boolean once = false;
    /**
     * Main method that controls the bot - makes decisions what to do next.
     * It is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in GameBots ini file in
     * UT2004/System folder.
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() throws PogamutException {
        Location curLoc = bot.getLocation();
        int[] ind = frame.getGridIndex(curLoc);
        int i=ind[0], j=ind[1];
        frame.setPoint(curLoc);
        frame.drawString("Player at "+i+","+j+"  H: "+healthItemCount+"  Frags: "+
                frags+" vs "+enemyFrags+" ("+myState.name()+")");

        

        if(myattackmode){
            try{
                if(canSeeEachOther()){
                    myState = MyState.ATTACK;
                    int state = 0;
                    if(info.getHealth() < 30) state = 1;
                    else if(info.getHealth() < 60) state = 4;
                    else if(info.getHealth() < 90) state = 7;
                    else state = 10;

                    double distance = curLoc.getDistance2D(getEnemyLocation());
                    if(distance < 500){}
                    else if(distance < 1000) state++;
                    else state += 2;

                    //System.out.println("State: "+state);

                    // PERFORM THE ACTIONS
                    // attackPolicy[0] is set to zero, since state0 doesn't exist
                    int[] attackPolicy = new int[]{0,1,1,1,2,3,3,1,1,3,1,3,3};
                    int action = attackPolicy[state];

                    checkWeapon();
                    if(action == 1){
                        performMove();
                        performAttack();
                    }
                    if(action == 2){
                        move.stopMovement();
                        performAttack();
                    }
                    if (action == 3){
                        shoot.stopShooting();
                        performMove();
                    }
                    if(action == 4){
                        shoot.stopShooting();
                        move.stopMovement();
                    }

                } else {
                    myState = MyState.EXPLORE;
                    shoot.stopShooting();
                }
            } catch(Exception ex){
                System.out.println("... EXCEPTION ...");
            }
            
        }



        if(rungrid && !pathExecutor.isExecuting() && myState==MyState.EXPLORE){
            int dir = policy[i][j];
            if(Math.random() < 0)
                dir = (int)Math.floor(Math.random()*4);
            int ni=i,nj=j;
            if(dir==1)  nj--;
            else if(dir==2) ni--;
            else if(dir==3) nj++;
            else if(dir==4) ni++;
            //somebody pushed bot outside of policy grid
            //put him back to grid
            else{
                if(policy[ni][nj-1] != 0)   nj--;
                else if(policy[ni-1][nj] != 0)  ni--;
                else if(policy[ni][nj+1] != 0)  nj++;
                else ni++;
            }
            Location next = frame.getMapLocation(ni, nj);
            dir = policy[ni][nj];
            if(dir==1)  nj--;
            else if(dir==2) ni--;
            else if(dir==3) nj++;
            else if(dir==4) ni++;
            Location nextnext = frame.getMapLocation(ni, nj);

            move.moveAlong(next,nextnext);
            //getAct().act(new Move().setFirstLocation(frame.getMapLocation(ni, nj)));
            int tolerance = 6;
            int distance = Math.abs(end[curpolicy][0]-i) + Math.abs(end[curpolicy][1]-j);
            //System.out.println(distance);
            if(distance < tolerance){
                // get all the healths if they exist
                getHealthPacks();

                changePolicy();
            }

            checkItemsInSight();
        }


    }

    
    /**
     * Called each time the bot dies. Good for reseting all bot's state dependent variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        this.bot.start();
    }


    class J2DFrame extends javax.swing.JFrame{
        UT2004Map map;
        boolean showArrows = false;
        public J2DFrame(){
            initComponents();
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        public J2DFrame(UT2004Map utMap){
            this();
            map = utMap;
        }
        private void initComponents(){
            setBackground(Color.white);
            setSize(COL*cellsize+2*margin[0], ROW*cellsize+2*margin[1]);
            setLocation(1160, 0);
            setVisible(true);
        }

        int set = 0;
        public void setPoint(Location l){
            Graphics2D g2 = (Graphics2D) getGraphics();
            int ind[] = getGridIndex(l);
            int i = ind[0], j = ind[1];
            dyeGridCell(i, j);
            g2.setColor(Color.red);
            Location l2 = scale(l);
            g2.fillOval((int)l2.x, (int)l2.y, 3, 3);
        }

        public void drawBubble(Location loc){
            Location s = scale(loc);
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.fillOval((int)s.x-5, (int)s.y-5, 10, 10);
        }

        public void drawString(String text){
            Graphics2D g2 = (Graphics2D) getGraphics();
            Location l = new Location(minX,maxY);
            l = scale(l);
            g2.setFont(new Font("Arial",Font.BOLD,13));
            g2.clearRect((int)l.x, (int)l.y+2, (int)l.x+ROW*cellsize, (int)l.y+24);
            g2.drawString(text,(int)l.x,(int)l.y+24);
        }

        @Override
        public void paint(Graphics g){
            Graphics2D g2 = (Graphics2D) g;
            g2.clearRect(0, 0, this.getSize().width, this.getSize().height);

            drawGrid(g2);

            if(map != null){
                g2.setColor(Color.black);
                Set<Waylink> wl = map.edgeSet();
                int numlinks = wl.size();
                Iterator<Waylink> it = wl.iterator();
                Line2D lin;
                for(int i=0; i<numlinks; i++){
                    Waylink w = it.next();
                    Location sl = scale(w.getStart().getLocation());
                    Location el = scale(w.getEnd().getLocation());
                    lin = new Line2D.Double(sl.x,sl.y,el.x,el.y);
                    g2.draw(lin);
                }
            }
        }

        private void drawGrid(Graphics2D g2){
            g2.setColor(Color.yellow);
            for(int i=0; i<ROW; i++){
                    for(int j=0; j<COL; j++){
                        if(policy[i][j]!=0){

                            g2.fillRect(j*cellsize+margin[0], i*cellsize+margin[1], cellsize, cellsize);
                        }
                }
            }
            g2.setColor(Color.green);
            for(int y=minY; y<=maxY; y+=cutrange){
                Location s = scale(new Location(minX,y));
                Location e = scale(new Location(maxX,y));
                g2.drawLine((int)s.x,(int)s.y,(int)e.x,(int)e.y);
            }
            for(int x=minX; x<=maxX; x+=cutrange){
                Location s = scale(new Location(x,minY));
                Location e = scale(new Location(x,maxY));
                g2.drawLine((int)s.x,(int)s.y,(int)e.x,(int)e.y);
            }

            if(showArrows){
                int arrowMargin = 6;
                for(int i=0; i<ROW; i++){
                    for(int j=0; j<COL; j++){
//                        if(rewards[i][j] > 0)   g2.setColor(Color.black);
//                        else    g2.setColor(Color.red);
//                        g2.drawString(String.format("%.1f", 1000*rewards[i][j]),
//                                j*cellsize+margin[0], (i+1)*cellsize+margin[1]);

                        g2.setColor(Color.blue);
                        if(policy[i][j] == 1){
                            Point ar0 = new Point((j+1)*cellsize+margin[0]-arrowMargin,
                                                (int)((i+.5)*cellsize+margin[1]));
                            Point ar1 = new Point(j*cellsize+margin[0]+arrowMargin,
                                                (int)((i+.5)*cellsize+margin[1]));
                            paintArrow(g2, ar0.x, ar0.y, ar1.x, ar1.y);
                        }
                        else if(policy[i][j] == 2){
                            Point ar0 = new Point((int)((j+.5)*cellsize+margin[0]),
                                                (i+1)*cellsize+margin[1]-arrowMargin);
                            Point ar1 = new Point((int)((j+.5)*cellsize+margin[0]),
                                                i*cellsize+margin[1]+arrowMargin);
                            paintArrow(g2, ar0.x, ar0.y, ar1.x, ar1.y);
                        }
                        else if(policy[i][j] == 3){
                            Point ar0 = new Point(j*cellsize+margin[0]+arrowMargin,
                                                (int)((i+.5)*cellsize+margin[1]));
                            Point ar1 = new Point((j+1)*cellsize+margin[0]-arrowMargin,
                                                (int)((i+.5)*cellsize+margin[1]));
                            paintArrow(g2, ar0.x, ar0.y, ar1.x, ar1.y);
                        }
                        else if(policy[i][j] == 4){
                            Point ar0 = new Point((int)((j+.5)*cellsize+margin[0]),
                                                        i*cellsize+margin[1]+arrowMargin);
                            Point ar1 = new Point((int)((j+.5)*cellsize+margin[0]),
                                                        (i+1)*cellsize+margin[1]-arrowMargin);
                            paintArrow(g2, ar0.x, ar0.y, ar1.x, ar1.y);
                        }

                    }
                }
            }
        }
        
    private void paintArrow(Graphics g, int x0, int y0, int x1,int y1){
	int deltaX = x1 - x0;
	int deltaY = y1 - y0;
	double frac = 0.2;

	g.drawLine(x0,y0,x1,y1);
	g.drawLine(x0 + (int)((1-frac)*deltaX + frac*deltaY),
		   y0 + (int)((1-frac)*deltaY - frac*deltaX),
		   x1, y1);
	g.drawLine(x0 + (int)((1-frac)*deltaX - frac*deltaY),
		   y0 + (int)((1-frac)*deltaY + frac*deltaX),
		   x1, y1);

    }

        
        int margin[] = new int[]{20,40};
        int scaledown = 5;
        int cellsize = cutrange/scaledown;
        private Location scale(Location l){
            Location l2 = new Location();
            l2.x = (l.x - minX) / scaledown + margin[0];
            l2.y = (l.y - minY) / scaledown + margin[1];
            return l2;
        }

        private int[] getGridIndex(Location l){
            return new int[]{((int)l.y-minY)/cutrange,((int)l.x-minX)/cutrange};
        }
        private Location getMapLocation(int i, int j){
            return new Location(j*cutrange+cutrange/2+minX,i*cutrange+cutrange/2+minY);
        }
        private void dyeGridCell(int i, int j){
            if(i>=0 && i<grid.length && j>=0 && j<grid[0].length){
                Graphics2D g2 = (Graphics2D) getGraphics();
                g2.setColor(Color.orange);
                g2.fillRect(j*cellsize + margin[0]+1,
                            i*cellsize + margin[1]+1,cellsize-1,cellsize-1);
            }
        }

    }


    //////////////////////////////////
    //////////// TRAINING ////////////
    //////////// TRAINING ////////////
    //////////////////////////////////

    int minX = 800;
    int minY = -2750;
    int maxX = 3200;
    int maxY = 850;
    int cutrange = 120;
    int ROW = (maxY-minY)/cutrange;
    int COL = (maxX-minX)/cutrange;
    int[][] grid = new int[ROW][COL];
    NavPoint[] navlist = new NavPoint[33];
    HashMap<String, Integer> navhash = new HashMap<String, Integer>();








    
    long prevTime = System.currentTimeMillis();
    long startTime = System.currentTimeMillis();

    public void run(){
        IUT2004Analyzer analyzer = new UT2004AnalyzerRunner(new UT2004AnalyzerFactory(new UT2004AnalyzerModule())).startAgent();

        Iterator<Player> it = analyzer.getPlayers().iterator();
        Player player = null;
        
        if(it.hasNext()){
            player = it.next();
        }
        if(it.hasNext()){
            enemy = it.next();
        }


        // if accidentally Knightbot becomes enemy, then switch it with player
        if(enemy != null){
            if(enemy.getName().equals("Knightbot")){
                Player temp = enemy;
                enemy = player;
                player = temp;
                System.out.println("... Enemy switched ...");
            }
            System.out.println("Enemy " + enemy.getName()+"\t"+enemy.getId().getStringId());
        }
        System.out.println("Player "+player.getName()+"\t"+player.getId().getStringId());



        prepareNavList(analyzer.getWorldView());
        frame = new J2DFrame(analyzer.getMap());

        
        boolean training = true;
        
        if(training){

            LinkedList<Point> path = new LinkedList<Point>();
            LinkedList<Integer> navpath = new LinkedList<Integer>();

            while(true){
                Location l = player.getLocation();
                frame.setPoint(l);
                int[] ind = frame.getGridIndex(l);
                int i = ind[0], j = ind[1];
                grid[i][j] = 1;
                Point cell = new Point(i,j);
                if(path.isEmpty())
                    path.add(cell);
                else if(!path.getLast().equals(cell)){
                    path.add(cell);
                }

                
                NavPoint nav = findClosestNavPoint(l,160);
                if(nav != null){
                    int id = getNavId(nav);
                    if(navpath.isEmpty())
                        navpath.add(id);
                    else if(!navpath.getLast().equals(id)){
                        navpath.add(id);
                    }
                    frame.drawBubble(nav.getLocation());
                    frame.drawString("Nav: "+navhash.get(nav.getId().getStringId())+" "+i+","+j);
                }

                ///////////////////////////////////
                ///// Training for attack mode ////
                ///////////////////////////////////
                if(enemy != null){
                    String data = player.getWeapon()+"\t"+player.isVisible()+"\t"+player.getAction()+"\t"+player.getFiring()+"\t"+player.isReachable();
                    //System.out.println("Enemy visible " + enemy.isVisible());
                    //System.out.println(enemy.getLocation());
                    System.out.println(data);
                }

                try{Thread.sleep(160);} catch(Exception ex){}
                long currentTime = System.currentTimeMillis();
                if(currentTime-startTime > 20000) break;
            }
            printGrid(grid);
            printPath(path);
            printNavPath(navpath);
        }
        
    }

    public int getNavId(NavPoint nav){
        int id = navhash.get(nav.getId().getStringId());
        return id;
    }

    public void prepareNavList(){
        prepareNavList(getWorldView());
    }
    public void prepareNavList(IWorldView worldView){
        navhash.put("dm-trainingday.inventoryspot48", 1);
        navhash.put("dm-trainingday.playerstart19", 2);
        navhash.put("dm-trainingday.pathnode103", 3);
        navhash.put("dm-trainingday.inventoryspot56", 4);
        navhash.put("dm-trainingday.inventoryspot46", 5);
        navhash.put("dm-trainingday.inventoryspot55", 6);
        navhash.put("dm-trainingday.playerstart18", 7);
        navhash.put("dm-trainingday.playerstart20", 8);
        navhash.put("dm-trainingday.inventoryspot62", 9);
        navhash.put("dm-trainingday.inventoryspot61", 10);
        navhash.put("dm-trainingday.inventoryspot60", 11);
        navhash.put("dm-trainingday.pathnode84", 12);
        navhash.put("dm-trainingday.playerstart21", 13);
        navhash.put("dm-trainingday.inventoryspot50", 14);
        navhash.put("dm-trainingday.inventoryspot47", 15);
        navhash.put("dm-trainingday.inventoryspot49", 16);
        navhash.put("dm-trainingday.inventoryspot52", 17);
        navhash.put("dm-trainingday.inventoryspot43", 18);
        navhash.put("dm-trainingday.inventoryspot45", 19);
        navhash.put("dm-trainingday.inventoryspot44", 20);
        navhash.put("dm-trainingday.playerstart22", 21);
        navhash.put("dm-trainingday.pathnode129", 22);
        navhash.put("dm-trainingday.inventoryspot54", 23);
        navhash.put("dm-trainingday.inventoryspot42", 24);
        navhash.put("dm-trainingday.inventoryspot53", 25);
        navhash.put("dm-trainingday.playerstart25", 26);
        navhash.put("dm-trainingday.playerstart23", 27);
        navhash.put("dm-trainingday.inventoryspot57", 28);
        navhash.put("dm-trainingday.inventoryspot58", 29);
        navhash.put("dm-trainingday.inventoryspot59", 30);
        navhash.put("dm-trainingday.pathnode133", 31);
        navhash.put("dm-trainingday.playerstart24", 32);
        navhash.put("dm-trainingday.inventoryspot51", 33);
        for(NavPoint nav : worldView.getAll(NavPoint.class).values()){
            System.out.println(nav.getId().getStringId().toLowerCase());
            System.out.println(navhash.get(nav.getId().getStringId().toLowerCase()));
            navlist[navhash.get(nav.getId().getStringId().toLowerCase())-1] = nav;
        }
    }

    public NavPoint findClosestNavPoint(Location loc, int range){
        NavPoint closestNav = null;
        double min = (range <=0 ? Double.MAX_VALUE : range);
        for(NavPoint nav : navlist){
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

    public void storeGrid(int[][] grid){
        try{
            FileWriter outFile = new FileWriter("grid.txt");
            PrintWriter out = new PrintWriter(outFile);
            for(int i=0; i<grid.length; i++){
                for(int j=0; j<grid[0].length; j++){
                    out.print(grid[i][j]+" ");
                }
                out.println();
            }
            out.close();
        } catch(Exception ex){ex.printStackTrace();}
    }

    private void printGrid(int[][] grid){
        for(int i=0; i<grid.length; i++){
            for(int j=0; j<grid[0].length; j++){
                System.out.format("%3d ", grid[i][j]);
            }
            System.out.println(";");
        }
    }

    private void printPath(LinkedList<Point> path){
        System.out.print("path = [");
        for(int i=0; i<path.size(); i++){
            Point p = path.get(i);
            System.out.print(p.x+" "+p.y);
            if(i!=path.size()-1)    System.out.print("; ");
        }
        System.out.println("] + 1;");
    }

    private void printNavPath(LinkedList<Integer> navpath){
        System.out.print("path = [");
        for(int i=0; i<navpath.size(); i++){
            System.out.print(navpath.get(i)+" ");
        }
        System.out.println("];");
    }


    int[][][] policies = Policy.policies;
    int[][] start = Policy.start;
    int[][] end = Policy.end;

    int curpolicy = 2;
    int[][] policy = policies[curpolicy];

    int[] policy2 = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1,
                            4, 1, 4, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1};




     /**
     * This method is called when the bot is started either from IDE or from command line.
     *
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
        new EmptyBot().run();
    	//new UT2004BotRunner(EmptyBot.class, "EmptyBot").startAgent();
    }

}
