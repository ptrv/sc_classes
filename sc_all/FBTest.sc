FBTest {
	var x, <>r;
	
	*new { arg x, r;
		^super.new.init(x,r);
	}
	
	init { arg xarg, rarg;
		x = xarg ? 0.5;
		r = rarg ? 3.1;
		// ^this
	}

	x {
		x = r * x * (1 - x);
		^x
	}

	x_ { arg value;
		x = value.clip(0,1);
	}
}

Pfb : Pattern {
	var <>r, <>lo, <>hi, <>start, <>length;
	
	*new { arg r=3.65, lo=0.0, hi=1.0,start=0.5, length=inf;
		^super.newCopyArgs(r, lo, hi, start, length);
	}

	storeArgs { ^[r, lo, hi, start, length] }

	embedInStream { arg inval;
		var loVal, hiVal, rVal;
		var loStr = lo.asStream;
		var hiStr = hi.asStream;
		var rStr = r.asStream;
		var x = start ?? 0.5;

		length.value(inval).do {
			hiVal = hiStr.next(inval);
			loVal = loStr.next(inval);
			rVal = rStr.next(inval);

			if(hiVal.isNil or: {loVal.isNil}) {^inval};
			x = rVal * x* (1-x);
			inval = x.linlin(0,1,loVal, hiVal).yield;
		};
		^inval;
	}
	
}

