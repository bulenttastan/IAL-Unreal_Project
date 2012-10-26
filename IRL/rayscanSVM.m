clear; close all; clc;
addpath('libsvm');
load globals;


% me = readfile('data/Bulent_1305441107582.txt');
% rays = readraysfile('data/Bulent_1305441107582_rays.txt');
% save me_rays me rays;
load me_rays;
% me = readfile('data/Bulent_1305487832094.txt');
% rays = readraysfile('data/Bulent_1305487832094_rays.txt');
% save me_rays_test me rays;
% load me_rays_test;


%in order to align the time in me data and rays data,
%we need to remove everything in rays data up to time starts in me dataset
ind = find(rays.time == me.time(1)) - 1;
rays.time(1:ind) = [];
rays.dist(1:ind,:) = [];


myt = me.time;
myd = rays.dist;
myj = me.jumping;
myy = me.yaw;

% data alignment check
for i=1:length(myt)
    if(myt(i) ~= rays.time(i))
        disp('Error');
    end
end




mya = zeros(length(myt)-1,1);
angles = zeros(length(myt)-1,1);

% for IRL
myd4IRL = myd(:,[2,4,5,6,10,14,15,16,18]); %remove some data cause state space is large
featuresize = size(myd4IRL,2);
% cutpoints = inf*ones(featuresize,4); %divide into 4 (19x4)
% cutpoints(:,2) = round(median(myd,1));
% for i=1:size(myd,2)
%     cutpoints(i,1) = round(median(myd(myd(:,i)<cutpoints(i,2), i)));
% end
% for i=1:size(myd,2)
%     cutpoints(i,3) = round(median(myd(myd(:,i)>cutpoints(i,2), i)));
% end
cutpoints = inf*ones(featuresize,2);
cutpoints(:,1) = round(median(myd4IRL,1));
from = zeros(length(myt)-1,1);
to = zeros(length(myt)-1,1);
act = zeros(length(myt)-1,1);


for i=1:length(myt)-1
   	angle = 360*(myy(i+1) - myy(i)) / 65535;
    if angle>180; angle = angle - 360;
    elseif angle <-180; angle = angle + 360; end
    angles(i) = angle;
    
    if angle < -4
        mya(i) = 1;
    elseif angle < 4
        mya(i) = 2;
    else
        mya(i) = 3;
    end
    
%     if angle < -10
%         mya(i) = 1;
%     elseif angle < -4
%         mya(i) = 2;
%     elseif angle < 4
%         mya(i) = 3;
%     elseif angle < 10
%         mya(i) = 4;
%     else
%         mya(i) = 5;
%     end



    % IRL data creation
    state = zeros(1,featuresize);
    for j=1:featuresize
        state(j) = find(myd4IRL(i,j) < cutpoints(j,:),1,'first') - 1;
    end
    s = state(1);
    for j=2:featuresize
        s = s * size(cutpoints,2) + state(j);
    end
    from(i) = s + 1;
    state = zeros(1,featuresize);
    for j=1:featuresize
        state(j) = find(myd4IRL(i+1,j) < cutpoints(j,:),1,'first') - 1;
    end
    s = state(1);
    for j=2:featuresize
        s = s * size(cutpoints,2) + state(j);
    end
    to(i) = s + 1;
    
    act(i) = mya(i);
    % end of IRL data creation
    
    
    
    
%     if myj(i) == 1
%         mya(i) = mya(i) + 5;
%     end
%     disp(['mya(' num2str(i) '): ' num2str(mya(i))]);
end



