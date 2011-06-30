/*

Simple class which provides an interface for live "interactive evolution",
typically creating droney soundscape.

You get some parameters to control via GUI (an X/Y co-ordinate and a trigger),
and you press the happy/sad buttons to influence the evolutionary pressures.

s.boot;
i = GAOwnDrone.new(server:s).cullProportion_(0.2).makeGui;

s.queryAllNodes

*/

GAOwnDrone : GAIsland {

var evolvetask, volumestask, ebbflows, window, postfx;

*new { |genomeLength=40, server, target, addAction=\addToTail, 
					numinds=20, channels=2|
	// "groupsize" is not used by this class - all individuals play at once.
	// Hence the reworking of the constructor args.
	^super.new(numinds, genomeLength, server, target, addAction, numinds, channels:channels);
}



preinit {
	
}

postinit {
	ebbflows = {1.0}.dup(indproxys.size);
	evolvetask = Task({
		loop({
			21.0.wait;
			this.cullAndMate;
			this.resetFitnesses;
			// Refresh the proxys where deaths-and-rebirths have occurred
			indproxys.do{|indproxy, index|
				if(inds[index].age==0, {
					indproxy.source_(this.class.phenomeToGraphFunc(inds[index].phenome));
					// Also reset the newbie fitnesses from the class default of zero
					inds[index].fitness = 1.0;
				});
			};
			
		});
	}, AppClock);
	
	volumestask = Task({
		loop({
			indproxys.do{|indproxy, index|
				ebbflows[index] = 1.0.rand;
				indproxy.set(\ebbflow, ebbflows[index]);
			};
			2.5.wait;
		});
	}, AppClock);
	
	// The post-fx for the overall output, according to preference (c'mon, whack a reverb on it)
	{ |amp = 1.0|
		var son;
		son = In.ar(numChannels:channels) * amp * 0.7;
		son = LPF.ar(son, 4000);
		son = FreeVerb.ar(son);
		ReplaceOut.ar(0, son);
	}.play(server, addAction:\addToTail);

}


start {			// Start the audio output and the evolutionary process
	this.resetFitnesses;
	indproxys.do{|indproxy, index|
		indproxy.fadeTime_(0.5).source_(this.class.phenomeToGraphFunc(inds[index].phenome));
	};
	evolvetask.reset;
	evolvetask.start;
	volumestask.reset;
	volumestask.start;
	
}

stop {			// Stop the audio output and the evolutionary process
	volumestask.stop;
	evolvetask.stop;
	indproxys.do{|indproxy, index|
		indproxy.source_({0});
	};
}

*phenomeToGraphFunc{ |p, trig=0| // The phenome may be used in different contexts, so the island here performs the standard final wrap-up.
	var textdef;

//("*phenomeToGraphFunc called on the following: " ++ p).postln;

	// Similar to the default in GAGraphCreator2, but with no audio input
	textdef = "{ |t_trig="++trig++", x=0.5, y=0.5, ebbflow=1.0|
var a0, k0, k1, k2"++p[0]++";
// Default inputs:
a0 = Dust.ar(5);
k0 = x;
k1 = y;
k2 = t_trig;
// Genetically-defined nodes:
"
++p[1]++
" * Slew.ar(K2A.ar(ebbflow), 1, 1);
}";		
	^textdef.interpret;
}


makeGui {
	
	var val, buts, ampSlid, xy, smileys;
	
	window = SCWindow.new("OwnDrone interactive evolution", Rect(100, 900, 360, 430));
	
	val = [4, 2, 0.5, 0.25];
	smileys = [":((", ":(", ":)", ":))"];
			
	buts = Array.newClear(15);
	for(0,3, { |i|
		var mulval;
			
		mulval = val[i];
					
		buts[i] = SCButton(window,Rect(i*85+13,10,80,25));
		buts[i].states = [
				[smileys[i],Color.black,Color.white],
						];
		buts[i].action = { |but|
				("Alter fitness by " + mulval).postln;
				this.modifyFitnesses(mulval);
				};
	});
	
	// X/Y control
	xy = SC2DSlider(window, Rect(10, 40, 340, 340)).x_(0.5).y_(0.5).action_({
		|but|
		synthsgroup.set(\x, but.x, \y, but.y);
	});

	// Trigger
	SCButton(window, Rect(285,390, 65,25)).states_([["hit"]]).action_({|but|
		this.trigger;
	});

	// Go/stop
	SCButton(window, Rect(10,390, 65,25)).states_([[">"], ["||"]]).action_({|but|
		if(but.value==0, {this.stop}, {this.start});
	});
	
	window.front;
}

modifyFitnesses { |mulval|
	// The fitness is proportional to the current volume, i.e. vol=1, full value, vol=0, no change
	ebbflows.do{ |ebbflow, index|
		// Now apply the multiplication factor
		//("mulval="+mulval+", ebbflow="+ebbflow+", fitness="+inds[index].fitness).postln;
		inds[index].fitness = inds[index].fitness * (mulval ** ebbflow);
		//(" new fitness="+inds[index].fitness).postln;
	};
}

resetFitnesses {
	inds.do{|ind| ind.fitness = 1.0};
}


trigger {			// Send a trigger signal to the group on which the synths are playing.
	synthsgroup.set(\t_trig, 1);
	// Similar to superclass but leaving judgesynths out of the picture.
}


*yearLogFilePath {		// Disable logging.
	^nil;
}
createJudgeSynth { |testbus, out, judgeindex|
	// We DON'T use judge synths here - judgment is interactive rather than automated.
	^nil;
}
*individualClass {
	^GAIndividual2;
}

}