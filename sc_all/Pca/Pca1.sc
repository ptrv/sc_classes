// cellular automatas  redFrik 050612

Pca1 : ListPattern {	//1 dimensional
	
	var <>rule, <>generation, <>offset, dict;
	
	*new {|list, rule= 30, generation= 0, repeats= inf, offset= 0|
		^super.new(list, repeats).rule_(rule).generation_(generation).offset_(offset);
	}
	
	storeArgs {
		^[list, rule, generation, repeats, offset];
	}
	
	embedInStream {
		var localList;
		dict= ();
		8.do{|i| dict.put(i.asBinaryDigits(3).join.asSymbol, rule.asBinaryDigits[7-i])};
		localList= list.copy.shift(0-offset);
		generation.do{|i| localList= this.prNext(localList)};
		repeats.do{
			localList.do{|cell| cell.yield};
			localList= this.prNext(localList);
		};
	}
	
	//--private
	prNext {|argList|
		^[0]++argList.slide(3, 1).clump(3).collect{|cell|
			dict[cell.join.asSymbol];
		}++[0];
	}
}

/*

//simple post example
(
	var list, ca, cell, str= "", i= 0;
	list= Array.fill(64, 0).put(32, 1);			//set initial pattern
	ca= Pca1(list, 90, 0, 32).asStream;			//rule90 with 32 repetitions
	while({(cell= ca.next).notNil}, {
		str= str++" *"[cell];
		if(i%list.size==(list.size-1), {str.postln; str= ""});
		i= i+1;
	});
)


//scroll through all 256 rules
(
	var list, doc;
	list= Array.fill(100, 0).put(50, 1);
	doc= Document("Pca1 test scroll all");
	doc.bounds_(Rect(60, 35, 700, 700));
	AppClock.play(
		Routine{
			256.do{|i|
				var ca, str;
				ca= Pca1(list, i).asStream;
				str= "";
				(50*list.size).do{|i|
					str= str++" *"[ca.next];
					if(i%list.size==(list.size-1), {str= str++"\n"});
				};
				doc.string_(str);
				0.05.wait;
			};
		};
	);
)


//slowly from chaos to order
(
	s.waitForBoot{
		SynthDef("catest", {|out= 0, freq= 400, dur= 0.1, amp= 0.5|
			var e, z;
			e= EnvGen.kr(Env.perc(0.01, dur), doneAction: 2);
			z= SinOsc.ar(freq.dup, 0, amp);
			Out.ar(0, z*e);
		}).send(s);
	};
)
(
	var list, doc, ca, fre;
	list= Array.fill(31, {2.rand}).postln;		//random pattern as init
	doc= Document("Pca1 test with sound");
	ca= Pca1(list, 30).asStream;				//use rule30
	fre= Pseq(([60, 72]).midicps, inf).asStream;
	AppClock.play(Routine{
		var cell, i= 0;
		while({(cell= ca.next).notNil}, {
			if(cell==1, {s.sendMsg(\s_new, "catest", -1, 0, 0, \freq, fre.next, \dur, 0.1)});
			doc.string_(doc.string++" *"[cell]);
			if(i%list.size==(list.size-1), {doc.string_(doc.string++"\n"); fre.reset;});
			0.05.wait;
			i= i+1;
		});
	});
)


//pen example
(
	var w, run= true, list, ca, cell, i= 0, x, y, width= 300, height= 150, scale= 2;
	w= GUI.window.new("Pca1 pen example", Rect(100, 100, width*scale, height*scale), false);
	w.view.background= Color.white;
	w.onClose_({run= false});
	list= Array.fill(width, 0).put(width/2, 1);	//random pattern as init
	ca= Pca1(list, 181).asStream;				//use rule90
	w.drawHook= {
		(width*height).do{|i|
			cell= ca.next;
			if(cell==1, {
				x= i%width*scale;
				y= i.div(width)*scale;
				GUI.pen.fillRect(Rect(x, y, scale, scale));
			});
			i= i+1;
		};
	};
	w.front;
)

*/
