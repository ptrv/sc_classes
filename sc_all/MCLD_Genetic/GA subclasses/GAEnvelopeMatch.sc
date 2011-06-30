/*
Server.default_(s = Server.local.boot); // ...or...
Server.default_(s = Server.internal.boot);
i = GAEnvelopeMatch.new(server:s, numinds:80, groupsize:8, genomeLength:14).numTestsPerYear_(1).cullProportion_(0.1).pmutate_(0.01);
i.judgesbusses.scope;
i.loopYear = true;
i.runOneYear;
GAIsland.phenomeToGraphFunc(i.bestIndivid.phenome).postcs;
GAIsland.phenomeToGraphFunc(i.bestIndivid.phenome).plot(1);
i.pmutate_(0.04);
i.pmutate_(0.07);
i.loopYear = false;
i.plotLogData;
i.writeData;

s.queryAllNodes;

i.playOnce(i.bestIndivid.phenome);
i.playOnce(i.worstIndivid.phenome);
*/

GAEnvelopeMatch : GAIsland {


	createJudgeSynth { |testbus, out, judgeindex|
		^Synth(\_ga_judge_ampenv, 
						[	\testbus, testbus,
							\out, out
						], judgesgroup, \addToTail);
	}
	*yearLogFilePath {
		^"/Users/dan/experiments/GAEnvelopeMatch/_auto_logs_";
	}

}
