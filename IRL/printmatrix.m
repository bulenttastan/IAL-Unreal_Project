function printmatrix( grid )
%PRINTGRID Summary of this function goes here
%   Detailed explanation goes here
prec = floor(log10(max(max(grid)))) + 1;

disp(upper(inputname(1)));
for i=1:size(grid,1)
    str = ' ';
    for j=1:size(grid,2)
        str = [str sprintf(['%' num2str(prec) 'd'],grid(i,j)) ' '];
    end
    disp(str);
end

end

