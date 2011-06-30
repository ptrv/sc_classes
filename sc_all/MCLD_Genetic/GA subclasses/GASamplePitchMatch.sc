/*
Server.default_(s = Server.local.boot); // ...or...
Server.default_(s = Server.internal.boot);
i = GASamplePitchMatch.new(server:s, numinds:80, groupsize:8, genomeLength:14).numTestsPerYear_(1).cullProportion_(0.1).pmutate_(0.01).chooseTemplateSoundFile;
i.templatebus.scope;
i.templatesynth;
i.trigger;
i.judgesbusses.scope;
i.loopYear = true;
i.runOneYear;
GAIsland.phenomeToGraphFunc(i.bestIndivid.phenome).postcs;
GAIsland.phenomeToGraphFunc(i.bestIndivid.phenome).plot(1);
i.playOnce(i.bestIndivid.phenome);
i.pmutate_(0.04);
i.pmutate_(0.07);
i.loopYear = false;
i.plotLogData;
i.writeData;

s.queryAllNodes;

i.playOnce(i.bestIndivid.phenome);
i.playOnce(i.worstIndivid.phenome);
*/

GASamplePitchMatch : GAIsland {

	var <templatebuf, <templatebus, <templatesynth,
	     <templatedirectout;


	*new { |groupsize = 10, genomeLength=40, server, target, addAction=\addToTail, 
					numinds=80, fftSize=128|
		^super.new(groupsize, genomeLength, server, target, addAction, numinds);
	}

	preinit {
		templatebus = Bus.audio(server, 1);
		("template bus is "++templatebus.index).postln;

		// Create the template buffer - initialise it with a small empty buffer for now
		templatebuf = Buffer.alloc(server, 400, 1);
	}
	
	postinit {
		// Create the template-playing synth - NB it MUST be on the same group as the nodproxies so it can take the same trigger
		templatesynth = Synth.new(\_ga_just_playbuf, [\bufnum, templatebuf.bufnum, 
						\out, templatebus.index], synthsgroup, \addToTail);
		("templatesynth: "++templatesynth.nodeID++" on "++templatesynth.server.name).postln;
		
		Routine({
		0.6.wait;
		templatedirectout = Monitor.new.postln.play(templatebus.index, 1, 0, 2, target, volume:0, addAction:\addToTail).postln;
		
			// Check out the judges if you like
			judges.do{|judge|
				judge.postln;
				judge.get(\templatebus,	{|val| ("judge's \\templatebus: "++val).postln});
				judge.get(\testbus,	{|val| ("judge's \\testbus: "++val).postln});
				judge.get(\out,		{|val| ("judge's \\out: "++val).postln});
				judge.get(\active,		{|val| ("judge's \\active: "++val).postln});
				judge.get(\t_reset,	{|val| ("judge's \\t_reset: "++val).postln});
			};
		}).play;
	}

	createJudgeSynth { |testbus, out, judgeindex|
		("Creating a judge. Args: testbus="++testbus++" templatebus="++templatebus.index++" out="++out).postln;
		^Synth(\_ga_judge_pitchmatch, 
						[	\testbus, testbus,
							\templatebus, templatebus.index,
							\out, out
						], judgesgroup, \addToTail);
	}
	*yearLogFilePath {
		^"/Users/dan/experiments/GASamplePitchMatch/_auto_logs_";
	}

	setTemplateSoundFile { |sndfile|
		var oldbuf;
		// Load the new sound into the buffer
		// Also re-create the templatesynth if needed
		oldbuf = templatebuf;
		
		Buffer.read(server, sndfile, action: {|newbuf|
					templatebuf = newbuf;
					oldbuf.free;
					templatesynth.set(\bufnum, templatebuf.bufnum);
				});
	}
	chooseTemplateSoundFile {
		var oldbuf;
		// Load the new sound into the buffer, after user has chosen it
		// Also re-create the templatesynth if needed
		oldbuf = templatebuf;
		
		Buffer.loadDialog(server, action: {|newbuf|
					if(newbuf.numChannels==1,
						{
							templatebuf = newbuf;
							oldbuf.free;
							templatesynth.set(\bufnum, templatebuf.bufnum);
						},
						{
							"\n---------------------------".postln;
							("Wrong number of channels! "++newbuf.numChannels).postln;
							"---------------------------\n".postln;
						}
					)
				});
	}

}
