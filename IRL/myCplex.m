function R = myCplex( N,A,P,policy )
%CPLEX LEARNING Summary of this function goes here
tic();

Rmax=1;
lambda = .65;
gamma = .99;
cplex = Cplex('lpex1');
cplex.Model.sense = 'maximize';
cplex.addCols([ones(N,1); -lambda*ones(N,1); zeros(N,1)]);
cplex.Model.lb = [-inf*ones(2*N,1); -Rmax*ones(N,1)];
cplex.Model.ub = [inf*ones(2*N,1); Rmax*ones(N,1)];

for i=1:N
    for a=1:A
        if a==policy(i); continue; end

        Pi = (P(i,:,policy(i))-P(i,:,a))* inv(eye(N)-gamma*P(:,:,policy(i)));% * R - lambda*norm(R,1);
        
        C = zeros(1,3*N);
        C(i) = -1;
        C(2*N+1:end) = Pi;
        cplex.addRows(0,C,inf);
    end
    disp(['i: ' num2str(i)]);
end

for i=1:N
    C = zeros(1,3*N);
    C(N+i) = 1;
    C(2*N+i) = -1;
    cplex.addRows(0,C,inf);
    
    C(2*N+i) = 1;
    cplex.addRows(0,C,inf);
    disp(['i: ' num2str(i)]);
end

cplex.Param.timelimit.Cur = 300;

cplex.solve();
fprintf('Solution status = %s\n',cplex.Solution.statusstring);
fprintf('Solution value = %f\n',cplex.Solution.objval);
R = cplex.Solution.x(2*N+1:end)';
str = ['R = ' num2str(R(1))];
for i=2:length(R)
    str = [str ', ' num2str(R(i))];
end

disp(['It took ' num2str(toc) ' seconds to run CPLEX']);
disp([str ';']);