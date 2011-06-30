GAIndividual2 : GAIndividual {
	
	var <>adefaultnum=1, <>kdefaultnum=3; // You may need to change these according to yr application
	
	calculatePhenome {
		// Phenome is a STRING which will be wrapped in "{|t_trig=0|" and "}", then interepreted.
		// Override this in subclasses to create whatever is desired, based on the genome values
		^GAGraphCreator2.process(this.genome, adefaultnum, kdefaultnum);
	}
}