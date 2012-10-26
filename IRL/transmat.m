clear; clc; close all;
% weapons = {'AssaultRifle','ShockRifle','LinkGun','BioRifle','Minigun','FlakCannon','RocketLauncher','SniperRifle','LightningGun','Redeemer','IonPainter','AVRiL','GrenadeLauncher','MineLayer','BallLauncher','None','ShieldGun'};
% enemies = {'Mokara','Makreth','Faraleth','Nebri','Damarus','Komek','Motig',...
%            'Selig','Thorax','Mandible','Widowmaker','Syzygy','Cobalt','Rapier',...
%            'Corrosion','Renegade','Diva','Memphis','Asp','Cleopatra','Scarab',...
%            'Horus','Roc','Hyena','Rylisa','Reinha','Ambrosia','Siren','Gorge',...
%            'Frostbite','Cannonball','Arclite','Prism','BlackJack','Sapphire',...
%            'Satin','Lauren','Wraith','Torch','Romulus','Remus','Brock','Malcolm',...
%            'Domina','Subversa','Lilith','Fate','Brutalis','Ravage','Mr.Crow',...
%            'Harlequin','Dominator','Guardian','Skakruk','Kraagesh','ClanLord',...
%            'Drekorig','Gaargod','Gkublok','Virus','Cyclops','Axon','XanKriegor',...
%            'Cathode','Matrix','Divisor','Enigma','Othello','Jakob','Taye',...
%            'Azure','Aryss','Riker','Picard','Annika','Tamika','Outlaw','Garrett',...
%            'Zarina','Kaela','Kane','Baird','Rae','Greith','Abaddon','Ophelia',...
%            'Mekkor','Skrilax','BarkTooth','Karag','Kragoth','Thannis','NoName'};
% save globals weapons enemies;
% file = ['data/Knightbot_1302884558458.txt';
%         'data/Knightbot_1302885131417.txt';
%         'data/Knightbot_1302885697022.txt';
%         'data/Knightbot_1302887755649.txt';
%         'data/Knightbot_1303774894726.txt';
%         'data/Knightbot_1303776176867.txt';
%         'data/Knightbot_1303874819292.txt'];
% file =['data/Knightbot_1303874819292.txt'];
% me = readfile(file);
% save data me;

% [fp, m1] = fopen('data/rayscan_DM-TrainingDay.txt','r');
% disp(m1);
% scan = [];
% i=0;
% while ~feof(fp)
%     i=i+1;
%     line = fgets(fp);
%     sp = strsplit(' ',line);
%     x = str2double(sp{1});
%     y = str2double(sp{2});
%     z = str2double(sp{3});
% %     if z>-90 && z<-60
%         scan(end+1,:) = [x y z];
% %     end
%     if mod(i,100000)==0
%         break;
%     end
% end
% fclose(fp);
% save rayscan3 scan;
% disp(['min x: ' num2str(min(scan(:,1))) ]);
% disp(['min y: ' num2str(min(scan(:,2))) ]);
% disp(['max x: ' num2str(max(scan(:,1))) ]);
% disp(['max y: ' num2str(max(scan(:,2))) ]);
minX = 770;
minY = -2700;
maxX = 3270;
maxY = 800;



% %plotting the rayscan
% load rayscan;
% axis equal;
% hold on;
% % set(gca,'YDir','reverse')
% for i=1:6
%     x = (maxX-minX)/5*(i-1) + minX;
%     plot([x x],[minY maxY]);
% end
% for i=1:8
%     y = (maxY-minY)/7*(i-1) + minY;
%     plot([minX maxX],[y y]);
% end
% for i=1:length(scan)
%     plot(scan(i,1),scan(i,2),'r');
% end




load data;


myt = me.time;
myh = me.health;
mys = me.shooting;
myl = [me.locx me.locy me.locz];
env = me.enemyVisible;
hvi = me.isHealthVicinity;
wvi = me.isWeaponVicinity;
hpi = me.healthPicked;
wpi = me.weaponPicked;


% TRANSITION MATRIX GENERATION

len = floor(length(myt)/5);

