// CorrelationMeterView : UserView {
// 	var <background;
// 
// 	
// 	init { arg parent, bounds;
// 		super.init(parent, bounds);
// 	//	this.relativeOrigin = true;
// 		this.background = Color.black;
// 		// this.drawFunc = { this.draw };
// 		this.refresh;
// 		
// 	}
// 
// 	background_ { arg color;
// 		background = color;
// 		this.refresh;
//  	}
// 	draw {
// 		Pen.fillColor = background;
// 		Pen.fillRect(this.bounds.moveTo(0,0));
// 		Pen.width = 1;
// 		
// 		Pen.strokeColor = Color.white;
// 		Pen.moveTo(10@10);
// 		Pen.line(10@20, 10@30);
// 		Pen.fillStroke;
// 		Pen.stroke;
// 	}		
// }

CorrelationMeter {
	var window, cmView, bus1View, bus2View;
	var memoButton;
	var freeze;
	var <server, <bus1, <bus2, <target;
	var correlationValue;
	var bufferLength = 0.5, <rate=0.5;
	var buffer1, buffer2, task;
	var synth1, synth2;
	
	*new { arg target, bus;
		^super.new.init(target, bus);
	}
	
	init { arg argtarget,argbus;
		
		target = argtarget ? RootNode(Server.default);
		server = target.server;
		bus1 = argbus ? 0;
		bus2 = bus1 + 1;

		buffer1 = Buffer.alloc(server, (server.sampleRate * 0.5));
		buffer2 = Buffer.alloc(server, (server.sampleRate * 0.5));

		task = Task({
			loop {
				
				rate.wait;
			};
		});
		this.makeGUI;
	}

	makeGUI {
		var decorator;

		window = Window("Correlation Meter", Rect(128, 64, 640, 200));
		window.view.decorator = FlowLayout(window.view.bounds, 5@5, 5@5 );
		decorator = window.view.decorator;
 		bus1View = NumberBox(window, 50 @ 30) 
			.clipLo_(0)
			.clipHi_(server.options.numControlBusChannels - 1)
			.value_(bus1)
			.action_({ // arg view; this.bus_(view.value)
			});

		bus2View = NumberBox(window, 50 @ 30) 
			.clipLo_(0)
			.clipHi_(server.options.numControlBusChannels - 1)
			.value_(bus2)
			.action_({ // arg view; this.bus_(view.value)
			});
		
		
		memoButton = ToggleButton(window, "Memo", {
			freeze = true;
		}, {
			freeze = false;
		}, false, 50, 30);
		
		decorator.nextLine;
		cmView = UserView(window,Rect(0,0, window.view.bounds.width-10,window.view.bounds.height-50))
		.drawFunc_({ |me|
			var center = me.bounds.width / 2;
			var viewHeight = me.bounds.height / 2;
			Pen.fillColor = Color.black;
			Pen.fillRect(me.bounds.moveTo(0,0));
			Pen.width = 6;
			
			Pen.strokeColor = Color.white;
			//Pen.moveTo(315@10);
			Pen.line(center@(viewHeight-20), center@(viewHeight-30));
			Pen.line(center@(viewHeight+20), center@(viewHeight+30));
			2.do { |i|
				Pen.line(center+(100 * (i+1))@(viewHeight-20), center+(100 * (i+1))@(viewHeight-30));
				Pen.line(center+(100 * (i+1))@(viewHeight+20), center+(100 * (i+1))@(viewHeight+30));
				Pen.line(center-(100 * (i+1))@(viewHeight-20), center-(100 * (i+1))@(viewHeight-30));
				Pen.line(center-(100 * (i+1))@(viewHeight+20), center-(100 * (i+1))@(viewHeight+30));
			};
			Pen.fillStroke;
			Pen.width = 2;
			2.do { |i|
				4.do { |j|
					Pen.line(center+(20*(j+1)+(i*100))@(viewHeight-20), center+(20*(j+1)+(i*100))@(viewHeight-30));
					Pen.line(center+(20*(j+1)+(i*100))@(viewHeight+20), center+(20*(j+1)+(i*100))@(viewHeight+30));
					Pen.line(center-(20*(j+1)+(i*100))@(viewHeight-20), center-(20*(j+1)+(i*100))@(viewHeight-30));
					Pen.line(center-(20*(j+1)+(i*100))@(viewHeight+20), center-(20*(j+1)+(i*100))@(viewHeight+30));
				};				
			};
			Pen.fillStroke;
			Pen.font = Font("Helvetica", 20);
			Pen.fillColor = Color.white;
			Pen.stringAtPoint("0", (center-7)@(viewHeight-60));
			Pen.stringAtPoint("0", (center-7)@(viewHeight+30));
			Pen.stringAtPoint(".5", (center-7 - 100)@(viewHeight-60));
			Pen.stringAtPoint(".5", (center-7 - 100)@(viewHeight+30));
			Pen.stringAtPoint(".5", (center-7 + 100)@(viewHeight-60));
			Pen.stringAtPoint(".5", (center-7 + 100)@(viewHeight+30));
			Pen.stringAtPoint("-1", (center-7 - 200)@(viewHeight-60));
			Pen.stringAtPoint("-1", (center-7 - 200)@(viewHeight+30));
			Pen.stringAtPoint("+1", (center-7 + 200)@(viewHeight-60));
			Pen.stringAtPoint("+1", (center-7 + 200)@(viewHeight+30));

			Pen.stroke;
			
			Pen.fillColor = Color.yellow(0.2);
			Pen.fillRect(Rect(center-8,viewHeight-10, 16, 20));
			Pen.fillColor = Color.green(0.2);
			10.do { |i|
				Pen.fillRect(Rect((center-10)+(20*(i+1)), viewHeight-10, 19,20 ));
			};
			Pen.stroke;
			Pen.fillColor = Color.red(0.2);
			10.do { |i|
				Pen.fillRect(Rect((center-10)-(20*(i+1)), viewHeight-10, 19,20 ));
			};

		});
		
		window.front;

		window.onClose_({
			this.stopCorrelationMeter;
		});

	}

	doOnServerTree { this.runCorrelationMeter }
	
	runCorrelationMeter {
	}

	stopCorrelationMeter{
	}
}
