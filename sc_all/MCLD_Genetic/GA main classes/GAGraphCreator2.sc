/* This (static) class does the conversion from a string of numbers to a graph of UGens.

It defines a default beginning and end to add to the string, which you can override
in subclasses if you wish it to be different. However, you must always make sure that
the values of adefaultnum and kdefaultnum are also altered to correspond to the number
of defaults you've introduced. In the default case, the function takes audio input as 
well as two control-rate parameters "x" and "y" (and the t_trig, which is used within
envelopes etc).

To test:
s.boot;
g = {1.0.rand}.dup(50);
f = GAGraphCreator2.defaultFunc(GAGraphCreator2.process(g)).postln.interpret;
n = f.play;
n.free;


Returns an array with TWO items:
 0: textual list of variables that must be declared
 1: graph definition


This method is better than GAGraphCreator1 because it allows for UGens to be used
as inputs to more than one other UGen, i.e. for a wider range of graphs.

*/

GAGraphCreator2 {
  classvar iter, <agens, <kgens, <egens, <mapaudio, <mapfreq, <mapfreqk,
  		adefaultpos, kdefaultpos, edefaultpos, adefaultnum, kdefaultnum;


  // This function turns the output of "*process" into something playable, largely for demo purposes
  *defaultFunc { |result|
    ^"{ |inbus, t_trig=0, x=1, y=1|
var audioin, a0, k0, k1, k2, k3, k4"++result[0]++";
// Default inputs:
audioin = AudioIn.ar([1,2]).mean;
a0 = (audioin * 0.85).clip2(0.85);
k0 = ((A2K.kr(ZeroCrossing.ar(audioin)) * 0.02).log10 - 1).clip2(1);
k1 = Amplitude.kr(audioin);
k2 = MouseX.kr(-1,1);//x;
k3 = MouseY.kr(-1,1);//y;
k4 = t_trig;
// Genetically-defined nodes:
"
++result[1]++
"
}";
  }


  // Class initialisation - define the UGens to use
  *initClass {

  // First an array of audio-rate UGens, and an array of control-rate UGens, 
  // each of which describes how to plug things into them.

  // All UGens should be MONO, and CPU-gentle (since dozens and dozens will be used).
  // Having done some benchmarking it looks like the following are too CPU-heavy:
  // 	DO NOT USE Pulse, Saw, Blip, FSinOsc, Gendy2 or Gendy1 :(
  // 	FSinOsc is more efficient than SinOsc but unfortunately blows up and destroys the output.

  // Each entry contains two elements - UGen name, and an array of inputs.
  // The inputs define what goes in (either a static number, or dynamic [rate, remappingexpression])
  // All inputs are to be in range +-1 and will be scaled by the mul and add values

     // To scale an input to a good range of audible freqs: "*5000+5020"? or "+1**2*2500"? Can it be done more efficiently?
	mapfreq =  ["10**(", \e, "+1)*50"]; // ["e","10**(","+1)*50"];
	mapfreqk = ["10**(", \k, "+1)*50"]; // ["e","10**(","+1)*50"];

	// Reminders:
	//   - NEVER use a symbol unless it's one of our \e, \i, \k, \a
	//   - ALWAYS put a space after a mathematical operator. SC understands "1+ -2" but not "1+-2"
	//   - Although \k or \a inputs are typically supplied a signal varying between -1 and +1, 
	//       the \i inputs take directly from the genome, and these vary between 0 and 1.
	
	
	agens = [
		// Oscillators - would be nice to be able to use AmpComp to avoid high-frequency sounds being too piercing
		["SinOsc.ar(", mapfreq, ", 0, ", \e, \a, ")"].flatten,
		["LFPulse.ar(",  mapfreq, ", 0, ", \k, "*0.5+0.5,", \e, \a  , ")"].flatten,
		// NB I *think* LFSaw has blowup problems with an a-rate first argument. Maybe other UGens (LFPulse?) will exhibit the same.
		["LFSaw.ar(",  mapfreqk, ",0,", \e, \a  , ")"].flatten,
		["WhiteNoise.ar(",  \e, \a, ")"].flatten,
		["PinkNoise.ar(",  \e, \a, ")"].flatten,
		["Dust2.ar(",  \k, \e, \a, ")"].flatten,
				//BLOWUP PROBS :(		["FSinOsc.ar(",  mapfreq, 0, \e, \a  , ")"].flatten,
				//CPU-LOAD PROBS :(		["Gendy1.ar(1, 1, 1, 1, 10**(", \e,"+1)*40,10**(", \e,"+1)*60)"].flatten,
		
		// Filters
		["MidEQ.ar(",  \a, ",", mapfreqk, ",", \k,"*0.5 + 1,", \k, "*10)"].flatten,
		["BPF.ar(",  \a, ",", mapfreqk, ",", \k, "*0.5 + 1,", \e, \a  , ")"].flatten,
		["RLPF.ar(",  \a, ",", mapfreqk, ",", \k, "*0.5 + 1,", \e, \a  , ")"].flatten,
		["LPF.ar(",  \a, ",", mapfreqk, ",", \e, \a  , ")"].flatten,
		["RHPF.ar(",  \a, ",", mapfreqk, ",", \k, "*0.5 + 1,", \e, \a  , ")"].flatten,
		["HPF.ar(",  \a, ",", mapfreqk, ",", \e, \a  , ")"].flatten,
		["XFade2.ar(",  \a, \a, \k  , ")"].flatten,

		["CombN.ar(",  \a, ",0.5,", \k,"+1.1*0.2,", \k,"*2.0,", \e, \a  , ")"].flatten,
		["AllpassN.ar(",  \a, ",0.5,", \k,"+1.1*0.2,", \k,"*2.0,", \e, \a  , ")"].flatten,
		// I suspect the Spring UGen of involvement in blowups
		//["Spring.ar(", \e, \k,"+1.1*10,", \k,"+1.1*0.05)"].flatten,
		["Ringz.ar(",  \a, ",", mapfreq, ",", \k, "+1.01,", \e, \a  , ")"].flatten,
		["Formant.ar(",  mapfreq, ",", mapfreq, ",", \k,"+1*250,", \e, \a  , ")"].flatten,

		// Noisys
		["CuspN.ar(",  \k,"+1.2*100, 1, 1.9, 0, ", \e, \a, ")"].flatten,
		["HenonN.ar(",  \k,"+1.2*100, 1.4, 0.3, 0, 0,", \e, \a, ")"].flatten,
		["LinCongN.ar(",  \k,"*1e4+1e4,", \k,"*0.5+1.4,", \k,"*0.1+0.1,", \k  , ")"].flatten,
		["Crackle.ar(", \k, "+1*0.5)"].flatten,

		// Simple mathematical manipulations
		[\a, " * ", \a].flatten,
		[\a, " + ", \a].flatten,
		[\a, ".round(", \i, ")"].flatten,
		["(", \a, "+1).wrap(-1, 1)"].flatten,
		["((", \i, "*", \a, ") + (", \i, "*", \a, "))"].flatten,
		
		// Choosing
		["if(", \k, ">0, ", \a, \a, ")"].flatten,

		// Envelopes
		["EnvGen.ar(Env.perc(0.1* ",   \i, \i, \i, \i,     "*8-4), ", \t, ")"].flatten,
		["EnvGen.ar(Env.perc(",       \i, \i, \i, \i,     "*8-4), ", \t, ")"].flatten,
		["EnvGen.ar(Env.linen(",      \i, \i, \i, \i, \i, "*8-4), ", \t, ")"].flatten,
		["Decay2.ar(", \a, \i, "*0.1,", \i, ")"].flatten,
		
		// Delay
		["DelayN.ar(", \a, ", 1, ", \i, ".abs,", \k, \a, ")"].flatten,
		];

	kgens = [
		// Oscillations
		["LFPar.kr(1.01+ ",  \k, ", 0,",  \k, \k  ,")"].flatten, // V slow range
		["LFPar.kr(", \k, "*50+50.1, 0, ", \k, \k  ,")"].flatten, // Middling range
		["LFPulse.kr(", \k, "*50+50.1, 0, ", \k,"*0.5+0.5,", \k, \k  ,")"].flatten,
		["VarSaw.kr(", \k, "*50+50.1, 0,", \k,"*0.5+0.5, ", \k, \k  ,")"].flatten,

		// Noisys
		["LFNoise0.kr(",  \k, \k, \k, ")"].flatten,
		["LFNoise1.kr(",  \k, \k, \k, ")"].flatten,
		["LFNoise2.kr(",  \k, \k, \k, ")"].flatten,

		// Simple mathematical manipulations
		[\k, " * ", \k].flatten,
		[\k, " + ", \k].flatten,
		["0 - ", \k].flatten,
		["(", \k, ").round(0.05)"  ].flatten,
		["(", \k, ").round(0.1)"  ].flatten,
		["(", \k, ").round(0.2)"  ].flatten,
		["(", \k, "+1).wrap(-1, 1)"  ].flatten,
		["(", \k, "*5).wrap(-1, 1)"  ].flatten,
		["(", \k, "*2).fold(-1, 1)"  ].flatten,
		["((", \i, "*", \k, ") + (", \i, "*", \k, "))"].flatten,
		
		// Choosing
		["if(", \k, ">0, ", \k, \k, ")"].flatten,
		
		// Demand-rate stuff - sequences of numbers called from triggers
		["Demand.kr(", \k, ", 0, Dseq([", \i, \i, \i, "], inf))*2-1"].flatten,
		["Demand.kr(", \k, ", 0, Dseq([", \i, \i, \i, \i, "], inf))*2-1"].flatten,
		["Demand.kr(", \k, ", 0, Dseq([", \i, \i, \i, \i, \i, \i, "], inf))*2-1"].flatten,

		["Demand.kr(", \k, ", 0, Drand([", \i, \i, \i, "], inf))*2-1"].flatten,
		["Demand.kr(", \k, ", 0, Drand([", \i, \i, \i, \i, "], inf))*2-1"].flatten,
		["Demand.kr(", \k, ", 0, Drand([", \i, \i, \i, \i, \i, \i, "], inf))*2-1"].flatten,
		
		// Envelopes
		["EnvGen.kr(Env.perc(0.1* ",   \i, \i, \i, \i,     "*8-4), ", \t, ")"].flatten,
		["EnvGen.kr(Env.perc(",       \i, \i, \i, \i,     "*8-4), ", \t, ")"].flatten,
		["EnvGen.kr(Env.linen(",      \i, \i, \i, \i, \i, "*8-4), ", \t, ")"].flatten,
	   ];
    egens = agens ++ kgens; // For \e (=="either"), we use the whole gamut
    
  }


	*process { |genome, adefaultnum=1, kdefaultnum=5|
		// adefaultnum=1, kdefaultnum=4 correspond to the indices at which counting should start - i.e. influenced by 
		//   the amount of variables automatically created by the "funcBeginning" code.

		var asofar, ksofar, ret, gpos, mygen, mygennum, iskrate, varsdef, gene, pick;
		
		// The a-rate and k-rate "starting points" MUST exist
		ret = "";
		varsdef = "";
		asofar = adefaultnum; // Number corresponds to the number defined in the funcBeginning string
		ksofar = kdefaultnum; // Number corresponds to the number defined in the funcBeginning string
		gpos = 0;

         while({gpos < genome.size}, {
//"Pos 0".postln;
              // Pick the UGen type we're supposed to be using
              gene = genome[gpos];
//("Fetched gene #"++gpos++", value "++gene).postln;
//("asofar = "++asofar).postln;
//("ksofar = "++ksofar).postln;
//("genome.size is "++genome.size).postln;
			mygennum = (egens.size * gene).floor;
			mygen = egens[mygennum];
			iskrate = (mygennum >= agens.size);

//"Pos 0.5".postln;

//("\ngene["++gpos++"] picks a unit generator, and the next gen (#"++mygennum
//     ++") being "++mygen++", ret is currently:" ++ ret).postln;
//("\ngene["++gpos++"] picks a unit generator").postln;

              gpos = gpos + 1;

		        ret = ret ++ if(iskrate, "k"++ksofar, "a"++asofar) ++ " = (";
		        varsdef = varsdef ++ ", " ++ if(iskrate, "k"++ksofar, "a"++asofar);

//"Pos 1".postln;
		        mygen.do{ |thisarg, index| 
		          if(thisarg.isKindOf(Symbol), // Must ONLY use Symbols for \e, \i, \k, \a
		          {
		            if(thisarg==\t, 
		            { // We don't need to look at a gene for \t. Just fill in the trigger value.
		              ret = ret ++ "t_trig";
		            },{
		            
		              // If there are any genes left in the genome, then pick the next gene off the list.
		              // Otherwise we just stick on the final gene for the duration of this unit.
		              if(gpos != genome.size, {
		                gene = genome[gpos];
//("\ngene["++gpos++"] picks a backreference of type \\"++thisarg++"").postln;
		                gpos = gpos + 1;
		              });
//"Pos 2".postln;
		          
		              if(index!=0 && mygen[index-1].isKindOf(Symbol), 
		            	  {ret = ret ++ ", "}); // Add commas if we find Symbols next to each other
	
		              // Add reference to a previously-generated variable
		              ret = ret ++ thisarg.switch(
		            	  \a, { "a" ++ floor(asofar * gene) },
		            	  \k, { "k" ++ floor(ksofar * gene) },
		                \e, { 
		            	         pick = floor((asofar+ksofar) * gene);
		            	         if(pick >= asofar, {
		            	           "k" ++ (pick - asofar)
		            	         }, {
		            	           "a" ++ pick
		            	         })
		            	      },
		            	  \i, { gene }
		              );
//"Pos 3".postln;
		            }); // End of is-it-a-\t?
	
		          }, 
		          { // It's not one of our symbols - i.e. it's a string with a fragment of code
		            ret = ret ++ thisarg;
		          });
		        }; // End of iteration over our UGen's alleged inputs
		        ret = ret ++ ").clip2(1);\n";
//("Pos 4: gpos="++gpos).postln;
		     
		        if(iskrate, {ksofar = ksofar+1}, {asofar = asofar+1});




		}); // End of iteration over the genome
		
		
		// At the end, we need to make sure the last-created a-rate synth is the thing that is returned
		ret = ret ++ "\n//The final item to be returned will be:\na"++(asofar - 1)++"\n";
		
		^[varsdef, ret];
	}

} // End of GAGraphCreator2 class
