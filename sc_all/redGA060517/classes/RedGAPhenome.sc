//what to do with iphase and other that can not be modulated?
//comment the code

RedGAPhenome {
	classvar <>specs, <>binOps, <>arGens, <>krGens;
	var <chromosome, <depth, <>env, <>defname, genes, index, maxIndex;
	*initClass {
		specs= (
			\freq: #[\exprange, 20, 20000],
			\lofreq: #[\exprange, 0.1, 20],
			\phase: #[\range, 0, 2pi],
			\phase1: #[\range, 0, 1],
			\width: #[\range, 0, 1],
			\mul: #[\range, 0, 1],
			\add: #[\range, -1, 1],
			\harmonics: #[\range, 1, 100],
			\feedback: #[\range, 0, 1],
			\iphase1: #[\range, 0, 1],
			\iphase2: #[\range, 0, 2],
			\iphase4: #[\range, 0, 4],
			\width: #[\range, 0, 1],
			\rate: #[\range, 0.1, 20],
			\depth: #[\range, 0, 0.5],
			\delay: #[\range, 0, 2],
			\onset: #[\range, 0, 1],
			\rateVar: #[\range, 0, 0.1],
			\depthVar: #[\range, 0, 0.1],
			\chaosParam: #[\range, 1, 2],
			\start: #[\range, 0, 20000],
			\end: #[\range, 0, 20000],
			\dur: #[\range, 0.01, 10],
			\lodur: #[\range, 0.01, 1],
			\in: #[\range, -1, 1]
		);
		binOps= #[
			'+', '-', '*', '/', 'mod', 'div', 'pow', 'min', 'max', 'ring1', 'ring2',
			'ring3', 'ring4', 'difsqr', 'sumsqr', 'sqrsum', 'sqrdif', 'absdif', 'thresh',
			'amclip', 'scaleneg', 'clip2', 'fold2', 'wrap2', 'excess', 'firstArg'
		];
		arGens= [
			[SinOsc, [\freq, \phase, \mul, \add]],
			[SinOscFB, [\freq, \feedback, \mul, \add]],
			[Formant, [\freq, \freq, \freq, \mul, \add]],
			[LFSaw, [\freq, \iphase2, \mul, \add]],
			[LFPar, [\freq, \iphase2, \mul, \add]],
			[LFCub, [\freq, \iphase2, \mul, \add]],
			[LFTri, [\freq, \iphase4, \mul, \add]],
			[LFPulse, [\freq, \width, \mul, \add]],
			[VarSaw, [\freq, \iphase1, \width, \mul, \add]],
			[Impulse, [\freq, \phase1, \mul, \add]],
			[SyncSaw, [\freq, \freq, \mul, \add]],
			[Vibrato, [\freq, \rate, \depth, \delay, \onset, \rateVar, \depthVar, \iphase1]],
			[WhiteNoise, [\mul, \add]],
			[BrownNoise, [\mul, \add]],
			[PinkNoise, [\mul, \add]],
			[ClipNoise, [\mul, \add]],
			[GrayNoise, [\mul, \add]],
			//[NoahNoise, [\mul, \add]],
			[Crackle, [\chaosParam, \mul, \add]],
			//[Logistic, [\chaosParam2, \freq, \mul, \add]],
			[LFNoise0, [\freq, \mul, \add]],
			[LFNoise1, [\freq, \mul, \add]],
			[LFNoise2, [\freq, \mul, \add]],
			[LFClipNoise, [\freq, \mul, \add]],
			[LFDNoise0, [\freq, \mul, \add]],
			[LFDNoise1, [\freq, \mul, \add]],
			[LFDNoise3, [\freq, \mul, \add]],
			[LFDClipNoise, [\freq, \mul, \add]],
			[Dust, [\freq, \mul, \add]],
			[Dust2, [\freq, \mul, \add]],
			[Line, [\start, \end, \dur, \mul, \add]],
			[XLine, [\start, \end, \dur, \mul, \add]],
			[FSinOsc, [\freq, \iphase1, \mul, \add]],
			[Blip, [\freq, \harmonics, \mul, \add]],
			[Saw, [\freq, \mul, \add]],
			[Pulse, [\freq, \width, \mul, \add]]
			//[PSinGrain, [\freq, \lodur, \mul, \add]]
		];
		krGens= [
			[SinOsc, [\lofreq, \phase, \mul, \add]],
			[SinOscFB, [\lofreq, \feedback, \mul, \add]],
			[LFSaw, [\lofreq, \iphase2, \mul, \add]],
			[LFPar, [\lofreq, \iphase2, \mul, \add]],
			[LFCub, [\lofreq, \iphase2, \mul, \add]],
			[LFTri, [\lofreq, \iphase4, \mul, \add]],
			[LFPulse, [\lofreq, \width, \mul, \add]],
			[VarSaw, [\lofreq, \iphase1, \width, \mul, \add]],
			[Impulse, [\lofreq, \phase1, \mul, \add]],
			[SyncSaw, [\lofreq, \lofreq, \mul, \add]],
			[Vibrato, [\lofreq, \rate, \depth, \delay, \onset, \rateVar, \depthVar, \iphase1]],
			[WhiteNoise, [\mul, \add]],
			[BrownNoise, [\mul, \add]],
			[PinkNoise, [\mul, \add]],
			[ClipNoise, [\mul, \add]],
			[GrayNoise, [\mul, \add]],
			//[NoahNoise, [\mul, \add]],
			[Crackle, [\chaosParam, \mul, \add]],
			//[Logistic, [\chaosParam2, \lofreq, \mul, \add]],
			[LFNoise0, [\lofreq, \mul, \add]],
			[LFNoise1, [\lofreq, \mul, \add]],
			[LFNoise2, [\lofreq, \mul, \add]],
			[LFClipNoise, [\lofreq, \mul, \add]],
			[LFDNoise0, [\lofreq, \mul, \add]],
			[LFDNoise1, [\lofreq, \mul, \add]],
			[LFDNoise3, [\lofreq, \mul, \add]],
			[LFDClipNoise, [\lofreq, \mul, \add]],
			[Dust, [\freq, \mul, \add]],
			[Dust2, [\freq, \mul, \add]],
			[Line, [\start, \end, \dur, \mul, \add]],
			[XLine, [\start, \end, \dur, \mul, \add]],
			[FSinOsc, [\lofreq, \iphase1, \mul, \add]]
		];
	}
	
	*new {|genome, maxDepth= 10| ^super.new.initRedGAPhenome(genome, maxDepth)}
	initRedGAPhenome {|genome, maxDepth|
		var arr= [];
		genes= genome.chromosome;
		if(genes.size<4, {"RedGAPhenome: too short genome".error; this.halt});
		depth= (genes[0]*maxDepth).round.asInteger;	//first gene is global depth
		maxIndex= genes.size-1;
		index= 1;
		while({(index+2)<=maxIndex}, {
			arr= arr.add(this.prFormatOsc(genes[index], 0, \in, \ar).asCompileString);
			if((index+3)<=maxIndex, {
				arr= arr.add(binOps[binOps.size*0.99999999*genes[index]]);
			});
			index= index+1;
		});
		chromosome= this.prAddBinOps(arr);			//save translated string chromosome
	}
	
	asDefString {									//wrap in string
		^this.prSynthDefHeaderFooter(
			"{|out= 0, amp= 1| Out.ar(out, "++chromosome++"*amp)}"
		)
	}
	asDefString2 {								//wrap in string with limiter+leakdc
		^this.prSynthDefHeaderFooter(
			"{|out= 0, amp= 1| Out.ar(out, Limiter.ar(LeakDC.ar("++chromosome++"*amp)))}"
		)
	}
	asDefString3 {								//wrap in string with limiter+leakdc and env
		var e= env ?? {Env.asr(0.01, 1, 0.02)};
		^this.prSynthDefHeaderFooter(
			"{|out= 0, amp= 1, gate= 1| Out.ar(out, Limiter.ar(LeakDC.ar("
				++chromosome++"*EnvGen.kr(#"++e.asArray++", gate, amp, 0, 1, 2))))}"
			)
	}
	
	asSynthDef {^this.asDefString.interpret}
	asSynthDef2 {^this.asDefString2.interpret}
	asSynthDef3 {^this.asDefString3.interpret}
	
	//--private
	prSynthDefHeaderFooter {|str|
		var name= defname ?? {"RedGAPhenomeDef_"++Date.getDate.stamp};
		^"SynthDef('"++name++"', "++str++")";
	}
	prAddBinOps {|arr|
		if(arr.size>2, {
			^""++arr[0]++".perform('"++arr[1]++"', "++this.prAddBinOps(arr.copyToEnd(2))++")";
		}, {
			^arr[0];
		});
	}
	prFormatOsc {|gene, level, spec, rate|
		var osc, args, min, max, daArgs;
		if(rate==\ar, {
			#osc, args= arGens[(arGens.size*0.99999999*gene).asInteger];
			if(osc.isNil, {	//debug
				"RedGAPhenome: warnAR-prFormatOsc".postln;
				arGens[(arGens.size*0.99999999*gene).asInteger].postln;
			});
		}, {
			#osc, args= krGens[(krGens.size*0.99999999*gene).asInteger];
			if(osc.isNil, {	//debug
				"RedGAPhenome: warnKR-prFormatOsc".postln;
				krGens[(krGens.size*0.99999999*gene).asInteger.postln].postln;
			});
		});
		min= genes[index+1];
		max= genes[index+2];
		if(min.round(0.000001)==max.round(0.000001), {
			//min= (min-0.1).wrap(0, 1);		//different possibility
			max= (max+0.1).wrap(0, 1);
		});
		index= index+3;
		daArgs= this.prFormatArgs(args, level+1, rate);
		^RedGAPhenomeToDefOsc(spec, osc, rate, RedGAPhenomeToDefArgs(daArgs), min, max);
	}
	prFormatArgs {|args, level, rate|
		var gene, spec, restArgs;
		if(index>maxIndex, {
			^nil;
		}, {
			gene= genes[index];
			spec= args[0];
			if(level>depth, {					//force num
				gene= gene*0.33;
			}, {
				if((index+2)>maxIndex, {		//force num
					gene= gene*0.33;
				});
			});
			if(gene<=0.33, {
				restArgs= args.copyToEnd(1);
				if(restArgs.isEmpty, {
					index= index+1;
					^RedGAPhenomeToDefNum(spec, gene/0.33);
				}, {
					index= index+1;
					^[RedGAPhenomeToDefNum(spec, gene/0.33)]
						++this.prFormatArgs(restArgs, level, rate);
				});
			}, {
				if(rate==\kr, {				//force kr
					gene= gene.linlin(0.33, 1, 0.33, 0.67);
				});
				if(gene<=0.67, {
					^this.prFormatOsc(gene-0.33/0.34, level, spec, \kr);
				}, {
					^this.prFormatOsc(gene-0.67/0.33, level, spec, \ar);
				});
			});
		});
	}
}

