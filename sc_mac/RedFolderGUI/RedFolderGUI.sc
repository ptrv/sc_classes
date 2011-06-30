// 050125-26, 050427 /f0
// bugfixes: 061206 asArray, 071216 guified, 080907 reveal was broken, 080909 listView height fix
// 081025 replaced Font with GUI.font, clear up colours
// 090705 using view redirect classes (sc3.3.1)
// based on two old classes called RedPatchListGUI and RedSynthDefsGUI

/* -keyboard shortcuts:

	'ctrl+u' = update list
*/

/*
	quickstart:
		RedFolderGUI.new

	and put some documents in a folder called 'patches' in your main sc folder.
	with the GUI in focus, press 'ctrl+u' to scan the directory again.  your files should show.
	select and press 'enter' to open.
	
	you can set path, recursiveness and bounds...
		RedFolderGUI("Help", true, Rect(100, 200, 300, 400));
	
	
	warning... 'reveal' button only works on os x
*/

RedFolderGUI {
	
	var <window, <path, <>recursive, <allPaths, <listView;
	
	*new {|path= "patches/", recursive= false, bounds|
		^super.new.recursive_(recursive).initRedFolderGUI(path, bounds);
	}
	
	initRedFolderGUI {|argPath, bounds|
		var fnt= Font("Monaco", 9);
		var col= Color.black;
		var colBack= Color.grey(0.5, 0.8);
		path= PathName(argPath).asAbsolutePath;
		bounds= bounds ? Rect(Window.screenBounds.width-170, 41, 169, Window.screenBounds.height);//default bounds
		window= Window(path.basename, bounds, false).alpha_(0.9).front;
		window.view.background= colBack;
		Button(window, Rect(1, 1, bounds.width-2/3, 17))
			.font_(fnt)
			.states_([["loadThis", col, Color.clear]])
			.action_{if(allPaths.notEmpty, {allPaths[listView.value].load}); listView.focus};
		Button(window, Rect(bounds.width-2/3+1, 1, bounds.width-2/3, 17))
			.font_(fnt)
			.states_([["loadAll", col, Color.clear]])
			.action_{allPaths.do{|x| x.load}; listView.focus};
		Button(window, Rect((bounds.width-2/3*2)+1, 1, bounds.width-2/3, 17))
			.font_(fnt)
			.states_([["reveal", col, Color.clear]])
			.action_{
				Platform.case(
					\osx, {
						//fails if there is a ' in the filename
						var p= allPaths[listView.value];
						("osascript -e 'tell application \"Finder\" to (reveal item \""
						++p.tr($/, $:)++"\" of startup disk) activate'").unixCmd;
						//("open"+PathName(p).pathOnly).unixCmd;
					},
					{"RedFolderGUI: reveal for osx only".postln}
				);
			};
		listView= ListView(window, Rect(0, 20, bounds.width, bounds.height-24))
			.font_(fnt)
			.focus
			.background_(colBack)
			.keyDownAction_{|view, char, mod, unicode|
				//added ctrl+u for updating list
				if((mod&262144==262144)&&(unicode==21), {this.update});
				view.defaultKeyDownAction(char, mod, unicode);
			}
			.enterKeyAction_{|view|
				if(allPaths.notEmpty, {Document.open(allPaths[view.value])})
			};
		this.update;
	}
	
	update {
		var func;
		allPaths= List.new;
		if(recursive, {
			func= {|x|
				x.do{|y|
					if(PathName(y).isFolder, {
						func.value((y++"*").pathMatch);
					}, {
						allPaths.add(y);
					});
				};
			};
			func.value((path++"/*").pathMatch);
		}, {
			(path++"/*").pathMatch.do{|y|
				if(PathName(y).isFile, {
					allPaths.add(y);
				});
			};
		});
		defer{listView.items= allPaths.asArray.collect{|x| x.asString.basename}};
	}
	
	font_ {|font|
		window.view.children.do{|view| view.font= font};
		window.refresh;
	}
	
	front {
		window.front;
	}
}

