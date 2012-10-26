%_________________________________________________

% endeavored by Muhammad Shoaib Sehgal

% Example file how to integrate weka in matlab

% Date cerated: 14/3/08

% Version : 1.0

% Updated:

%_________________________________________________

%Note: You can either set WEKA_HOME as an environment variable or pass
% absolute path of weka.jar to javaaddpath. For example,
% javaaddpath('c:\weka\bin\weka.jar');

javaaddpath('WEKA_HOME/weka.jar');

clear all
clc

import weka.classifiers.Classifier
import weka.classifiers.bayes.BayesNet
import weka.classifiers.Evaluation;

% calling classifier from matlab
v1 = java.lang.String('-t');
%v2 = java.lang.String('D:\Shoaib\BioMANTA\bioManta code\classification\loc_nlboost\bn\loc_trDt.csv');
v2 = java.lang.String('WEKA_HOME\trainData.arff');

v3 = java.lang.String('-T');
v4 = java.lang.String('WEKA_HOME\testDATA.arff');

prm = cat(1,v1,v2,v3,v4);

Evaluation.evaluateModel(javaObject('weka.classifiers.bayes.BayesNet'),prm);
