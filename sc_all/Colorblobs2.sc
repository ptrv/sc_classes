Colorblobs2{
	var <>guis,<curloc;
	var <points;
	var <parameterDict;
	var <weightDict;
	var <defaultParDict;
	var <parKeys;
	var <currentPars;
	var <weightfactor=2;
	var <>mindist = 1;
	var weight;
	var shift, ctrl;
	var dummy;

	// GUI
	var w, v, all, tmppoint, getPoint, move;
	var colors;

	*new{ arg ... args;
		^super.new.init(args);
	}

	init{ arg args; 
		points = List.new; //coordinates of points
		parKeys = List.new; // list for GUI variables
		parameterDict = MultiLevelIdentityDictionary.new; //a point looks up its parameters
		defaultParDict = IdentityDictionary.new; //default parameters
		weightDict = IdentityDictionary.new; //weights 
		curloc = Point.new; //current location
		currentPars = IdentityDictionary.new; //current parameters

		weight = weight ? weightfactor;

	Platform.case(
		\osx,		{ ctrl= 262401; shift= 131330 },
		\linux,		{ ctrl= 262144; shift= 131072 },
		\windows, 	{ "ERROR: havent tested this on Windows, so I dont know what number shift and ctrl give in a keydownaction. insert it here (in the class) and it should work.".postcs }
	);


		parKeys.array_(args); //put the GUI variables into a list

		parKeys.do({|it| //weights
			weightDict.put(it, weight);
		});
		
		parKeys.do({|it|	//initial values
			case
			{ [GUI.slider, GUI.numberBox,GUI.button,GUI.popUpMenu, GUI.listView, GUI.knob].includes(it.class)} { 
				defaultParDict.put( it, it.value.round(0.001) );
				currentPars.put(it, it.value.round(0.001));}
			{it.class == GUI.slider2D} {  
				defaultParDict.put( it, [it.x.round(0.001), it.y.round(0.001)] );
				currentPars.put(it, [it.x.round(0.001), it.y.round(0.001)]);}
			{it.class == GUI.rangeSlider} { 
				defaultParDict.put( it, [it.lo.round(0.001), it.hi.round(0.001)] ); 
				currentPars.put(it, [it.lo.round(0.001), it.hi.round(0.001)]);}
			{it.class == GUI.soundFileView} { 
				dummy= it.selectionStart(0);
				//dummy.class.postln;
				//dummy.postln;
				defaultParDict.put( it, dummy );	
				currentPars.put(it, it.selectionStart(0));}
			{it.class == GUI.multiSliderView} { 
				defaultParDict.put( it, it.value.round(0.001) );
				currentPars.put(it, it.value.round(0.001));};		
		});
		
	
		
		//GUI


		colors= [Color.green, Color.red, Color.yellow, Color.blue, Color.new255(127, 255, 212), Color.new255(208, 32, 144), Color.new255(255, 192, 203), Color.new255(255, 105, 180), Color.new255(107, 142, 35), Color.new255(255, 165, 0)];
		
		tmppoint= 0;
		
		w= GUI.window.new("colorblobs", Rect(800, 0, 300, 300));
		v= GUI.userView.new(w, Rect(0, 0, 300, 300))
		.background_(Color.black)
		.canFocus_( false )
		.mouseDownAction_({ arg view,x,y, mod;
			this.mouseDown(view, x, y, mod);
		})
		.mouseMoveAction_({ arg view,x,y, mod;
			this.mouseMove(x, y, mod);
		})
		.mouseUpAction_({ arg view, x, y, mod;
			if (mod == ctrl, {
				case 
				{tmppoint.isNil.not} {this.addPoint(tmppoint);
					tmppoint = nil;
					view.refresh;}				
			});
		})

		.drawFunc_({
			var pt, sizeF;
			
			GUI.pen.use {  
				GUI.pen.width = 2;
				
				if (tmppoint.isNil.not {
					GUI.pen.color = Color.white;
					GUI.pen.fillOval( Rect( tmppoint.x-20, tmppoint.y-20, 40, 40));
				});
				points.do{  arg p, i; 
						
					10.do({|count|
						sizeF= ((count-10).abs)*0.1;
						GUI.pen.color = colors[i].alpha_(count*0.1);
						GUI.pen.fillOval( Rect( p.x-(sizeF*25), p.y-(sizeF*25), sizeF*50, sizeF*50));
					})
					
				};
				
				//crosshair
				GUI.pen.color = Color.white;
				GUI.pen.moveTo( curloc.translate( -8 @  0 ));
				GUI.pen.lineTo( curloc.translate( -2 @  0 ));
				GUI.pen.moveTo( curloc.translate(  0 @ (-8) ));
				GUI.pen.lineTo( curloc.translate(  0 @ (-2) ));
				GUI.pen.moveTo( curloc.translate(  2 @ 0 ));
				GUI.pen.lineTo( curloc.translate(  8 @ 0 ));
				GUI.pen.moveTo( curloc.translate(  0 @ 2 ));
				GUI.pen.lineTo( curloc.translate(  0 @ 8 ));
				
				GUI.pen.stroke;
			};
		});
		getPoint = {|xx, yy| //check within what circle the mouse is
			var val;
			points.do{|p, i|
				if(p.x >= (xx-20), {
					if (p.x <= (xx+20), {
						if(p.y >= (yy-20), {
							if (p.y <= (yy+20), { val = points[i] }); //move
						});
					});
				});
			};
			val;
		};
		
		w.front;
				
		
	}


	mouseDown{ | view,x,y, mod |
			var lastVals, newPoint, pointIndex;
			case 
			{mod == ctrl} { //ctrl (linux= 262144, osx= 262401)
				move= getPoint.value(x, y);
				if (move.isNil, { tmppoint =  x @ y;}, // add point
					{ this.updatePoint(move);}); //overwrite parameters in point
				//"potentially updating point...".postln;
				 }
			{mod == shift} {move= getPoint.value(x, y);} //shift - move the selected blob
			{mod == 0} {curloc.set(x, y); this.calculate;} // change the parameters
			{mod == 256} {curloc.set(x, y); this.calculate;}; // change the parameters osx
			view.refresh;
			//mod.postln;
	}

	mouseMove{ |  x, y, mod= 0 |
			var lastVals, newPoint, pointIndex;
			case 
			{mod == ctrl} { nil }
			{mod == shift} {  //move the selected blob
				move.isNil.not.if({
					move= getPoint.value(x, y);
					newPoint= x @ y;
					lastVals= parameterDict[move];
					parameterDict.removeAt(move);
					parameterDict.put(newPoint, lastVals);
					pointIndex= points.indexOfEqual(move);
					points.put(pointIndex, newPoint);
					//"moving a point".postln;
				});
				{v.refresh}.defer; 
			}
			{mod == 0} { 
				curloc.set(x, y); 
				this.calculate;
				v.refresh;
			}
			{mod == 256} { 
				curloc.set(x, y); 
				this.calculate;
				v.refresh;
			};
	}

	getPars{ |point|
		parKeys.do({|it|	
			case
			{ [GUI.slider, GUI.numberBox,GUI.button,GUI.popUpMenu, GUI.listView, GUI.knob].includes(it.class)} { parameterDict.put( point, it, it.value.round(0.001) );}
			{it.class == GUI.slider2D} {  parameterDict.put( point, it, [it.x.round(0.001), it.y.round(0.001)] )}
			{it.class == GUI.rangeSlider} { parameterDict.put( point, it, [it.lo.round(0.001), it.hi.round(0.001)] ) }
			{it.class == GUI.soundFileView} { parameterDict.put(point, it, it.selectionStart(0))}
			{it.class == GUI.multiSliderView} { parameterDict.put( point, it, it.value.round(0.001) );};		
		});
		//"getpars doing its thang".postln;
	}

	addPoint{ |point|
		points.add( point );
		this.getPars(point);
		//parameterDict.postln;
	}
	
	updatePoint{ |point|

		this.getPars(point);
	}

	weightfactor_{ |newweight|
		weightDict.keysValuesChange{ |key,value|
			if ( value == weightfactor,
				{ newweight },
				{ value });
		};
	}

	calculate{
		var distances,invdistsum,thispoint;
		thispoint = points.detect( { |it| it.dist( curloc) < mindist } );
		if ( thispoint.isNil, {
			distances = points.collect{ |it,i| it.dist( curloc ) };
			currentPars.keysValuesChange( { |key,value| 
				invdistsum = distances.sum( { |it| 1/pow(it,weightDict.at( key )) } );
				defaultParDict.at( key ) + (points.sum( { |point,i| (parameterDict.at( point, key )-defaultParDict.at( key ))/pow(distances[i],weightDict.at( key ) ) } ) / invdistsum );
			} );
			//parKeys.do({|it|	{it.valueAction= currentPars[it]}.defer; });			
			this.setPars;
		},{
			//"notnil".postln;
			currentPars = parameterDict.at( thispoint );
			//parKeys.do({|it|	{it.valueAction= currentPars[it]}.defer; });
			this.setPars;
		});
	}

	setPars{
		parKeys.do({|it|	
			case
			{ [GUI.slider, GUI.knob].includes(it.class) } { {it.valueAction= currentPars[it]}.defer;}
			{ it.class == GUI.numberBox } { {it.valueAction_(currentPars[it])}.defer;}
			{ [GUI.button,GUI.popUpMenu, GUI.listView].includes(it.class)} { {it.valueAction= (currentPars[it]).round}.defer;}
			{it.class == GUI.slider2D} { {it.activex_(currentPars[it][0].round(0.001)); it.activey_(currentPars[it][1].round(0.001)); }.defer; }
			{it.class == GUI.rangeSlider} { 
				{it.activeLo_(currentPars[it][0].round(0.001)); it.activeHi_(currentPars[it][1].round(0.001)); }.defer; }
			{it.class == GUI.soundFileView} {  { it.setSelectionStart(0, currentPars[it]); it.mouseUpAction.value}.defer; }
			{it.class == GUI.multiSliderView} { {it.value_(currentPars[it].round(0.01)).doAction}.defer;};		
		});
	}

	//Load and Save Functions
	//..to do: a way to save into the document like MacicPreset
	save{|path|
		var saveList, savePoints;
		saveList= List.new;

		points.do({ |it, i|
			var values;
			values= parKeys.collect({ |it, iteration| //get values from the identity dictionary
				parameterDict.at(points[i])[parKeys[iteration]];
			});
			values.postln;
			"preset saved".postln;
			saveList.add([points[i], values]) //store them orderly
		});
		saveList= [points, saveList]; //store points in two places for easy retrieval
		(saveList).writeArchive(path);
	}
	
	load{|paths|
		var values, saveList;
		saveList= Object.readArchive(paths);
		saveList.postcs;
		points.array_(saveList[0]);
		values= saveList[1];     //[[point, [values]], [point, [values]]]
		"preset loaded".postln;
		points.do({|itPoint, iPoint| 
			parKeys.do({|itPar, iPar|		
				parameterDict.put( itPoint, itPar, values[iPoint][1][iPar] )
			});
		});

	v.refresh;
	}

	add{ arg ... args;
		var newParKeys;
		newParKeys = args; //put the GUI variables into a list
		
		newParKeys.do({|it| 
			weightDict.put(it, weight);
			parKeys.add(it);
		});

		newParKeys.do({|itPar, iPar|	//set initial values 4 each new GUI
			case
			{ [GUI.slider, GUI.numberBox,GUI.button,GUI.popUpMenu, GUI.listView, GUI.knob].includes(itPar.class)} { 
				defaultParDict.put( itPar, itPar.value.round(0.001) );
				currentPars.put(itPar, itPar.value.round(0.001));}
			{itPar.class == GUI.slider2D} {  
				defaultParDict.put( itPar, [itPar.x.round(0.001), itPar.y.round(0.001)] );
				currentPars.put(itPar, [itPar.x.round(0.001), itPar.y.round(0.001)]);}
			{itPar.class == GUI.rangeSlider} { 
				defaultParDict.put( itPar, [itPar.lo.round(0.001), itPar.hi.round(0.001)] ); 
				currentPars.put(itPar, [itPar.lo.round(0.001), itPar.hi.round(0.001)]);}
			{itPar.class == GUI.soundFileView} { 
				dummy= itPar.selectionStart(0);
				//dummy.postln;
				//dummy.class.postln;
				defaultParDict.put( itPar, dummy);	
				currentPars.put(itPar, dummy);}
			{itPar.class == GUI.multiSliderView} { 
				defaultParDict.put( itPar, itPar.value.round(0.001) );
				currentPars.put(itPar, itPar.value.round(0.001));}		
		});
		
		points.do({|itPoint, iPoint| 
			
			newParKeys.do({|itPar, iPar|	//set initial values 4 each new GUI
				case
				{ [GUI.slider, GUI.numberBox,GUI.button,GUI.popUpMenu, GUI.listView, GUI.knob].includes(itPar.class)} { 
					parameterDict.put( itPoint, itPar, itPar.value.round(0.001) );}
				{itPar.class == GUI.slider2D} {
					parameterDict.put( itPoint, itPar, [itPar.x.round(0.001), itPar.y.round(0.001)] );}
				{itPar.class == GUI.rangeSlider} { 
					parameterDict.put( itPoint, itPar, [itPar.lo.round(0.001), itPar.hi.round(0.001)] );}
				{itPar.class == GUI.soundFileView} { 
					dummy= itPar.selectionStart(0);
					//dummy.postln;
					//dummy.class.postln;
					parameterDict.put( itPoint, itPar, dummy);
					//("sfv: "++parameterDict.at(itPar)).postln;
				}
				{itPar.class == GUI.multiSliderView} { 		
					parameterDict.put( itPoint, itPar, itPar.value.round(0.001) );};
			});
		});
	}
	
	
	print{
	parameterDict.postcs;
	}
	printDefaults{
		"defaultParDict:"+defaultParDict.postcs;
		"parKeys:"+parKeys.postcs;
	}

}