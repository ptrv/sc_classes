+ SequenceableCollection {

	playNotes { arg repeats=1, dur=0.3;
		Pbind(
			\midinote, Pseq( this, repeats ),
			\dur, dur
			).play	
	}
	
	playFreqs { arg repeats=1, dur=0.3;
		Pbind(
			\freq, Pseq( this, repeats ),
			\dur, dur
			).play	
	}
	
	play { arg repeats=1, dur=0.3;
		this.playNotes(repeats, dur);
	}
	
}	