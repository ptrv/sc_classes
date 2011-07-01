
// PointsView : ViewRedirect { 

// 	*key { ^\pointsView }
	
// 	*initClass {
// 		StartUp.add({
// 			var scheme;
			
// 			scheme = GUI.get( \cocoa );
// 			if( scheme.notNil, {scheme.put( \pointsView, SCPointsView )});
// 			scheme = GUI.get( \swing );
// 			if( scheme.notNil, {scheme.put( \pointsView, JSCPointsView )});
// 		});
// 	}
// }



// SCPointsView : SCUserView {
// 	var <>step;
// 	var points, <colors, <which, mousepoint, mousedist;
// 	var <background;
	
// 	*viewClass { ^SCUserView }
	
// 	init { arg parent, bounds;
// 		super.init(parent, bounds);
// 	//	this.relativeOrigin = true;
// 		background = Color.grey(0.1);
// 	//	this.drawFunc = { this.draw };
		
// 	}

// 	points_ { arg array;
// 		points = array.collect {Êarg point, i; Rect.aboutPoint(point * this.bounds.extent, 12, 12) }   ;
// 		this.refresh;
// 	}

// 	points {
// 		^points.collect {Êarg rect, i; rect.center / this.bounds.extent}   ;
// 	}

// 	background_ { arg color;
// 		background = color;
// 		this.refresh;
// 	}
		
// 	colors_ { arg array;
// 		colors = array;
// 		this.refresh;
// 	}
	
// 	draw {
// 		Pen.fillColor = background;
// 		Pen.fillRect(this.bounds.moveTo(0,0));			
// 		Pen.width = 1;
// 		points.do { arg c, i; 
// 			Pen.fillColor = colors[i];   
// 			Pen.fillOval(c); 
// 			Pen.fillColor = Color.black;
// 			Pen.stringCenteredIn((i+1).asString, c); 
// 		};
// 		Pen.strokeColor = Color.black;
// 		if(which.notNil) {
// 			Pen.width = 3;
// 			Pen.strokeColor = Color.white;
// 			Pen.strokeOval(points[which]);
// 		};	
	
// 		Pen.stroke;
		
// 	}

// 	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
// 		mousepoint = Point(x,y);
// 		which = nil;
// 		which = points.detectIndex { arg c, i;  c.containsPoint(mousepoint) };
// 		if(which.notNil) { mousedist = mousepoint - (points[which].origin) };
// 		this.refresh;
// 	}
	
// 	mouseMove { arg x, y, modifiers;
// 		if (which.notNil) { 
// 			points[which] = points[which].moveToPoint(Point(x,y) - mousedist);
// 			this.refresh;
// 			action.value(this, x, y, modifiers);
// 		};
		
// 	}
	
// 	mouseUp { arg x, y, modifiers;
// 		which = nil;
// 		this.refresh;	
// 	}

// /*
// 	value_ { arg val;
// 		//value = val.clip(0.0, 1.0);
// 		this.refresh;
// 	}
	
// 	value {
// 		^this.mousePosition / this.bounds.extent;
	
// 	}

// 	valueAction_ { arg val;
// 		//value = val.clip(0.0, 1.0);
// 		//action.value(this);
// 		this.refresh;
// 	}
// */

// }


// JSCPointsView : JSCUserView {
// 	var <>step;
// 	var points, <colors, <which, mousepoint, mousedist;
// 	var <background;
	
// 	*viewClass { ^JSCUserView }

// 	init { arg parent, bounds;
// 		super.init(parent, bounds);
// 	//	this.relativeOrigin = true;
// 		background = Color.grey(0.1);
// 		this.drawFunc = { this.draw };
// 	}

// 	points_ { arg array;
// 		points = array.collect {Êarg point, i; Rect.aboutPoint(point * this.bounds.extent, 12, 12) }   ;
// 		this.refresh;
// 	}

// 	points {
// 		^points.collect {Êarg rect, i; rect.center / this.bounds.extent}   ;
// 	}

// 	background_ { arg color;
// 		background = color;
// 		this.refresh;
// 	}
		
// 	colors_ { arg array;
// 		colors = array;
// 		this.refresh;
// 	}
	
// 	draw {
// 		JPen.fillColor = background;
// 		JPen.fillRect(this.bounds.moveTo(0,0));			
// 		JPen.width = 1;
// 		points.do { arg c, i; 
// 			JPen.fillColor = colors[i];   
// 			JPen.fillOval(c); 
// 			JPen.fillColor = Color.black;
// 			JPen.stringCenteredIn((i+1).asString, c); 
// 		};
// 		JPen.strokeColor = Color.black;
// 		if(which.notNil) {
// 			JPen.width = 3;
// 			JPen.strokeColor = Color.white;
// 			JPen.strokeOval(points[which]);
// 		};	
	
// 		JPen.stroke;
		
// 	}

// 	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
// 		mousepoint = Point(x,y);
// 		which = nil;
// 		which = points.detectIndex { arg c, i;  c.containsPoint(mousepoint) };
// 		if(which.notNil) { mousedist = mousepoint - (points[which].origin) };
// 		this.refresh;
// 	}
	
// 	mouseMove { arg x, y, modifiers;
// 		if (which.notNil) { 
// 			points[which] = points[which].moveToPoint(Point(x,y) - mousedist);
// 			this.refresh;
// 			action.value(this, x, y, modifiers);
// 		};
		
// 	}
	
// 	mouseUp { arg x, y, modifiers;
// 		which = nil;
// 		this.refresh;	
// 	}


// /*
// 	value_ { arg val;
// 		//value = val.clip(0.0, 1.0);
// 		this.refresh;
// 	}
	
// 	value {
// 		^this.mousePosition / this.bounds.extent;
	
// 	}

// 	valueAction_ { arg val;
// 		//value = val.clip(0.0, 1.0);
// 		//action.value(this);
// 		this.refresh;
// 	}
// */
// }