states = zeros(len,1);
actions = zeros(len,1);
from = [];
to = [];
act = [];
prevState = 0;
prevC = 0;
Csize = 35;
Hsize = 5;
Esize = 2;
Wsize = 2;
hsize = 2;
Dsize = 9;
Ssize = 2;
ignoreList = [];
for i=1:len
    st = (i-1)*5+1;
    en = i*5;
    %disp([num2str(st) ' -> ' num2str(en)]);
    % AVERAGE HEALTH AND LOCATION OF THE BOT
    health = mean(myh(st:en));
    location = mean(myl(st:en,:));
    if health<30; H = 0;
    elseif health<60; H = 1;
    elseif health<90; H = 2;
    elseif health<120; H = 3;
    else H = 4;
    end
    
    C = ceil((location(1)-minX)/500) + floor((location(2)-minY)/500) * 5;
    E = max(env(st:en));
    W = max(wvi(st:en));
    h = max(hvi(st:en));
    
    currentState = C + Csize*(H + Hsize*(E + Esize*(W + Wsize*h)));
    states(i) = currentState;
    
    
    %DEBUGGING
    correct = false;
    if prevC~=0
        if C==prevC || C==prevC+5 || C==prevC+6 || C==prevC+1 || C==prevC-4 || C==prevC-5 || C==prevC-6 || C==prevC-1 || C==prevC+4
            from(end+1) = prevState;
            to(end+1) = currentState;
            correct = true;
        end
    end
    prevC = C;
    prevState = currentState;
    %END OF DEBUGGING
    
    
    
%     a = myl(st:en,1:2);
%     plot(a(:,1),a(:,2));
  
    d = myl(en,1:2) - myl(st,1:2);
    x = d(1);
    y = d(2);
    
    angle = abs(atan(y/x) * 180/pi);
    
    % action for 8 connectivity and extra for not moving
    if abs(x) < 10 && abs(y) < 10
        direction = 9;
    else
        if angle < 35 %x axis
            if x > 0
                direction = 3;
            else
                direction = 7;
            end
        elseif angle > 55 %y axis
            if y > 0
                direction = 1;
            else
                direction = 5;
            end
        else %diagonal
            if x > 0 && y > 0
                direction = 2;
            elseif x > 0 && y < 0
                direction = 4;
            elseif x < 0 && y < 0
                direction = 6;
            else
                direction = 8;
            end
        end
    end
    
    
%     disp(['x:' num2str(round(x)) ' y:' num2str(round(y)) ' ang:' num2str(angle) ...
%         ' dir:' num2str(direction)]);
    
%     if direction == 9
%         disp('');
%     end


    shooting = max(mys(st:en));
    healthPackPicked = max(hpi(st:en));
    weaponPicked = max(wpi(st:en));

    if healthPackPicked
        itemPicked = 1;
    elseif weaponPicked
        itemPicked = 2;
    else
        itemPicked = 0;
    end
    
    currentAction = direction + Dsize*(shooting + Ssize*itemPicked);
    actions(i) = currentAction;
    
    if correct
        act(end+1) = currentAction;
    end
end







A = 54;
N = 1400;
% P = zeros(N,N,A);
% 
% % USE STATES FOR TRANSITION MATRIX
% % disp(length(states));
% for i=1:length(states)-1
%     from = states(i);
%     to = states(i+1);
%     act = actions(i);
%     P(from,to,act) = P(from,to,act) + 1;
%     % ADD ACTIONS to STATE TRANSITION
% end


P = zeros(N,N,A);
for i=1:length(from)
    P(from(i),to(i),act(i)) = P(from(i),to(i),act(i)) + 1;
end





%%%% REWARD FUNCTION CALCULATIONS
R = zeros(N,1);

