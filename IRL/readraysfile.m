function rays = readraysfile(file)
tic;

%%%%% variables
% Time LocX LocY LocZ Health Shoot AltSh EnVis Jump Crouch RotPitch RotYaw 
% Weapon ELocX ELocY ELocZ HVic WVic HTaken WTaken EName");
time = [];
dist = [];





i=0;

for f=1:size(file,1)
    filename = file(f,:);
    disp(filename);
    [myfile, m1] = fopen(filename,'r');
    disp(m1);


    while(~feof(myfile))
        i=i+1;
        for j=1:19
            line = fgets(myfile);
            sp = strsplit(line);

            id = strsplit(sp{1},'_');
            time(i) = str2double(id{1});
            
            orray = 0;  %original ray
            orlayer = 0;  %original layer
            if j==1; orray = -5;
            elseif j==2||j==3||j==4
                orray = -4;
                orlayer = j-2;
            elseif j==5||j==6
                orray = j-8;
            elseif j==7||j==8||j==9
                orray = -1;
                orlayer = j-7;
            elseif j==10
                orray = 0;
            elseif j==11||j==12||j==13
                orray = 1;
                orlayer = j-11;
            elseif j==14||j==15
                orray = j-12;
            elseif j==16||j==17||j==18
                orray = 4;
                orlayer = j-16;
            elseif j==19
                orray = 5;
            end
            ray = str2double(id{2});
            layer = str2double(id{3});
            
            if ray~=orray || layer~=orlayer
                disp('ERROR in the order of rays in the data file');
            end
            
            
            dist(i,j) = str2double(sp{2});

        end
    end

    fclose(myfile);
end

%just because i like column vectors convert time into column vector
[m,n] = size(time);
if m*n==n
    time = time';
end


rays = struct('time',time,'dist',dist);


disp(['it took ' num2str(toc) ' seconds to read the file']);
fclose('all');