/*

Subclassing of GAIsland and GAIndividual to create a GA which simply wants 
the genome (an array of floats between 0 and 1, size 14 by default) to sum 
to 10. This should be an incredibly easy problem to solve, since there are 
many ways it can be satisfied, and there are no local minima which trap us
anywhere sub-optimal - the fitness function should ramp us smoothly towards
a total of 10.0-ish. 

Note that this is such a simple example that it doesn't produce any sound.
It uses the NodeProxys-and-judgesynths framework, but this is over the top
because only constant values are going to the (audio-rate) busses!

Usage:
i = GASumTenIsland.new(s).numTestsPerYear_(1).cullProportion_(0.3).pmutate_(0.01).loopYear_(true);
i.judgesbusses.scope; // Handy if on internal server
i.runOneYear; // While loopYear is true this executes indefinitely
i.bestIndivid.phenome; // What's the best value?
i.bestIndivid.genome; // What's the best genome?
i.plotLogData(320); // Look at the fitness curve over the most recent 50 iterations ("years")
i.loopYear = false;
*/

GASumTenIsland : GAIsland {

	*new { |server|
		^super.new(genomeLength:14, server:server).initSumTen;
	}
	
	initSumTen {
		this.init; // The superclass's initialisation
	}
	
	*individualClass {
		^GASumTenIndividual;
	}
	
	*yearLogFilePath {
		^"/Users/dan/experiments/GASumTen/_auto_logs_";
	}
		
	*initClass {
		
		// We'll also create the judge synth right here
		StartUp.add{
			SynthDef(\_ga_judge_sumten, { |testbus, out=0, active=0, t_reset=0, targetvalue=10|
				var testsig, comparison, integral, freq, hasFreq;
				
				testsig = A2K.kr(In.ar(testbus, 1));
								
				comparison = (testsig - targetvalue).abs;
				
				// We want the maximum difference to equate to a slope of 1.0 per second on the integral.
				// Assuming the comparison produces 0<=x<=1, all we need do is divide by the server's control rate.
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			}).writeDefFile;

		} // End of StartUp.add

	}
	
	createJudgeSynth {|testbus, out, judgeindex|
		^Synth(\_ga_judge_sumten, 
						[	\testbus, testbus,
							\out, out
						], judgesgroup, \addToTail);
	}
	
	
} // End of GASumTenIsland class

