/*
   OscToSynth.sc
   
   Created by Peter Vasil on 2010-02-07.
   Copyright 2010 Peter Vasil. All rights reserved.
*/


OscToSynth : Object {
	var <>debugMode=true;
	var responderNodes;
	var window, wWidth=120, wHeight=80;
	var synths;
	
	*new { |synthdefnames, msgs|
		^super.new.init(synthdefnames, msgs);
	}

	init { 
		
		window = Window("a gui", Rect(128, 64, wWidth, wHeight));
		window.addFlowLayout; 
		
		window.front;
		window.onClose = {};
	}
}

