UltimateSoundscaper {
	
	var window;
	
	var numChannels;
	
	var popUpOutputFormat, outputFormat;
	
	var sliderPosition;
	
	var btFileChooser, soundFile;
	
	*new { 
		^super.new.init;
	}

	init {
		
		numChannels = 4;
		
		this.makeGUI;
		
		window.front;

		window.onClose_({
			
		});
	}
	
	makeGUI {
		var decorator;

		window = Window("Ultimate SoundScaper", Rect(128, 64, 300, 240));
		window.view.decorator = FlowLayout(window.view.bounds, 5@5, 5@5 );
		decorator = window.view.decorator;
		
		btFileChooser = Button(window, Rect(0,0,80,30));
		
		popUpOutputFormat = PopUpMenu(window, Rect(0, 0, 80, 30));
		
		decorator.nextLine;
		
		sliderPosition = Slider2D(window, Rect(0, 0, 160,160));
		
	}
}