%%%%%%% IRL %%%%%%%
if false
    N = size(cutpoints,2)^featuresize;
    A = max(act);
    P = zeros(N,N,A);
    for i=1:length(from)
        P(from(i),to(i),act(i)) = P(from(i),to(i),act(i)) + 1;
    end

    % normalize P
    for i=1:N
        for j=1:A
            s = sum(P(i,:,j));
            if s~=0
                P(i,:,j) = P(i,:,j)./s;
            end
        end
    end




    for i=1:A
        countS = 0;
        for j=1:N
            if sum(P(j,:,i)) == 0
                countS = countS+1;
            end
        end
        disp(['# empty slots for action(' num2str(i) '): ' num2str(countS) ' - %: ' num2str(100*countS/N)]);
    end


    policy = zeros(N,1);
    Psum = sum(P,2);
    for i=1:N
        [m,mi] = max(Psum(i,:,:));
        policy(i) = mi;
    end


    R = myCplex(N,A,P,policy);
    % R = [-1, -1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, -1, -1, 0, 0, 0,...
    % -1, -1, 0, 1, 0, -1, 0, 1, 1, -1, -1, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0, 0, 0, ...
    % 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, ...
    % 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,...
    % 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 1, 0, 0, ...
    % 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, -1, 0, 0, 1, 1, -1, -1, 0, -1, ...
    % 0, 1, -1, 1, -1, -1, -1, -1, 0, 0, 1, -1, 0, 1, 0, -1, 1, 1, 1, 1, 0, 1, 0, ...
    % -1, 0, 0, 0, -1, 0, -1, 0, 0.0035147, 0, -1, 0, -1, 0, -1, 0, 1, 0, 0, 0, 0,...
    % 0, 0, 0, -1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 1, ...
    % -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, -1, 0, -1, ...
    % 0, -1, 0, 1, 0, 1, 0, 1, 0, 0.38405, 0, -0.40102, 0, 0, 0, -1, 0, 1, 0, 1, 0, ...
    % -1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.75661, 0, 0, 0, 0, 0, ...
    % 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,...
    % 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ...
    % 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ...
    % 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, ...
    % 1, 0, 0, 0, 0, 0, 0, -1, 1, 0, 1, 0, -0.11782, 0, 1, 0, -1, 0, -1, 0, 1, 1,...
    % 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, -1, 0, 1, 0, 0, 0, 1, 0, -1, 0, 0, 0, -1, 0, ...
    % 0, 0, -0.52197, 0, 1, 0, 0, 0, -1, 0, -1, 0, 1, 0, 1, 0, 0, 0, -1, 0, 1, 0,...
    % 0, 0, 0, 0, -1, 0, -1, 0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0, -1,...
    % 0, 0, 0, -1, 0, -1, 0, 1, 0, 1, 0, 0, 0, 1, 0, -1, 0, -0.50405, 0, 1, 0, 1,...
    % 0, -1, 0, -1, -1, 1, 1, 1, -1, -1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1];

    ValueIteration(N,A,P,R);


end


%%%%%%% SVM %%%%%%%
featuresize = size(myd,2);
myd(end,:) = [];
myj(end) = [];

% make the labels list of same size
minlen = length(find(mya==1));
for i=2:max(mya)
    length(find(mya==i));
    minlen = min(minlen, length(find(mya==i)));
end
%maxlen = min([length(find(mya==1))  length(find(mya==2))  length(find(mya==3))]);

list = [];find(mya==1,minlen,'first');
for i=1:max(mya)
    list = [list;find(mya==i,minlen,'first')];
end
list = sort(list);
mya = mya(list);
myd = myd(list,:);


%rescaling the features
for i=1:size(myd,2)
    data = myd(:,i);
    data = (data - min(data));
    data = data ./ max(data);
    myd(:,i) = data;
%     disp([num2str(mean(myd(:,i))) ' - ' num2str(max(myd(:,i)))]);
end


% shuffle the data
order = randperm(length(mya));
mya = mya(order);
myd = myd(order,:);

% [file,m1] = fopen('svmdata.txt','w');
% disp(m1);
% str = '%d';
% for i=1:featuresize
%     str = [str ' ' num2str(i) ':%.6f'];
% end
% for i=1:length(mya)
%     fprintf(file,[str '\n'],mya(i),myd(i,:));
% end
% fclose(file);


tind = round(80/100*length(mya));


svmoptions= '-s 0 -t 2 -c 2000';
model = svmtrain(mya(1:tind),myd(1:tind,:),svmoptions);

[label,accuracy] = svmpredict(mya(tind+1:end),myd(tind+1:end,:),model);

find(label~=3);
for i=1:length(label)
    disp(['Action: ' num2str(mya(i)) ' - Label: ' num2str(label(i))]);
end



% hist(mya,40);
