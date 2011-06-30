/*
   SoundTester.sc
   
   Created by Peter Vasil on 2010-02-15.
   Copyright 2010 Peter Vasil. All rights reserved.
*/

SoundTester : Object {
	var <>debugMode=true;
	var window, wWidth=200, wHeight=100, height=20;
	var outNum=8;
	var synths;
	*new { 
		^super.new.init;
	}
		
	init { 
		window = Window("a gui", Rect(128, 64, wWidth, wHeight));
		window.addFlowLayout; 

		SynthDef(\testsynth, { 
			|out=0, gate=1|
			var env = Linen.kr(gate, 0.01, 1, 0.01, 2);
			var snd;
			
			snd = SinOsc.ar(200);
			snd = snd * env;
			Out.ar(out, snd);
		}).memStore;
		outNum.do{ |i|
			synths = synths.add(Synth.new("testsynth"));			
		};
		window.front;
		window.onClose = {  };
		
	}
}

