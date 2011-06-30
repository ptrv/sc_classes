//part of RedGAPhenome.  to match UGen-range

+ SimpleNumber {
	range {|lo= 0.0, hi= 1.0|
		^this.linlin(0, 1, lo, hi);
	}
	exprange {|lo= 0.01, hi= 1.0|
		^this.linexp(0, 1, lo, hi);
	}
}
