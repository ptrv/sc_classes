/*
   SoundTester.sc
   
   Created by Peter Vasil on 2010-02-15.
   Copyright 2010 Peter Vasil. All rights reserved.
*/

SoundTester : Object {
	var <>debugMode=true;
	var window, wWidth=350, wHeight=50, height=20;
	var buttons;
	var btnSoundOff;
	var outNum=8;
	var synths, running;
	*new { 
		^super.new.init;
	}
		
	init { 
		window = Window("Sound Tester", Rect(128, 64, wWidth, wHeight));
		window.view.decorator = FlowLayout(window.view.bounds);
		window.addFlowLayout; 

		SynthDef(\soundtestersynth, {|out=0, gate=1|
			var env = Linen.kr(Impulse.kr(2), 0.01, 0.6, 1.0, doneAction: 0);
			var snd = SinOsc.ar(600) * env;
			Out.ar(out, snd);
		}).store;
		
		synths = Array.new(8);
		running = Array.new(8);
		outNum.do { |i|
			synths.add(Synth("soundtestersynth", [\out, i]).run(false));
			running.add(false);			
		};
		
		buttons = Array.new(8);
		8.do { |i|
			buttons.add(Button(window.view, Rect(0,0,30,30))
						.states_([
							[i.asString, Color.white, Color.black],
							[i.asString, Color.white, Color.red]
						])
						.action_({ |b|
							if(running.at(i), {
								synths.at(i).run(false);								running.put(i, false);
							},{
								synths.at(i).run(true);								running.put(i, true);
							});
							
						});
			);
		};
		
		btnSoundOff = Button(window.view, Rect(0,0,60,30))
					.states_([
						["Sound off", Color.red, Color.white]
					])
					.action_({
						synths.do { |synth|
							synth.run(false);
						};
						running.do { |run|
							run = false;
						};
						buttons.do { |button|
							button.valueAction_(0);
						};
					});
		window.front;
		window.onClose = { 
			synths.do { |synth|
				synth.free;
			};
		};
		
	}
}

