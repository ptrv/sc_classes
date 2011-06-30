//random or fixed number of crossover points?
//scramble needed if more than 2 parents?
//rewrite better anyPoint crossover method
//http://www.genarts.com/karl/papers/siggraph91.html

RedGAGenome {
	var <>chromosome, <>fitness;
	*new {|chromosome, fitness= 0|
		^super.newCopyArgs(chromosome, fitness)
	}
	embedInStream {^chromosome.do{|item| item.yield}}
	asStream {^r{this.embedInStream}}
}

RedGA {
	classvar <>crossOverRate= 0.1, <>mutationRate= 0.001, >mutationFunc;
	*singlePointCrossOver {|a, b|			//singlepoint crossover with 2 parents
		var aSize, bSize, newA, newB, x, newFit;
		if(crossOverRate.coin, {			//decide if crossover or not
			aSize= a.chromosome.size;
			bSize= b.chromosome.size;
			x= 1.rrand(min(aSize, bSize));	//pick random crossover point
			newA= a.chromosome.copyRange(0, x-1)++b.chromosome.copyRange(x, bSize-1);
			newB= b.chromosome.copyRange(0, x-1)++a.chromosome.copyRange(x, aSize-1);
		}, {
			newA= a.chromosome;			//no crossover so children are copies of parents
			newB= b.chromosome;
		});
		newFit= a.fitness+b.fitness*0.5;		//average
		^[RedGAGenome(newA, newFit), RedGAGenome(newB, newFit)];
	}
	*multiPointCrossOver {|a, b|			//parameterised uniform crossover with 2 parents
		var newA, newB, chromoA, chromoB, childA= [], childB= [], which= true, newFit;
		newA= a.asStream;
		newB= b.asStream;
		while({							//find last gene in both chromosomes
			chromoA= newA.next;
			chromoB= newB.next;
			(chromoA.isNil and:{chromoB.isNil}).not;
		}, {
			childA= childA++if(which, chromoA, chromoB);
			childB= childB++if(which, chromoB, chromoA);
			if(crossOverRate.coin, {which= which.not});
		});
		newFit= a.fitness+b.fitness*0.5;		//average
		^[RedGAGenome(childA, newFit), RedGAGenome(childB, newFit)];
	}
	//*nPointCrossOver {|a, b|}				//maybe add later
	//*poissonCrossOver {|a, b|}
	//*equalProbability {|a, b|}			//karl simms mating method #2
	//*randomPercentage {|a, b|}			//karl simms mating method #3
	//*randomInterpolate {|a, b|}			//karl simms mating method #4
	*mutate {|a|
		mutationFunc= mutationFunc?{1.0.rand};
		a.chromosome= a.chromosome.collect{|x, i|
			if(mutationRate.coin, {mutationFunc.value(x, i)}, x);
		};
		^a								//return the mutated genome
	}
	*breedMultiPoint {|a, b| ^this.multiPointCrossOver(a, b).do{|x| this.mutate(x)}}
	*breedSinglePoint {|a, b| ^this.singlePointCrossOver(a, b).do{|x| this.mutate(x)}}
	//*breedNPoint {|a, b, n| ^this.nPointCrossOver(a, b, n).do{|x| this.mutate(x)}}
	//*breedPoisson {|a, b| ^this.poissonCrossOver(a, b).do{|x| this.mutate(x)}}
}
