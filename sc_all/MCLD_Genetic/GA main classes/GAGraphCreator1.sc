/*

This (static) class does the conversion from a string of numbers to a graph of UGens.

It is DETERMINISTIC - a given series of numbers should always create the same graph.
This doesn't have to be true for all GA applications, but is a requirement in many 
situations.

The nodes in these graphs all expect values between -1 and +1, and so we use clip2(1)
to guarantee this.

Simple demonstration of the kind of output produced:

GAGraphCreator1.process({1.0.rand}.dup(14));

*/


GAGraphCreator1 {
  classvar iter, <agens, <kgens, <egens, <mapaudio, <mapfreq, <mapfreqk,
  		<adefaults, <kdefaults, <edefaults, adefaultpos, kdefaultpos, edefaultpos;

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
	//	- ALWAYS put a space after a mathematical operator. SC understands "1+ -2" but not "1+-2"
	agens = [
		// Oscillators
		["SinOsc.ar(", mapfreq, ", 0, ", \e, \a, ")"].flatten,
		["LFPulse.ar(",  mapfreq, ", 0, ", \k, "*0.5+0.5,", \e, \a  , ")"].flatten,
		// NB I *think* LFSaw has blowup problems with an a-rate first argument.
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
		["FBSineL.ar(", \k, " +1 * 1e4, ", \k, "+1 * 8 + 1, 1, 1.005, 0.7)"].flatten,

		// Noisys
		["CuspN.ar(",  \k,"+1.2*100, 1, 1.9, 0, ", \e, \a, ")"].flatten,
		["HenonN.ar(",  \k,"+1.2*100, 1.4, 0.3, 0, 0,", \e, \a, ")"].flatten,
		["LinCongN.ar(",  \k,"*1e4+1e4,", \k,"*0.5+1.4,", \k,"*0.1+0.1,", \k  , ")"].flatten,
		["Crackle.ar(", \k, "+1*0.5)"].flatten,
// I'm sick of the sound of this one ["GbmanN.ar(", mapfreqk, ")"].flatten,

		// Simple mathematical manipulations
		[\a, " * ", \a].flatten,
		[\a, ".round(", \i, ")"].flatten,
		["(", \a, "+1).wrap(-1, 1)"].flatten,
		["((", \i, "* ", \a, ") + (", \i, "* ", \a, "))"].flatten,
		
		// Envelopes
		["EnvGen.ar(Env.perc(0.1* ",   \i, \i, \i, \i,     "*8-4), t_trig)"].flatten,
		["EnvGen.ar(Env.perc(",       \i, \i, \i, \i,     "*8-4), t_trig)"].flatten,
		["EnvGen.ar(Env.linen(",      \i, \i, \i, \i, \i, "*8-4), t_trig)"].flatten,
		["Decay2.ar(", \a, \i, "*0.1,", \i, ")"].flatten,
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
		["((", \i, "* ", \k, ") + (", \i, "* ", \k, "))"].flatten,
		
		// Envelopes
		["EnvGen.ar(Env.perc(0.1* ",   \i, \i, \i, \i,     "*8-4), t_trig)"].flatten,
		["EnvGen.ar(Env.perc(",       \i, \i, \i, \i,     "*8-4), t_trig)"].flatten,
		["EnvGen.ar(Env.linen(",      \i, \i, \i, \i, \i, "*8-4), t_trig)"].flatten,
	   ];
    egens = agens ++ kgens; // For \e (=="either"), we use the whole gamut
    
    
    // If we run "off the end" of the genome, we need to fill in any remaining gaps in the node tree.
    // We need to do this in a deterministic way, so we iterate simply through these arrays.
    adefaults = [
    				"PinkNoise.ar",
    				"Dust2.ar(200, 1)"
    			];
    kdefaults = [
    				"Dust2.kr(200, 1)"
    			];
    edefaults = adefaults ++ kdefaults;
  }


	*createGraph  { |genome, mode=\a, indent=""| 
	  var ret, mygen, argtype;
	  if(iter>=genome.size, 
	    // If we've run out of codons, return one of the simple "default" ugens
	    // which has no inputs and can therefore "finish off" the loose branches of the tree
	    { switch(mode,
	    		\k, 
	          {
	            if(kdefaultpos >= kdefaults.size, {kdefaultpos = 0});
	            ret = kdefaults[kdefaultpos];
	            kdefaultpos = kdefaultpos + 1;
	          },
	    		\e, 
	          {
	            if(edefaultpos >= edefaults.size, {edefaultpos = 0});
	            ret = edefaults[edefaultpos];
	            edefaultpos = edefaultpos + 1;
	          },
	          \a,
	          {
	            if(adefaultpos >= adefaults.size, {adefaultpos = 0});
	            ret = adefaults[adefaultpos];
	            adefaultpos = adefaultpos + 1;
	          },
	          \i,
	          {
	            ret = "0.5"; // Use a fixed number rather than random, so the graph creation is deterministic
	          });
	    }, // End of the "if we've run out of genes to look at" clause
	    {
	      mygen = nil;
	      switch(mode,
	    		\k, 
	          {
	            if(genome[iter]>=0.1, 
	            {
	              ret = ((genome[iter]-0.1)*2.222222)-1;
	            }, {
	              mygen = kgens[(genome[iter]*10*kgens.size).floor]; // Choose appropriate kgen
	            });
	          },
	    		\e, 
	          {
	            mygen = egens[(genome[iter]*egens.size).floor]; // Choose appropriate egen
	          },
	          \a,
	          {
	            mygen = agens[(genome[iter]*agens.size).floor]; // Choose appropriate agen
	          },
	          \i,
	          {
	            ret = genome[iter]; // i-rate (instantiation rate == constant) just returns the genome val, i.e. 0-to-1
	          }
	        ); // End switch
	        
	        iter = iter + 1; // Move one position along the genome
	        
	        if(mygen.isNil.not, {
	          // At this point we're definitely using a UGen
		        //  so let's start to build up the SC code for a graph function
		        ret = "\n" ++ indent ++ "(";
		        mygen.do({ |thisarg, index| 
		          if(thisarg.isKindOf(Symbol), // Must ONLY use Symbols for \e, \i, \k, \a
		          {
		            if(index!=0 && mygen[index-1].isKindOf(Symbol), 
		            	{ret = ret ++ ",\n" ++ indent ++ "     "}); // Add commas if we find Symbols next to each other
	
		            argtype = thisarg;
		            ret = ret ++ this.createGraph(genome, argtype, indent++"  ");
		          }, 
		          { // It's not one of our symbols - i.e. it's a string with a fragment of code
		            ret = ret ++ thisarg;
		          });
		        }); // End of iteration over our UGen's alleged inputs
		        ret = ret ++ ").clip2(1)";
		     }); // End of are-we-wanting-a-UGengraph

	    }
	  ); // End of have-we-run-out-of-codons
	  ^ret; // Return the string which should now define a valid graph
	} // End of "createGraph" method

	// Basically this calls the "createGraph" method until the genome has been used up. (Very often only once!)
	// It also initialises some variables used by createGraph.
	*process  {|genome|
	  var strs, graphcount;
	  iter = 0;
	  graphcount = 0;
       adefaultpos = 0;
       kdefaultpos = 0;
       edefaultpos = 0;
	  strs = "[";
	  while({iter < genome.size},{ graphcount=graphcount+1; strs = strs ++ "\n{" ++ (this.createGraph(genome)) ++ "}, " });
	  strs = strs ++ "].mean";
	  ^strs;
	} // End of "process" method
} // End of class
