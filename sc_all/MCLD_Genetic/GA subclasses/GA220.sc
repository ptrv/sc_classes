/*
 Will aim for 220Hz instead of 660Hz.

Server.default_(s = Server.local.boot); // ...or...
Server.default_(s = Server.internal.boot);
i = GA220.new(server:s, numinds:80, groupsize:8, genomeLength:14).numTestsPerYear_(1).cullProportion_(0.1).pmutate_(0.05);
i.loopYear = true;
i.runOneYear;
GA220.phenomeToGraphFunc(i.bestIndivid.phenome).postcs;
i.loopYear = false;
i.plotLogData;
i.writeData;
{SinOsc.ar(220, 0, Line.kr(0.5,0,1,doneAction:2))}.play; // Let's hear a 220Hz reference...
i.playOnce(i.bestIndivid.phenome);
*/

GA220 : GAIsland {

	createJudgeSynth { |testbus, out, judgeindex|
		// The "targetpitch" judge will aim for 660Hz signals by default
		^Synth(\_ga_judge_targetpitch, 
						[	\testbus, testbus,
							\out, out,
							\targetpitch, 220
						], judgesgroup, \addToTail);
	}
	*yearLogFilePath {
		^"/Users/dan/experiments/GA220/_auto_logs_";
	}

}