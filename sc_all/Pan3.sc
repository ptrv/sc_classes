
Pan3 : Panner {
	
	*ar { arg in, pos = 0.0, level = 1.0;
		^PanAz.ar(3, in, pos.linlin(-1, 1, -1, 0.3333333), level, 2.0, 0.5).rotate
	}
	*kr { arg in, pos = 0.0, level = 1.0;
		^PanAz.kr(3, in, pos.linlin(-1, 1, -1, 0.3333333), level, 2.0, 0.5).rotate
	}
}
