/*
Server.default_(s = Server.local.boot); // ...or...
Server.default_(s = Server.internal.boot);
i = GAVaryPitch2.new(server:s, numinds:80, groupsize:8, genomeLength:14).numTestsPerYear_(1).cullProportion_(0.1).pmutate_(0.01);
i.judgesbusses.scope;
i.loopYear = true;
i.runOneYear;
GAIsland.phenomeToGraphFunc(i.bestIndivid.phenome).postcs;
i.pmutate_(0.04);
i.loopYear = false;
i.plotLogData;
i.writeData;

s.queryAllNodes;

i.playOnce(i.bestIndivid.phenome);
i.playOnce(i.worstIndivid.phenome);
*/

GAVaryPitch : GAIsland {


	createJudgeSynth { |testbus, out, judgeindex|
		^Synth(\_ga_judge_movingpitch, 
						[	\testbus, testbus,
							\out, out
						], judgesgroup, \addToTail);
	}
	*yearLogFilePath {
		^"/Users/dan/experiments/GAVaryPitch/_auto_logs_";
	}

}

GAVaryPitch2 : GAIsland {


	createJudgeSynth { |testbus, out, judgeindex|
		^Synth(\_ga_judge_movingpitch2, 
						[	\testbus, testbus,
							\out, out
						], judgesgroup, \addToTail);
	}
	*yearLogFilePath {
		^"/Users/dan/experiments/GAVaryPitch2/_auto_logs_";
	}

}