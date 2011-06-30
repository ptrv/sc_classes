/*

The GAIndividual represents an individual "creature" in the GA environment. Mainly it does 
only simple things: hold its genome (array of floats between 0.0 and 1.0), convert that genome 
into a phenome when requested, hold a fitness value which the outside world will modify, and
mate with another GAIndividual to produce a child with genetic heritage from the two parents.

This class knows nothing about the server etc - the GAIsland handles all that. The GAIsland is
also responsible for holding the probabilities of crossover/mutation/etc which come into play 
during mating.

*/

GAIndividual {

var <>genome, // An array of floats between 0 and 1
    <>defaultGenomeLength,
    <>age,
	// Fitness defaults to 0. It should always be non-negative; the closer to zero, the BETTER (the fitter) the individual.
    <>fitness,
    phenome // Will hold the UGen graph
    ;

	// CONSTRUCTOR
	*new { |genomeLength=20,  newAge=0|
		^super.new.init(genomeLength, newAge) 
	}
	init { |genomeLength, newAge|
	     // simple initiation stuff
	     age = newAge;
	     fitness = 0.0;

	     // Initialise the genome
	     if(genomeLength == nil, {genomeLength=defaultGenomeLength});
		genome = {1.0.rand}.dup(genomeLength);
	}

	mate { arg partner, pcrossover, pdelete, pmutate, pduplicate; // "partner" should be another GAIndividual
	  var child, randloc, pmutatepergene;
	  child = this.class.new;
	  // Initialise child's fitness, naively, as mean of parents' fitness.
	  child.fitness= (this.fitness + partner.fitness) / 2;
	  
	  // The following four genetic changes may or may not occur, independently of each other:
	  // Cross two genomes
	  if(pcrossover.coin,
	  {
		  randloc = min(this.genome.size, partner.genome.size).rand;
		  if(0.5.coin, 
		  	{child.genome = this.genome.copyRange(0, randloc - 1) 
		  		++ partner.genome.copyRange(randloc, partner.genome.size-1)},
		  	{child.genome = partner.genome.copyRange(0, randloc - 1) 
		  		++ this.genome.copyRange(randloc, this.genome.size-1)}
		    );
	  },
	  {
	  	child.genome = this.genome;
	  });
	  // Deletion
	  if(pdelete.coin,
	  {
	    randloc = child.genome.size.rand;
	    child.genome.removeAt(randloc);
	  }); 
	  // Duplication
	  if(pduplicate.coin,
	  {
	    randloc = child.genome.size.rand;
	    child.genome = child.genome.insert(randloc, child.genome[randloc]);
	  }); 
	  // Mutation
	  child.genome.do{|val, index|
	    if(pmutate.coin,{
	      child.genome[index] = (val + 1.0.sum3rand).wrap(0, 1);
	    })
	  };
	  ^child;
	} // End of mate method

     phenome {
       if(phenome == nil, {phenome = this.calculatePhenome});
       ^phenome;
     }

	calculatePhenome {
		// Phenome is a STRING which will be wrapped in "{|t_trig=0|" and "}", then interepreted.
		// Override this in subclasses to create whatever is desired, based on the genome values
		^GAGraphCreator1.process(this.genome);
	}

} // End of class