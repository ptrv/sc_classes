+ String {
	
	noteNumber {
		|oct=3|
		var index;
		index = ["c", "c#", "d", "d#", "e", "f", 
			"f#", "g", "g#", "a", "b", "h"].indexOfEqual(this);
		^index.notNil.if({
			index + (12 * (oct + 2));
		}, nil)
	}
	
}