RedGAPhenomeToDefOsc {
	var spec, ugen, rate, args, minval, maxval;
	*new {|spec, ugen, rate, args, minval, maxval|
		^super.newCopyArgs(spec, ugen, rate, args, minval, maxval);
	}
	storeOn {|stream|
		var range, min, max, rmin, rmax;
		#range, min, max= RedGAPhenome.specs[spec]
			?? {"RedGAPhenome: spec not found - using default".warn; #[\range, 0, 1]};
		rmin= this.prMinMapping(min, max);
		rmax= this.prMaxMapping(min, max);
		if(args.isNil, {
			stream<<"%.%.%(%, %)".format(ugen, rate, range, rmin, rmax);
		}, {
			stream<<"%.%%.%(%, %)".format(ugen, rate, args.asCompileString, range, rmin, rmax);
		});
	}
	//--private
	prMinMapping {|min, max| ^minval.linlin(0, 1, min, max)}
	prMaxMapping {|min, max| ^maxval.linlin(0, 1, min, max)}
}

/*RedGAPhenomeToDefOsc2 : RedGAPhenomeToDefOsc {
	prMinMapping {|min, max| ^minval.linexp(0, 1, min.max(0.0001), max)} //will not work
	prMaxMapping {|min, max| ^maxval.explin(0.0001, 1, min, max)}
}*/

RedGAPhenomeToDefNum {
	var spec, val;
	*new {|spec, val| ^super.newCopyArgs(spec, val)}
	storeOn {|stream|
		var range, min, max;
		#range, min, max= RedGAPhenome.specs[spec]
			?? {"RedGAPhenome: spec not found - using default".warn; #[\range, 0, 1]};
		stream<<val.perform(range, min, max).asCompileString;
	}
}

RedGAPhenomeToDefArgs {
	var args;
	*new {|args| ^super.newCopyArgs(args.asArray)}
	storeOn {|stream|
		var str;
		if(args.notEmpty, {
			str= args.asCompileString;
			stream<<$\(<<str.copyRange(2, str.size-3)<<$\);
		});
	}
}


/*
//--testcode
f= RedGAPhenomeToDefNum(\freq, 0.5);
f.asCompileString
e= RedGAPhenomeToDefArgs([0.1, 0.2, 0.3, f])
e.asCompileString
d= RedGAPhenomeToDefOsc(\lofreq, Saw, \kr, e, 0.1, 0.9)
d.asCompileString
c= RedGAPhenomeToDefNum(\freq, 0.1);
c.asCompileString
b= RedGAPhenomeToDefArgs([0.1, 0.2, 0.3, c, d, f])
b.asCompileString
a= RedGAPhenomeToDefOsc(\freq, SinOsc, \ar, b, 0.1, 0.9)
a.asCompileString


s.boot;
(
q= {
	a= RedGAGenome({1.0.rand}.dup(20));
	b= RedGAPhenome(a);
	d= b.asSynthDef3.play;
	b.asDefString3.postln;
	"\nGENOME:".postln;
	a.chromosome.postcs;
	"\nPHENOME:".postln;
	b.chromosome.postcs;
	"";
};
)
q.value;
b.depth;
d.release

*/