kills = zeros(Csize,1);
wasKilled = zeros(Csize,1);
smallHealth = zeros(Csize,1);
bigHealth = zeros(Csize,1);
weapon = zeros(Csize,1);
ammo = zeros(Csize,1);
wasAttacked = zeros(Csize,1);
attacks = zeros(Csize,1);
wasShot = zeros(Csize,1);
heardNoise = zeros(Csize,1);
sawPlayer = zeros(Csize,1);
[fp, m1] = fopen('data/hotspots_DM-TrainingDay.txt','r');
disp(m1);
% the data file is written bottom to up vertically
for i=0:Csize-1
    row = mod(i,7);
    col = floor(i/7);
    index = row*5+col+1;
    
    line = fgets(fp);
    sp = strsplit(' ',line);
    kills(index) = str2double(sp{2});
    wasKilled(index) = str2double(sp{4});
    smallHealth(index) = str2double(sp{6});
    bigHealth(index) = str2double(sp{8});
    weapon(index) = str2double(sp{10});
    ammo(index) = str2double(sp{12});
    wasAttacked(index) = str2double(sp{14});
    attacks(index) = str2double(sp{16});
    wasShot(index) = str2double(sp{18});
    heardNoise(index) = str2double(sp{20});
    sawPlayer(index) = str2double(sp{22});
end

% filtering the hot spots by ignoring anything below mean
kills(kills < mean(kills(kills~=0))) = 0;
wasKilled(wasKilled < mean(wasKilled(wasKilled~=0))) = 0;
smallHealth(smallHealth < mean(smallHealth(smallHealth~=0))) = 0;
bigHealth(bigHealth < mean(bigHealth(bigHealth~=0))) = 0;
weapon(weapon < mean(weapon(weapon~=0))) = 0;
ammo(ammo < mean(ammo(ammo~=0))) = 0;
wasAttacked(wasAttacked < mean(wasAttacked(wasAttacked~=0))) = 0;
attacks(attacks < mean(attacks(attacks~=0))) = 0;
wasShot(wasShot < mean(wasShot(wasShot~=0))) = 0;
heardNoise(heardNoise < mean(heardNoise(heardNoise~=0))) = 0;
sawPlayer(sawPlayer < mean(sawPlayer(sawPlayer~=0))) = 0;



% %low health
% smallHealth
% bigHealth
% 
% %high health
% smallHealth
% 
% %enemy not visible and high health
% sawPlayer
% 
% %high health
% kills | wasKilled
% attacks
% wasAttacked
% wasShot
for i=0:N-1
    C = mod(i,Csize);
    ind = (i - C)/Csize;
    H = mod(ind,Hsize);
    ind = (ind - H)/Hsize;
    E = mod(ind,Esize);
    C = C + 1;
    
    if H < 3 && (smallHealth(C) > 0 || bigHealth(C) > 0)
        R(i) = 1;
    elseif H >= 3 && smallHealth(C) > 0
        R(i) = 1;
    end

    if E == 0 && H >= 3 && sawPlayer(C)
        R(i) = 1;
    end

    if H >= 3 && (kills(C) || wasKilled(C) || attacks(C) || wasAttacked(C) || wasShot(C))
        R(i) = 1;
    end
end




% %NEW REWARDS
% occupancyGrid = [0 0 1 1 1 ...
%                  0 0 1 0 1 ...
%                  0 0 1 1 1 ...
%                  0 0 1 0 0 ...
%                  1 1 1 0 0 ...
%                  1 0 1 0 0 ...
%                  1 1 1 0 0];
% R = -1*ones(N,1);
% for h=0:1
%     for w=0:1
%         for c=1:Csize
%             if occupancyGrid(c) == 1
%                 index = c + Csize*(4 + Hsize*(1 + Esize*(w + Wsize*h)));
%                 R(index) = 1;
%             end
%         end
%     end
% end


% normalise P in 2nd dimension, done manually normalise function gives
% memory overflow error
for i=1:N
    for j=1:A
        s = sum(P(i,:,j));
        if s~=0
            P(i,:,j) = P(i,:,j)./s;
        end
    end
end

% normalizing transition matrix, so the probs will sum up to 1
% P = normalise(P,2);


clear_all_but('N','A','P','R');







% calculating the most performed action at each state
policy = zeros(N,1);
Psum = sum(P,2);
for i=1:N
    [m,mi] = max(Psum(i,:,:));
    policy(i) = mi;
end


myCplex(N,A,P,policy);






tic;
% VALUE ITERATION %
V = zeros(size(P,1),1);
p = zeros(size(P,1),1);
gamma = .9;
thres = 1e-4;
done = false;
numiter = 0;

