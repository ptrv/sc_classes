// Andre Bartetzki, April 2005

AudioMeter {
	classvar <>idCount = 0, <>id = 4242;
	
	var <server, <target, <addAction, synth, <active, <>oscresp, <>respfunc, <index, <size;
	var <>w, meter, clip, peak, peakval, <label, <rect, <alwaysOnTop=true;
	var <dbmax = 0.0, <dbmin = -60.0, <dbrange, <decay=60, <rate=30;
	var <autoreset = 0.0, schedfunc, resetfunc; 
	
	*new { arg index, target, addAction, point, label;
		^super.new.init(index, target, addAction, point, label);
	}	

	init { arg argindex, argtarget, argaddAction, argpoint, arglabel;

		active=false;

		target = argtarget ? Server.default;
		target = target.asTarget;
		server = target.server;
		if(server.serverRunning.not) { 
			("server '" ++ server.name ++ "' not running.\n  unable to make an AudioMeter!").warn; ^nil };
		addAction = argaddAction ? \addAfter;

		index = argindex ? [0, 1] ;  // Default bus = Stereo Output;
		if (index.isArray.not) {index = [index]};
		size = index.size;
		
		if(argpoint.notNil) 
			{ rect = Rect(argpoint.x, argpoint.y, size * 30 + 15, 230); }
			{ rect = Rect((idCount * 35) + 330, 10, size * 30 + 15, 230); };
		label = arglabel ? "";
		
		w = SCWindow(label, rect, resizable: false);
		w.front;
		w.alwaysOnTop = alwaysOnTop;
		w.view.background = Color.clear; 
		w.alpha = 1.0;
		w.onClose = { this.free; };
		w.view.keyDownAction = { arg view, char; this.keyDown(char) };

		meter = Array.new(size);
		clip = Array.new(size);
		peak = Array.new(size);
		peakval = Array.new(size);
		oscresp = Array.newClear(size);
		respfunc = Array.new(size);
		
		dbrange = dbmax - dbmin;

		this.activate(true);
		//this.autoactivate.value;

		respfunc = { arg i, x;
			meter[i].lo = (x.ampdb - dbmin) / dbrange;
			if( meter[i].lo > peakval[i]) {
				peakval[i] = meter[i].lo;
				peak[i].value = x.ampdb.round(0.1);
				};
			if(x >= 1.0) {clip[i].value = 1 };
		};
			
				
		resetfunc = { arg i;
			clip[i].value = 0;
			meter[i].hi = 1.0;	
			peakval[i] = 0.0; 
			peak[i].value = -90.0;
		};

		schedfunc = {
			if (autoreset > 0) 
				{ AppClock.sched(autoreset, 
					{ size.do({arg i; resetfunc.(i)  }); 
					  schedfunc.value;	
					  nil 
					}
				)};
		};

		index.do( { arg ix, i;
			
			SCStaticText(w, Rect( (i*30) + 10, 5, 25, 15))
				.string_(ix.asString).stringColor_(Color.black).align_(\center);
				
			clip.add(SCButton(w, Rect( (i*30) + 10, 25, 25, 10)));
			clip[i].canFocus = false;
			//clip[i].font = Font("Arial",9);
			clip[i].states = [ [" ", Color.black, Color.grey(0.5)] ,
						     [" ", Color.black, Color.red] ];
			clip[i].action = { arg view;
				resetfunc.(i);
				};
			
			meter.add(SCRangeSlider(w, Rect( (i*30) + 10, 40, 25, 160)));
			meter[i].knobColor = Color.black;
			meter[i].background = Gradient(Color.yellow, Color(0, 0.8, 0.2), \v);
			meter[i].canFocus = false;
			meter[i].hi = 1.0;
			meter[i].lo = 0.0;	
				
			peakval.add(0.0);
						
			peak.add(SCNumberBox(w, Rect( (i*30) + 10, 205, 25, 15)));
			peak[i].font = Font("Arial",11);
			peak[i].value = -90.0;
			peak[i].normalColor = Color.green;
			peak[i].background = Color.black;
						
			this.addresponder(i);
			
			resetfunc.(i);
			
			});
			
		//schedfunc.value;	
		idCount = idCount + size;
	}	
	
	
	addresponder { arg i;
		var commandpath;

		oscresp[i].remove;
		commandpath = ['/tr', synth.nodeID, id + idCount + i];
		oscresp[i].add( OSCpathResponder(server.addr, commandpath, { arg time,responder,msg;
					{ respfunc.(i, msg[3]) }.defer
				}).add );
	}
	
	
	*input { arg index, server, rect, label;  // input index 0 : same as AudioIn(1)
		var ix, sv;
		sv = server ? Server.default;
		if(index.asSymbol == \all) 
			{ ix = (0..(sv.options.numInputBusChannels-1)); }
			{ ix = index ? [0,1] };
		ix = ix + sv.options.numOutputBusChannels;
		^super.new.init( ix, sv, \addBefore, rect, label ? "in");
	}	
		
	*output  { arg index, server, rect, label;
		var ix, sv;
		sv = server ? Server.default;
		if(index.asSymbol == \all) 
			{ ix = (0..(sv.options.numOutputBusChannels-1)); }
			{ ix = index ? [0,1] };
		^super.new.init(ix, sv, \addAfter, rect, label ? "out");
	}	


	run {
		if(synth.isPlaying.not) {
			//index.dump;
			synth = SynthDef(label ++ (id + idCount), {arg decay=0.99994, rate=30;
				var p, t;
				p = PeakFollower.ar(index.collect({ arg ix; In.ar(ix, 1)}), decay);
				t = Impulse.ar(rate);
				SendTrig.ar(t, Array.series(size, id + idCount) , p);   
			}).play(target, [\decay, this.decayrate(decay), \rate, rate], addAction);

			synth.isPlaying = true;
			NodeWatcher.register(synth);
		}
	}

	autoactivate {
		if(server.serverRunning, { 
			this.run;
			index.do {arg ix, i; this.addresponder(i); };
			schedfunc.value;
			AppClock.sched(1.0, {this.autoactivate.value; nil})
		})	
	}

	activate { arg bool;
		if(server.serverRunning, { // don't do anything unless server is running
		
		if(bool, {
			if(active.not, {
				CmdPeriod.add(this);
				this.run;
				});
		}, {
			if(active, {
				synth.free;
				CmdPeriod.remove(this);
			});
		});
		active=bool;
		
		});
		^this
	}
			
	cmdPeriod {
		/*
		this.changed(\cmdPeriod);
		if(active == true, {
			CmdPeriod.remove(this);
			active = false;
			//this.active_(true);
			//fork { 1.0.wait; this.active_(true) };
		});
		*/	
		//this.run;
		
		//this.changed(\cmdPeriod);
		if(w.notNil) {
			fork { 
				0.5.wait; // wait until synth is freed
				this.run;
				//size.do({arg i; resetfunc.(i)  });
				index.do {arg ix, i; this.addresponder(i); }
			}		
		}{
			CmdPeriod.remove(this)
		};

	}

	nodeID {
		^synth.nodeID;
	}

	keyDown { arg char;
				if(char === $ ) { this.run;  ^this  };
				if(char === $c) { size.do({arg i; resetfunc.(i)  })  };
				//if(char === $.) { if(synth.isPlaying) { synth.free } };

	}

	
	decay_ { arg value;
		decay = value;
		if(synth.isPlaying) { synth.set(\decay, this.decayrate(decay);); };	}
	
	decayrate { arg value;		// value is dB per second
		^value.neg.dbamp ** server.sampleRate.reciprocal;
		}
	
	rate_ { arg value;
		rate = value;
		if(synth.isPlaying) { synth.set(\rate, rate); };	
	}

	autoreset_ { arg value;
		if( (value > 0) , 
			{ if (autoreset == 0) 
				{ autoreset = value.max(0.1); schedfunc.value }
				{ autoreset = value.max(0.1) }
			} ,
			{ autoreset = 0 }	
		);
	}

	dbmin_ { arg value;
		dbmin = value;
		dbrange = dbmax - dbmin;	
	}

	dbmax_ { arg value;
		dbmax = value;
		dbrange = dbmax - dbmin;	
	}
	
	alwaysOnTop_ { arg value;
		w.alwaysOnTop = value;
	}
	
	
	free {
		respfunc = nil;
		resetfunc = nil;
		if(synth.isPlaying) {  synth.free };
		synth = nil;
		oscresp.do({arg item, i; item.remove});
		CmdPeriod.remove(this);
		idCount = idCount - size;
	}

	quit {
		w.close;
		//this.free;
		^nil;
	}
	
	
}
