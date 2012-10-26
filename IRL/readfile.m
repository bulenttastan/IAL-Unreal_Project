function me = readfile(file)
tic;
global weapons enemies;
load globals;

%%%%% variables
% Time LocX LocY LocZ Health Shoot AltSh EnVis Jump Crouch RotPitch RotYaw 
% Weapon ELocX ELocY ELocZ HVic WVic HTaken WTaken EName");
time = [];
locx = [];
locy = [];
locz = [];
hlth = [];
shot = [];
asht = [];
evis = [];
jump = [];
crch = [];
ptch = [];
yaww = [];
weap = [];
elox = [];
eloy = [];
eloz = [];
enam = [];
hvic = [];
wvic = [];
hpic = [];
wpic = [];
kill = [];

i=0; j=0;

for f=1:size(file,1)
    filename = file(f,:);
    disp(filename);
    [myfile, m1] = fopen(filename,'r');
    disp(m1);

    line = fgets(myfile);
    sp = strsplit(line);
    botname = sp{2};
    for temp=0:10; fgets(myfile); end %ignore info line and 10 lines after if

    while(~feof(myfile))
        line = fgets(myfile);
        sp = strsplit(line);
        if length(sp) > 6   %it's normal message
            i=i+1;
            time(i) = str2double(sp{1});
            locx(i) = str2double(sp{2});
            locy(i) = str2double(sp{3});
            locz(i) = str2double(sp{4});
            hlth(i) = str2double(sp{5});
            shot(i) = str2double(sp{6});
            asht(i) = str2double(sp{7});
            evis(i) = str2double(sp{8});
            jump(i) = str2double(sp{9});
            crch(i) = str2double(sp{10});
            ptch(i) = str2double(sp{11});
            yaww(i) = str2double(sp{12});
            try
            weap(i) = find(strcmp(weapons,sp{13}));
            catch exception
                disp(['WEAPON NAME ' sp{13} ' ISNOT IN THE LIST']);
            end
            elox(i) = str2double(sp{14});
            eloy(i) = str2double(sp{15});
            eloz(i) = str2double(sp{16});
            hvic(i) = str2double(sp{17});
            wvic(i) = str2double(sp{18});
            hpic(i) = str2double(sp{19});
            wpic(i) = str2double(sp{20});
            try
            enam(i) = find(strcmp(enemies,strtrim(sp{21})));
            catch exception
                disp(['ENEMY NAME ' sp{21} ' ISNOT IN THE LIST']);
            end
        elseif strcmp(sp{1},'BOT_KILLED')    %it's bot_died message
            j=j+1;
            kill(j) = str2double(sp{3});
        end
    end

    fclose(myfile);
end
%just because i like column vectors
time = time(:);
locx = locx(:);
locy = locy(:);
locz = locz(:);
hlth = hlth(:);
shot = shot(:);
asht = asht(:);
evis = evis(:);
jump = jump(:);
crch = crch(:);
ptch = ptch(:);
yaww = yaww(:);
weap = weap(:);
elox = elox(:);
eloy = eloy(:);
eloz = eloz(:);
enam = enam(:);
hvic = hvic(:);
wvic = wvic(:);
hpic = hpic(:);
wpic = wpic(:);
kill = kill(:);

me = struct('botname',botname,'time',time,'locx',locx,'locy',locy,'locz',locz,...
            'health',hlth,'shooting',shot,'altShooting',asht,'enemyVisible',evis,...
            'jumping',jump,'crouching',crch,'pitch',ptch,'yaw',yaww,'weapon',weap,...
            'enemylocx',elox,'enemylocy',eloy,'enemylocz',eloz,'enemyName',enam,...
            'isHealthVicinity',hvic,'isWeaponVicinity',wvic,'healthPicked',hpic,...
            'weaponPicked',wpic,'killed',kill);


disp(['it took ' num2str(toc) ' seconds to read the file']);
fclose('all');