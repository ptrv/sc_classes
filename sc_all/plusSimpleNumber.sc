+ SimpleNumber {

	noteName { ^["c", "c#", "d", "d#", "e", "f", 
		"f#", "g", "g#", "a", "b", "h"].wrapAt(this.round) 
	}

	midiOctave { ^ (this * 12.reciprocal).floor - 1 } 
	
}