/*

Class representing an "island" (a population) of individuals. The class maintains references
to the individuals and co-ordinates the culling-and-mating process. It should also co-ordinate
the judgment process, fetching judgments and storing them in the individuals' fitness values;
this will be defined by the specific implementation.

The class creates two groups on the server: synthsgroup, and judgesgroup (which comes after
synthsgroup). The individuals for test should return synth graphs, which the NodeProxys will
play onto synthsgroup, and should be evaluated by judges on the judgesgroup which output their
verdicts (integrated over time) to busses.

To subclass this for your own use:
 1) In your subclass, let this class's "init" be called. You may need initialisation code of
    your own, in which case override the "preinit" and "postinit" functions.
 2) Override the createJudgeSynth(testbus, out) method to return your own judge synth, with extra 
    args if needed, etc. Remember that the judge synths should in general integrate their 
    judgments over time, with a higher value meaning a worse individual.
 3) Ensure that you override "*individualClass" to return the class of your desired GAIndividuals 
    - can stick with the default if wished, in which case the standard method of creating 
    synth graphs will definitely be used.
 4) Set *yearLogFilePath to return a path to a folder, or to nil if logging is not required.

*/


GAIsland {

	// Vars passed into constructor
	var <groupsize // How many test busses - should be a factor of the number of inds.
				 // It also specifies the number of judge synths.
	   , <genomeLength // the default when creating a new individual
	   , <server
	   , <target
	   , <addAction
	   , <channels // Number of channels to play back on
		;
	
	// Other vars
	var <inds, <judges, <judgesbusses,
	   <bestEverIndivid
	   , <worstEverIndivid
	   , <curBestFitness
	   , <indproxys // NodeProxys which will play the synths' content
	   , <curWorstFitness
	   ,  <synthsgroup, // Group holds the template synth and the individs' synths
	     <judgesgroup, // Group holds the judges, and the synths routing the individs to output
	     <templatedirectout, // Will be a Monitor
	     <currenttestgroup, // References to the ten-or-so indidivudals being tested
	     <groupiterator,
	     <overallmaxvol = 1.0,
	     <numYears = 0, // Number of "years" that have passed
	     <>loopYear = false, // Whether or not the "runOneYear" method should do multiple years
	    <>pcrossover = 0.75,
	    <>pdelete = 0.2,
	    <>pmutate = 0.05,
	    <>pduplicate = 0.2,
	    <dataoutpath, // Path to a folder which will be newly-created. 
	    <yearlogfilename,
    	     <yearlogfile, 
	     <genomePenaltyLength, // Above this length we punish long genomes - defaults to 2.5 * defaultlength
		<>cullProportion = 0.1, <>maxAge = 50, <>numTestsPerYear = 8;
	
	
	
	*new { |groupsize = 10, genomeLength=40, server, target, addAction=\addToTail, 
					numinds=80, channels=2|
		if(server.isNil, {server = Server.default});
		if(target.isNil, {target = server});
		^super.newCopyArgs(groupsize, genomeLength, server, target, addAction, channels
					).init(numinds)
	} // End *new
	


	init { |numinds=80|
	
		this.preinit;
		
		// Create the groups
		synthsgroup = Group.new(target, \addToTail);
		judgesgroup = Group.new(synthsgroup, \addAfter);
		
		//server.nodeLabels.put(synthsgroup.nodeID, "synthsgroup");
		//server.nodeLabels.put(judgesgroup.nodeID, "judgesgroup");
		
		("synthsgroup: "++synthsgroup.nodeID++" on "++synthsgroup.server.name).postln;
		("judgesgroup: "++judgesgroup.nodeID++" on "++judgesgroup.server.name).postln;
		
		if(this.class.yearLogFilePath.isNil.not, {
			this.createOutputDirectory;
			if(dataoutpath.isNil.not, {
				yearlogfilename = dataoutpath ++"/"++Date.localtime.stamp++".txt";
				yearlogfile = File.new(yearlogfilename, "w");
				if(yearlogfile.isOpen, {this.yearLogHeaders}, {
				  "Cannot open logfile:".postln;
				  yearlogfilename.postln
				});
			}, {
				"ERROR - was unable to create output directory".postln;
			});
		});
		
		// Create the population.
		inds = {this.class.individualClass.new(genomeLength, 0, server, synthsgroup, addAction)}.dup(numinds);
		
		// Put SOMETHING in the best/worst slots to avoid nils
		bestEverIndivid = inds[0];
		worstEverIndivid = inds[0];
		
		genomePenaltyLength = 2.5 * genomeLength;
		
		indproxys = Array.newClear(groupsize);
		groupsize.do{ |index|
			this.createNodeProxy(index);
		};

		this.resetGroupIterator;
		this.resetFitnesses;
		
		Task({
			0.6.wait;
			indproxys.do{|ip, index|
				ip.asTarget.moveToTail(synthsgroup.asTarget);
				//server.nodeLabels.put(ip.group.nodeID, "indproxy #"++index);
			};

			// Create the judge synths and their output busses
			judgesbusses = Bus.control(server, groupsize);
			("GAIsland: judges output busses are "++judgesbusses.index
						++" to "++(judgesbusses.index+groupsize-1)).postln;
			judges = Array.newClear(groupsize);
			groupsize.do{ |item, index|
				judges[index] = this.createJudgeSynth(indproxys[index].bus.index, judgesbusses.index + index, index);
				if(judges[index].isNil.not, { // Test is required to avoid error when no judges employed
					//server.nodeLabels.put(judges[index].nodeID, "Judge #"++index);
				});
			};

			0.8.wait; // Wait for judges to be initted

			/*
			// Check out the judges if you like
			judges.do{|judge|
				judge.postln;
				judge.get(\templatebus,	{|val| ("judge's \\templatebus: "++val).postln});
				judge.get(\testbus,	{|val| ("judge's \\testbus: "++val).postln});
				judge.get(\out,		{|val| ("judge's \\out: "++val).postln});
				judge.get(\active,		{|val| ("judge's \\active: "++val).postln});
				judge.get(\t_reset,	{|val| ("judge's \\t_reset: "++val).postln});
			};
			*/
			
			this.postinit;
			
		}).play;

	} // End init
	
	// Override these if you need extra init code in your subclasses:
	preinit  {}
	postinit {}
	
	createJudgeSynth { |testbus, out, judgeindex| // You MUST override this to return the judge synth as required for your experiment
		// The "targetpitch" judge will aim for 660Hz signals by default
		^Synth(\_ga_judge_targetpitch, 
						[	\testbus, testbus,
							\out, out
						], judgesgroup, \addToTail);
	}
	
	createNodeProxy { |index| // You may override this if you want them to be differently set up
		indproxys[index] = NodeProxy.audio(server, 1).play(index.wrap(0,channels-1));
	}
	
	trigger {
		/*
		  This function triggers the synth(s) under test,
		  as well as resetting and unpausing the judgment synth(s). 
		*/
		judgesgroup.set(\t_reset, 1, \active, 1);

		synthsgroup.set(\t_trig, 1);
		
	}
	
	pollJudges { // Poll the judgment synths in order to inform the individuals of their fitness.
		judgesbusses.getn(currenttestgroup.size, {|judgments| 
//			("Polling the judges - polled "++judgments).postln;
		    // The really crucial bit! Modify the fitness according to the judgment!
			currenttestgroup.do{ |ind, index|
				if(judgments[index].isNaN,{
					ind.blowupMessage;
					ind.fitness = inf; // Try in vain to remove the blowup ind?
				},{
					ind.fitness = ind.fitness + judgments[index];
				});
			};
		});
	}
	
	resetJudges { // Triggers the judgment synths to reset their judgments back to zero.
		judges.do{|judge|
		  judge.set(\t_reset, 1);
		};
	}

	resetGroupIterator { // Resets the references so that the next time we call "nextGroup" we get the first group.
		groupiterator = -1;
	}
	
	*phenomeToGraphFunc{ |p, trig=0| // The phenome may be used in different contexts, so the island here performs the standard final wrap-up.
		var textdef;
		textdef = "{|t_trig="++trig++"| "++p++"}";
		^textdef.interpret;
	}
	plotPhenome {|p, dur=3| // Executes the graph on the server and returns a 3-second sound sample plotted as a graph
		("{|t_trig=1| "++p++"}").interpret.plot(dur, server);
	}
	playOnce { |p,addAction=\addToTail, dur=3| // Execute a given phenome in a simple manner, on the server, for 3 seconds
		var posynth;
		posynth = ("{|t_trig=1| "++p++"}").interpret.play(server, addAction:addAction);
		Task{dur.wait; posynth.free}.play(AppClock);
	}
	
	nextGroup {
		var someWereFound = true, lower, upper;
		/*
		  Clear the "currenttestgroup" array and load it with references to the next 
		  set of individs.
		  
		  NB Return TRUE if there were some found (even if not the full desired amount).
		  Return FALSE if we found nothing - i.e. last time we were on the last group.
		*/
				
		groupiterator = groupiterator + 1;

		lower = groupiterator*groupsize;
		upper = lower + groupsize - 1;
		//("nextGroup: "++lower++" to "++upper).postln;
		
		// Load the relevant inds into currenttestgroup.
		currenttestgroup = inds.copyRange(lower, upper);
		
		// If we notice that there are no more to load, set moreToCome=false;
		if(currenttestgroup.size==0, {someWereFound = false; groupiterator=0});
		
		// Iterate over the judges, telling them each to look at the relevant ind's busses.
		// NB This is belt-and-braces - the "trigger" function also does this setting.
		currenttestgroup.do{|ind, index|
		  judges[index].set(
		  			\t_reset, 1
		  			);
		};

		indproxys.do{|indproxy, index|
			if(currenttestgroup[index].isNil,{
				indproxy.source_({0});
			}, {
				indproxy.source_(this.class.phenomeToGraphFunc(currenttestgroup[index].phenome));
			})
		};
		
		^someWereFound
		
	}
	
	cullAndMate {
		var matingpool, newchildren, scrapheap, cullamount, fitnessboundary, fitnesses, parent1, parent2, realindex;
		/*
		  This culls a proportion of the population, and refills their empty slots by randomly mating 
		  the remainder.
		  For fairness, make sure this is not called unless each individual has had an equal crack 
		  at the whip - i.e. don't execute it when some have been tested but others haven't.
		*/

		("-------------------------------------").postln;
		
		// Happy birthday!
		inds.do{|ind| ind.age = ind.age + 1};
		
		// Punish long genomes
		inds.do{|ind| if(ind.genome.size > genomePenaltyLength, {
			ind.fitness = ind.fitness + (ind.genome.size - genomePenaltyLength)
			})};
		
		
		// Initialise some zero-sized arrays for sorting the sheep from the goats
		matingpool = Array.new(inds.size);
		scrapheap = Array.new(inds.size);
		fitnesses = Array.new(inds.size);
		
		curBestFitness = inf;
		curWorstFitness = 0;
		// Now we want to put the individuals who are not too old into a mating pool
		// (putting the aged ones in the scrapheap)
		inds.do{|ind|
			if(ind.age > maxAge, {
				scrapheap.add(ind);
			}, {
				matingpool.add(ind);
				
				// Record info in bestIndivid or worstIndivid
				if(ind.fitness <  bestEverIndivid.fitness, { bestEverIndivid = ind});
				if(ind.fitness > worstEverIndivid.fitness, {worstEverIndivid = ind});
				if(ind.fitness <  curBestFitness, { curBestFitness = ind.fitness});
				if(ind.fitness > curWorstFitness, {curWorstFitness = ind.fitness});
				
				fitnesses.add(ind.fitness);
			})
		};
		
		("After age check: "++matingpool.size++" of "++inds.size++" remain").postln;
		
		// and we want to sort this mating pool by fitness.
		fitnesses.sort;
		//("Sorted fitnesses: "++fitnesses).postln;
		fitnessboundary = fitnesses[(fitnesses.size * (1.0 - cullProportion)).floor];
		("Fitnesses: min="++fitnesses[0]++", max="++fitnesses[fitnesses.size-1]++", boundary="++fitnessboundary).postln;
		
		cullamount = (inds.size * cullProportion).ceil - scrapheap.size;
		// Now, we want to cull only as many as needed to satisfy the cullProportion.
		// (putting the weak ones in the scrapheap).
		// NB "reverseDo" is needed so that the iterator doesn't get baffled by "removeAt"s
		realindex = matingpool.size;
		"Culling unfit individuals:".post;
		matingpool.reverseDo{ |ind, index|
			realindex = realindex - 1;
			//("Checking fitness #"++realindex++": "++ind.fitness++" >= "++fitnessboundary++", cullamount="++cullamount).postln;
			if((cullamount>0) && (ind.fitness >= fitnessboundary),{
				(" "++realindex).post;
				scrapheap.add(matingpool.removeAt(realindex));
				cullamount = cullamount - 1;
			})
		};
		"".postln;

		("After fitness check: "++matingpool.size++" of "++inds.size++" remain").postln;
		
		// Now we want to create the newchildren array and fill it with new individuals.
		newchildren = Array.new(inds.size - matingpool.size);
		(inds.size - matingpool.size).do{
			parent1 = parent2 = matingpool.choose;
			// Make sure to choose a different parent2
			while({ parent1 === parent2 }, {
				parent2 = matingpool.choose;
			});
			newchildren.add(mate(parent1, parent2, pcrossover, pdelete, pmutate, pduplicate));
		};
		
		("After mating: "++newchildren.size++" created").postln;

		// Now just set inds = matingpool ++ newchildren, but then shuffle to make sure there are no sorting effects
		inds = (matingpool ++ newchildren).scramble;

		("After merging the parents/children: "++inds.size++" individuals exist").postln;
		
		// Finally, make sure the scrapheap's synths (etc) are safely stopped and tidied away.
		scrapheap.do{ |ind|
			ind.release; // Not sure if this is needed, since I don't register dependents or anything.
		};
	}
	
	resetFitnesses {
		// Also reset EVERYBODY's fitness back to 0.
		// If we didn't do this there'd be a problem with newbies/oldies' incompatible fitnesses
		inds.do{|ind| ind.fitness = 0};
	}
	
	
	runOneYear { |isEvolving=true, triggerPeriod=2.5|
		/*
		  The "isEvolving" argument can be set to false to allow the group to 
		  cycle, while not changing the relative fitnesses, and not killing/mating.
		*/
	  var firstYear = true;
	  Task({while({firstYear || loopYear}, {
		
		this.resetGroupIterator;
		
		// for each group of test individs
		while{this.nextGroup}
		{
			1.2.wait; // Have to wait for the changeover
			// for the number of tests required per year
			numTestsPerYear.do
			{
				this.trigger;
		
				(triggerPeriod - 0.9).wait;
		
				if(isEvolving, {this.pollJudges});
				
				// Need to wait to make sure the polls come in (since it's asynchronous)
				0.9.wait;
				
			};
		};
		
		if(isEvolving, {this.cullAndMate});
		("Writing log line for age "++numYears).postln;
		this.yearLogLine;
		numYears = numYears + 1;
		("Island's age is now "++numYears).postln;
		this.resetFitnesses;
		
		if(server.avgCPU > 100.0, {
			("---------------------------------------------------").postln;
			("Stopping the automatic looping - gone over 100% CPU").postln;
			("---------------------------------------------------").postln;
			loopYear = false;
		});
		
		firstYear = false;
	  })}).play;
		
	} // End of runOneYear
	
	bestIndivid {
		var retindex = 0, retfitness = inf;
		inds.do{|ind, index| 
			if(ind.fitness < retfitness, {retindex = index})
		}
		^inds[retindex];
	}
	
	worstIndivid {
		var retindex = 0, retfitness = 0;
		inds.do{|ind, index| 
			if(ind.fitness > retfitness, {retindex = index})
		}
		^inds[retindex];
	}

	longestGenomeLength {
		var retlength = 0;
		inds.do{|ind, index| 
			if(ind.genome.size > retlength, {retlength = ind.genome.size})
		}
		^retlength;
	}
	shortestGenomeLength {
		var retlength = inf;
		inds.do{|ind, index| 
			if(ind.genome.size < retlength, {retlength = ind.genome.size})
		}
		^retlength;
	}
	
	indBusIndices {
		^indproxys.collect{|ip| ip.bus.index};
	}
	
	createOutputDirectory {
		if(this.class.yearLogFilePath.isNil.not, {
			dataoutpath = this.class.yearLogFilePath ++"/"++Date.localtime.stamp;
			('mkdir \"' ++ dataoutpath ++ '\"').systemCmd;
		})
	}
	
	yearLogHeaders {
		var logline;
		if(this.class.yearLogFilePath.isNil.not, {
		logline = "NumYears"
				++ "\tSecStamp"
				++ "\tBestFitness"
				++ "\tWorstFitness"
				++ "\tBestEverFitness"
				++ "\tWorstEverFitness"
				++ "\tLongestGenome"
				++ "\tShortestGenome"
				++ "\tAvgCPU"
				++ "\tPeakCPU"
				++ "\n"
				;
		yearlogfile.write(logline);
		yearlogfile.flush;
		});
	}
	yearLogLine {
		var logline;
		if(this.class.yearLogFilePath.isNil.not, {
		logline = "" ++ numYears 
				++ "\t" ++ Date.localtime.secStamp
				++ "\t" ++ this.curBestFitness 
				++ "\t" ++ this.curWorstFitness 
				++ "\t" ++ this.bestEverIndivid.fitness 
				++ "\t" ++ this.worstEverIndivid.fitness 
				++ "\t" ++ this.longestGenomeLength 
				++ "\t" ++ this.shortestGenomeLength 
				++ "\t" ++ server.avgCPU.round(0.1)
				++ "\t" ++ server.peakCPU.round(0.1)
				++ "\n"
				;
		yearlogfile.write(logline);
		logline.post;
		yearlogfile.flush;
		});
	}
	
	writeData { // Stores the GA app state to a file, and also writes out the best and worst individs' phenomes
		var file, fname;

		if(dataoutpath.isNil.not, {
//			fname = dataoutpath ++"/GAIsland_archive";
			this.writeArchive(dataoutpath ++"/GAIsland_archive_"++numYears++".sc");
			this.archivePhenome(this.bestIndivid.phenome, dataoutpath ++"/best_phenome_"++numYears++".sc");
			this.bestIndivid.genome.writeArchive(dataoutpath ++"/best_genome_"++numYears++".sc");
			this.archivePhenome(this.worstIndivid.phenome, dataoutpath ++"/worst_phenome_"++numYears++".sc");
			this.worstIndivid.genome.writeArchive(dataoutpath ++"/worst_genome_"++numYears++".sc");
		}, {
			"Logging is deactivated for this class".postln;
		});
	}
	
	archivePhenome { |p, path| // Pretty up the phenome so it's conveniently-written, then archive
		var str, file;
		str = "// Current datestamp: "++Date.localtime.stamp
			++"\n// Num years (iterations) elapsed: "++this.numYears
			++"\n\nx = {|t_trig=1| "++p++"}.play;\n\n/*\nx.free;";
		str.writeArchive(path);
		file = File.new(path, "w");
		if(file.isOpen, {
			file.write(str);
			file.close;
		}, {
			"archivePhenome() cannot open output file:".postln;
			path.postln;
		});
	}
	
	// Conveniences for plotting data from the log file
	plotLogData { |numYears = 0|
		var f;
		f = this.readLogFile;
		//this.class.plotWorstFitnesses(f, numYears);
		this.class.plotBestFitnesses(f, numYears);
	}
	
	readLogFile {
		if(yearlogfilename.isNil, {
			"Unable to read log file - filename not known. Most likely no path specified in the class.";
			^nil;
		}, {
			^TabFileReader.read(yearlogfilename);
		});
	}
	
	*plotBestFitnesses { |logfiledata, numYears = 0|
		this.plotAColumn(logfiledata, 2, "Best fitnesses", numYears);
	}

	*plotWorstFitnesses { |logfiledata, numYears = 0|
		this.plotAColumn(logfiledata, 3, "Worst fitnesses", numYears);
	}
	
	*plotAColumn { |logfiledata, colindex, title="Log file data", numYears|
		var col = logfiledata[1..].collect(_[colindex]);
		if(numYears > 0, {col = col[(col.size - numYears)..]});
		col = col.collect(_.interpret); // Convert string to number
		col.plot(title);
	}
	
	*individualClass {
		^GAIndividual;
	}
	*yearLogFilePath {// Return a folder. Log files and other data will be written with datestamped filenames. Return nil to disable logging.
		// ^nil;
		^"/Users/dan/experiments/GA01/_auto_logs_";
	}
	

}