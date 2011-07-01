FFTScope2 {

	var <server, <bus, <target;
	var <fftsize, <rate = 0.05;
	var buffer, synth, task;
	var <win, <scopeview, <busview;
	
	*new { arg target, bus, fftsize;
		^super.new.init(target, bus, fftsize);
	}
	
	init { arg argtarget, argbus, argfftsize;
	
		
		target = argtarget ? RootNode(Server.default);
		server = target.server;
		bus = argbus ? 0;
		fftsize = argfftsize ? 512;
		
		buffer = Buffer.alloc(server, fftsize);

		task = Task { 
			loop { 
				buffer.getn(0, buffer.numFrames, { arg buf;
					var z, x;
					z = buf.clump(2).flop;
					x = hypot(z[0], z[1]);
					{ if(scopeview.notClosed) { scopeview.value = x * 0.1 } }.defer;
		
				});
				rate.wait;
			}	
		};

		this.makeGUI;
		{ this.runFFT }.defer(0.5);

		ServerTree.add(this, server); // <---------------
	}
	
	makeGUI {
		var decorator;
		
		win = Window("FFT", Rect(100, 300, buffer.numFrames * 0.5 + 10, 200) ).front;
		// decorator = win.addFlowLayout( 5@5, 5@5 );
		win.view.decorator = FlowLayout(win.view.bounds, 5@5, 5@5 );
		decorator = win.view.decorator;
		
		busview = NumberBox(win, 20 @ 20) 
			.clipLo_(0)
			.clipHi_(server.options.numControlBusChannels - 1)
			.value_(bus)
			.action_({ arg view; this.bus_(view.value) });
		
		decorator.nextLine;
		
		scopeview = MultiSliderView(win, Point(win.view.bounds.width-10, win.view.bounds.height-40 ) )
				.readOnly_(true)
				.drawLines_(true)
				.drawRects_(false)
				.indexThumbSize_(1) 
				.valueThumbSize_(1)
				.background_(Color.black)
				.colors_(Color.yellow, Color.yellow);
				
		win.onClose = { this.stopFFT };
		win.front;		
	}
	
	doOnServerTree { this.runFFT }		// <---------------
	
	runFFT {
		// start synth
		synth = { arg in=0; FFT(buffer.bufnum, In.ar(in, 1)); Out.ar(0, 0.0) }
			.play(target, addAction: \addToTail, args: [\in, bus]);
		// start task
		task.start;
	}


	stopFFT {
		task.stop;		
		synth.free;
		ServerTree.remove(this, server);	// <---------------
	}

	bus_ { arg busnum;
		bus = busnum.clip(0, server.options.numControlBusChannels - 1);
		busview.value = bus;
		synth.set(\in, bus);
	}
	

}


