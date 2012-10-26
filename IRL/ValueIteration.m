function [ output_args ] = ValueIteration( N,A,P,R )
% VALUE ITERATION
% Runs Reinforcement Learning value iteration to find the policy
% N : number of states
% A : number of actions
% P : transition probability matrix
% R : rewards vector
tic();
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
disp(['It took ' num2str(toc) ' seconds to run value iteration']);

str = '';
for i=1:N-1
    str = [str num2str(p(i)) ', '];
end
str = [str num2str(p(i))];
disp(['int[] policy = new int[]{' str '};']);