while ~done
    numiter = numiter+1;
    newV = zeros(size(P,1),1);
    y = zeros(A,1);
    for s=1:size(P,1)
        for a=1:A
            y(a) = gamma*P(s,:,a)*V;
        end
        [ymax,yind] = max(y);
        newV(s) = R(s) + ymax;
        p(s) = yind;
    end

    if newV - V < thres; done = true; end
    
    disp(['Iter: ' num2str(numiter) '  diff: ' num2str(max(newV - V))]);
    
    V = newV;
end

disp(['Num iterations: ' num2str(numiter)]);
disp(['it took ' num2str(toc) ' seconds to run value iteration']);

clear_all_but('N','V','p');



str = '';
for i=1:N-1
    str = [str num2str(p(i)) ', '];
end
str = [str num2str(p(i))];
disp(['int[] policy = new int[]{' str '};']);
% double values = new double[]{0, 0, 9.9992, 8.9992, 6.6906, 0, 1, 7.3628, 5.8304, 6.0214, 0, 1, 9.9992, 8.9992, 8.181, 0, 1, 8.181, 0, 0, 9.9992, 9.9992, 8.9992, 0, 1, 7.8252, 1, 5.0135, 0, 0, 9.9992, 7.4992, 4.8304, 0, 0, 0, 0, 7.391, 7.391, 0, 0, 0, 8.0992, 4.5101, 7.2892, 0, 0, 8.181, 7.2892, 6.5602, 0, 1, 7.3628, 0, 0, 8.0992, 7.0121, 6.6264, 0, 1, 5.712, 0, 5.6655, 0, 0, 5.7969, 6.4411, 6.9247, 0, 0, 0, 0, 4.6298, 5.6588, 5.0929, 0, 0, 4.9062, 5.2903, 5.0623, 0, 0, 7.2892, 6.0295, 5.4264, 0, 9.9992, 6.0239, 5.014, 0, 5.7227, 8.9992, 6.5602, 0, 1, 4.9285, 4.8304, 5.0988, 0, 0, 5.9637, 6.6264, 6.0239, 0, 0, 0, 0, 4.9199, 4.0423, 3.9002, 0, 0, 5.4667, 1, 4.7671, 0, 0, 6.5602, 5.9041, 5.3136, 0, 1, 5.9041, 0, 0, 5.9041, 5.6211, 5.3136, 0, 1, 5.3672, 0, 4.3682, 0, 0, 5.9637, 6.6264, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 9.9992, 8.9992, 5.7849, 0, 1, 8.9992, 1, 6.7492, 0, 1, 9.9992, 9.9992, 8.9992, 0, 9.9992, 8.9992, 8.9992, 9.9992, 9.9992, 9.9992, 8.9992, 0, 1, 8.9992, 1, 7.3628, 0, 0, 9.9992, 9.9992, 6.7492, 0, 0, 0, 0, 8.2123, 7.391, 6.047, 0, 0, 7.3762, 4.716, 7.2892, 0, 0, 8.9992, 8.0992, 8.0992, 0, 7.2672, 7.6942, 3.5101, 0, 8.9992, 8.181, 7.3628, 0, 1, 6.7492, 0, 6.9247, 0, 0, 0, 8.9992, 7.6942, 0, 0, 0, 0, 7.391, 6.6518, 5.4422, 0, 0, 6.6264, 5.2903, 5.6249, 0, 0, 8.0992, 6.1903, 5.7849, 8.9992, 9.9992, 6.6264, 5.5712, 4.2206, 6.3587, 7.3628, 6.5602, 5.3136, 1, 5.7227, 4.8304, 6.2321, 0, 0, 5.1504, 6.6264, 7.3628, 0, 0, 0, 0, 4.7821, 4.3038, 4.0814, 0, 0, 5.2667, 4.6379, 4.3071, 0, 0, 7.2892, 5.9637, 5.0094, 0, 6.6211, 5.9627, 5.3672, 0, 5.1617, 6.2457, 5.9632, 0, 1, 4.4462, 0, 5.6088, 0, 0, 5.0988, 6.6264, 7.3628, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 9.9992, 8.0992, 7.2892, 0, 1, 8.142, 1, 6.0742, 0, 1, 9.9992, 8.0992, 7.3628, 0, 9.9992, 8.0992, 0, 0, 9.9992, 9.9992, 8.0992, 0, 1, 8.181, 1, 3.5101, 0, 0, 9.9992, 7.4992, 4.3473, 0, 0, 0, 0, 6.047, 6.335, 5.7014, 0, 0, 5.4422, 1, 6.5602, 0, 0, 0, 5.6365, 0, 0, 7.6264, 6.0271, 0, 0, 6.6264, 7.3628, 6.6264, 0, 1, 5.9637, 0, 5.6277, 0, 0, 6.6264, 6.948, 6.2531, 0, 0, 0, 0, 4.9062, 4.9686, 4.4717, 0, 0, 5.4514, 1, 3.6585, 0, 0, 6.5966, 5.6541, 0, 0, 1, 6.5602, 0, 0, 7.3628, 8.0992, 7.2892, 3.475, 1, 5.5009, 0, 5.4215, 0, 0, 5.5164, 5.9637, 5.3672, 0, 0, 0, 0, 5.3136, 3.5402, 3.1861, 0, 0, 5.9041, 1, 3.8763, 0, 0, 7.2892, 5.3136, 5.2969, 0, 1, 6.5602, 0, 0, 7.2892, 6.6264, 5.9041, 0, 1, 6.1168, 0, 4.8537, 0, 0, 5.0988, 5.4215, 4.4356, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 9.9992, 5.8138, 5.4667, 0, 1, 8.9992, 1, 8.0992, 0, 1, 9.9992, 9.9992, 8.9992, 0, 1, 8.9992, 0, 1, 9.9992, 9.9992, 7.3628, 0, 1, 6.0742, 1, 6.3108, 0, 0, 1, 9.9992, 0, 0, 0, 0, 0, 6.6518, 6.6264, 5.3971, 0, 0, 6.6385, 1, 5.9637, 0, 0, 7.3667, 8.142, 0, 0, 7.5404, 7.3628, 0, 0, 7.3628, 8.181, 7.3628, 0, 1, 7.3628, 4.8304, 7.2892, 0, 0, 8.9992, 8.0992, 8.0992, 0, 0, 0, 0, 5.4215, 0, 0, 0, 0, 5.5179, 6.3136, 5.0398, 0, 5.3136, 7.2892, 5.7899, 5.2109, 0, 1, 7.2892, 5.2109, 4.6897, 6.1122, 8.9992, 6.6264, 0, 1, 5.5009, 7.2892, 6.6264, 0, 0, 0, 7.2892, 6.5602, 0, 0, 0, 0, 5.3136, 0, 1.8952, 0, 0, 5.9041, 1, 4.7671, 0, 0, 6.5602, 5.8855, 5.2969, 6.5602, 6.9637, 5.9656, 5.3672, 0, 6.7966, 8.0992, 7.2892, 0, 1, 4.5006, 0, 6.5602, 0, 0, 5.6655, 6.2951, 6.0239, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 4.3038, 0, 1, 0, 1, 5.4192, 0, 1, 1, 4.3894, 4.8772, 0, 1, 0, 0, 0, 9.9992, 9.9992, 0, 0, 1, 6.4023, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 5.3672, 6.5602, 0, 1, 5.4215, 0, 0, 7.3628, 6.6264, 4.8792, 0, 1, 4.8792, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3.5044, 0, 0, 0, 4.8304, 2.9238, 0, 1, 5.4215, 0, 0, 0, 0, 0, 0, 1, 4.9507, 0, 0, 0, 0, 6.6264, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.6379, 4.129, 0, 0, 0, 4.8792, 0, 0, 1, 0, 0, 0, 6.1168, 4.9545, 0, 0, 1, 4.3912, 0, 0, 0, 0, 3.952, 3.5567, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 5.0589, 0, 1, 0, 1, 4.7821, 0, 1, 9.9992, 9.9992, 5.0617, 0, 1, 7.2892, 0, 1, 9.9992, 9.9992, 8.0992, 0, 1, 8.0992, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5.6211, 1, 6.5602, 0, 0, 0, 0, 7.2892, 0, 6.9637, 7.2892, 0, 0, 6.1947, 4.6458, 7.2892, 0, 1, 8.9992, 0, 6.2321, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6.6385, 1, 4.8304, 0, 0, 5.3672, 5.3672, 5.9637, 0, 1, 5.9637, 0, 0, 5.7227, 4.9545, 4.4589, 3.8612, 1, 6.0239, 3.952, 0, 0, 0, 7.3628, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4.1418, 0, 0, 5.9041, 5.4667, 3.3886, 0, 1, 5.9637, 4.2903, 0, 0, 5.5051, 5.367, 0, 1, 4.6454, 0, 5.3668, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 8.0992, 1, 5.3136, 0, 1, 9.9992, 8.9992, 0, 0, 9.9992, 5.9637, 0, 0, 1, 9.9992, 8.9992, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 5.9041, 0, 0, 0, 0, 0, 0, 1, 6.6264, 0, 0, 4.0505, 5.9637, 0, 0, 1, 4.5006, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2.9932, 0, 0, 0, 4.3473, 0, 0, 1, 5.9041, 0, 0, 4.4556, 4.0099, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3.0191, 0, 0, 0, 0, 1, 0, 0, 0, 4.1741, 0, 0, 0, 1, 5.369, 0, 0, 6.3385, 5.187, 0, 0, 1, 5.7046, 0, 0, 0, 0, 0, 2.9099, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 8.9992, 1, 0, 0, 1, 9.9992, 9.0992, 4.4589, 0, 8.2892, 8.0992, 0, 1, 1, 9.9992, 7.2892, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7.3297, 1, 6.5602, 0, 0, 8.1442, 0, 0, 0, 1, 8.0992, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.9129, 1, 5.3672, 0, 0, 5.4589, 0, 0, 0, 1, 7.3297, 0, 0, 0, 4.9545, 0, 0, 1, 3.952, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3.3547, 3.7275, 0, 0, 6.5602, 1, 0, 0, 0, 5.9041, 4.4726, 0, 0, 1, 5.369, 0, 0, 4.6454, 6.1477, 4.832, 0, 1, 6.1168, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0};
% int[] policy = new int[]{1, 1, 16, 16, 7, 1, 1, 10, 8, 14, 1, 1, 1, 8, 16, 1, 1, 14, 1, 1, 3, 12, 6, 1, 1, 1, 1, 3, 1, 1, 1, 16, 8, 1, 1, 1, 1, 15, 7, 1, 1, 1, 4, 14, 1, 1, 1, 14, 15, 8, 1, 1, 14, 1, 1, 1, 12, 46, 1, 1, 5, 1, 1, 1, 1, 3, 12, 5, 1, 1, 1, 1, 3, 7, 16, 1, 1, 1, 13, 3, 1, 1, 15, 16, 7, 1, 6, 12, 10, 1, 8, 40, 14, 1, 1, 7, 8, 5, 1, 1, 2, 14, 1, 1, 1, 1, 1, 1, 16, 10, 1, 1, 1, 1, 19, 1, 1, 7, 8, 7, 1, 1, 5, 1, 1, 17, 6, 14, 1, 1, 2, 1, 1, 1, 1, 3, 48, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 13, 53, 16, 1, 1, 16, 1, 1, 1, 1, 4, 7, 16, 1, 7, 12, 16, 10, 1, 1, 17, 1, 1, 12, 1, 14, 1, 1, 1, 4, 7, 1, 1, 1, 1, 10, 7, 15, 1, 1, 5, 12, 1, 1, 1, 12, 7, 10, 1, 18, 3, 14, 1, 13, 18, 16, 1, 1, 14, 1, 8, 1, 1, 1, 5, 7, 1, 1, 1, 1, 12, 7, 14, 1, 1, 12, 12, 1, 1, 1, 4, 12, 12, 14, 12, 4, 5, 10, 12, 15, 13, 14, 1, 5, 8, 3, 1, 1, 14, 12, 8, 1, 1, 1, 1, 12, 7, 3, 1, 1, 1, 14, 1, 1, 1, 52, 8, 16, 1, 16, 15, 12, 1, 12, 14, 15, 1, 1, 5, 1, 3, 1, 1, 14, 3, 18, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 12, 6, 1, 1, 14, 1, 10, 1, 1, 12, 10, 3, 1, 2, 14, 1, 1, 18, 1, 50, 1, 1, 5, 1, 8, 1, 1, 15, 17, 1, 1, 1, 1, 1, 3, 43, 7, 1, 1, 13, 1, 37, 1, 1, 1, 12, 1, 1, 7, 14, 1, 1, 14, 2, 1, 1, 1, 5, 1, 46, 1, 1, 2, 39, 16, 1, 1, 1, 1, 14, 43, 7, 1, 1, 1, 1, 5, 1, 1, 10, 16, 1, 1, 1, 46, 1, 1, 12, 11, 8, 39, 1, 5, 1, 10, 1, 1, 3, 7, 8, 1, 1, 1, 1, 16, 7, 7, 1, 1, 11, 1, 37, 1, 1, 13, 39, 16, 1, 1, 3, 1, 1, 10, 9, 13, 1, 1, 5, 1, 5, 1, 1, 10, 2, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 12, 10, 1, 1, 5, 1, 10, 1, 1, 5, 7, 16, 1, 1, 52, 1, 1, 5, 3, 5, 1, 1, 34, 1, 14, 1, 1, 1, 11, 1, 1, 1, 1, 1, 48, 12, 16, 1, 1, 5, 1, 14, 1, 1, 14, 12, 1, 1, 15, 17, 1, 1, 12, 9, 12, 1, 1, 10, 7, 7, 1, 1, 12, 39, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 10, 37, 10, 1, 5, 3, 16, 17, 1, 1, 8, 5, 5, 12, 41, 16, 1, 1, 14, 14, 12, 1, 1, 1, 12, 7, 1, 1, 1, 1, 1, 1, 9, 1, 1, 3, 1, 1, 1, 1, 2, 15, 7, 16, 17, 3, 14, 1, 12, 17, 52, 1, 1, 14, 1, 15, 1, 1, 10, 39, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 23, 1, 1, 1, 3, 5, 1, 1, 1, 1, 1, 7, 7, 1, 1, 1, 19, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 12, 1, 1, 5, 1, 1, 2, 8, 3, 1, 1, 19, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 23, 1, 1, 1, 3, 5, 1, 1, 23, 1, 1, 1, 1, 1, 1, 1, 19, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 14, 19, 1, 1, 1, 7, 1, 1, 1, 1, 1, 1, 23, 16, 1, 1, 1, 19, 1, 1, 1, 1, 5, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 16, 1, 1, 1, 1, 14, 1, 1, 1, 17, 9, 1, 1, 1, 1, 1, 10, 3, 1, 1, 1, 11, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 17, 1, 17, 1, 1, 1, 1, 12, 1, 1, 7, 1, 1, 10, 16, 5, 1, 1, 18, 1, 17, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 14, 1, 11, 1, 1, 17, 12, 12, 1, 1, 31, 1, 1, 14, 6, 16, 5, 1, 10, 7, 1, 1, 1, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 28, 1, 1, 5, 16, 14, 1, 1, 13, 5, 1, 1, 7, 12, 1, 1, 5, 1, 14, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 50, 1, 28, 1, 1, 10, 16, 1, 1, 7, 1, 1, 1, 1, 16, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 1, 1, 1, 1, 1, 1, 19, 1, 1, 1, 12, 1, 1, 1, 19, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1, 1, 1, 39, 1, 1, 1, 23, 1, 1, 37, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 32, 1, 1, 23, 3, 1, 1, 1, 23, 1, 1, 1, 1, 1, 43, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 1, 1, 1, 10, 16, 8, 1, 13, 32, 1, 1, 1, 9, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 28, 1, 1, 10, 1, 1, 1, 1, 32, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 11, 1, 1, 10, 1, 1, 1, 1, 14, 1, 1, 1, 7, 1, 1, 1, 17, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 1, 10, 1, 1, 1, 1, 16, 7, 1, 1, 1, 32, 1, 1, 16, 12, 14, 1, 1, 23, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

% testing why policy has a lot of 1s
% clc;
% for i=0:N-1
%     if p(i+1)==1
%         C = mod(i,Csize);
%         ind = (i - C)/Csize;
%         H = mod(ind,Hsize);
%         ind = (ind - H)/Hsize;
%         E = mod(ind,Esize);
%         ind = (ind - E)/Esize;
%         W = mod(ind,Wsize);
%         h = (ind - W)/Wsize;
%         C = C + 1;
%         disp(C);
%     end
% end








