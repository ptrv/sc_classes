SendPeak : UGen {
	*ar { arg input, decay=0.99994, rate=30, id=42;
		SendReply.ar(Impulse.ar(rate), \peak, PeakFollower.ar(input, decay), id);
	}
	*kr { arg input, decay=0.99994, rate=30, id=42;
		SendReply.kr(Impulse.kr(rate), \peak, PeakFollower.kr(input, decay), id);
	}
}