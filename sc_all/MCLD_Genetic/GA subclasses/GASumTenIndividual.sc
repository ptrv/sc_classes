/*

See GASumTenIsland for details

*/

GASumTenIndividual : GAIndividual {

	phenome {
		// The K2A is to ensure an audio-rate graph for the NodeProxys
		// although it's obviously ludicrously inefficient in this case.
		// Ordinarily you'd produce genuinely audio-rate output!
		^("K2A.ar("++genome.sum++")");
	}

} // End of GASumTenIndividual class
