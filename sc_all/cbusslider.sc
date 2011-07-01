CBusSlider {
	var <index, <spec;
	var <server, <bus, <value;
	var <win, sliderview, busview, valueview;

	*new { arg server, index=0, spec;
		^super.new.init(server, index, spec);
	}

	init { arg argserver, argindex, argspec;
		// wenn argserver==nil, dann Server.default:
		server = argserver ? Server.default;
		// Aufruf des eigenen setters fuer index, dort diverse Programmschritte ...
		this.index_(argindex);
		// wenn argspec==nil, dann ein default-Wertebereich von [0.0, 1.0]:
		spec = argspec ? [0,1].asSpec;
		value = spec.default;
		this.makeGui;
	}	

	makeGui {
		var decorator;
		win = Window("CBus", Rect(200, 700, 40, 220));
		decorator = win.addFlowLayout( 5@5, 5@5 );
	// 	decorator = FlowLayout(win.view.bounds, margin: 5@5, gap: 5@5)		
		valueview = StaticText(win, 30 @ 30) // 	// Rect(0,0,30,20)
			.string_(spec.default.asString)
			.align_(\left)
			.background_(Color.grey(0.8));
			
		sliderview = Slider(win, 30 @ 150) // SCSlider
			.background_(Color.grey(0.8))
			.knobColor_(Color.grey(0.3))
			.value_(spec.unmap(value))
			.step_(0.01)
			.action_({ arg view;
				// wenn Slider bewegt wird, dann dieses ausfuehren:
				this.value_(spec.map(view.value));
			});	
			
		busview = NumberBox(win, 30 @ 20)
			.value_(index)
			.action_({ arg view;
				// wenn Wert der NumberBox durch Nutzer geaendert wird, dann:
				this.index_(view.value);
			});
			
		win.front;	
	}


// so wuerde eine explizit codierte getter-Methode fuer die Variable index aussehen:
//	index { ^index } 
// dies ist aber nicht noetig wegen der Deklaration als <index (siehe oben)
	
	// der setter fuer die Variable index:
	index_ { arg value;
		index = value ? 0;
		index = index.clip(0, server.options.numControlBusChannels - 1);
		bus = Bus.new(\control, index, 1, server);
		if (busview.notNil) { busview.value = index };
	}
	
	// setter fuer die Variable value:
	value_ { arg val;
		value = spec.constrain(val);
		this.send;
		sliderview.value_(spec.unmap(value));
		valueview.string_(value.asString);
	}

	send {Ê
		bus.set(value);
	}

}

