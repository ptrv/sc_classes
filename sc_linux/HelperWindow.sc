/*
    HerperWindow.sc
    Class for a tool window.

    Author: Peter Vasil
    Date: 2011-02-08

*/
HelperWindow : Object {
    var window, w, h;
    *new {
        ^super.new.init;
    }

    init {
        w = 140;
        h = 20;

        window = Window("HelperWindow", Rect(5,Window.screenBounds.height/2-100, 150, 400));
        window.front;
        window.userCanClose_(true);
        window.alwaysOnTop_(false);
        //window.minimize;
        window.addFlowLayout;
        Button(window, Rect(0,0, w, h))
        .states_([["Edit startup.scd"]])
        // .action_({ unixCmd("emacsclient -c ~/.sclang.sc") });
        .action_({ unixCmd("emacsclient ~/.config/SuperCollider/startup.scd") });
        Button(window, Rect(0,0, w, h))
        .states_([["Help.gui"]])
        .action_({ Help.gui });
        Button(window, Rect(0,0, w, h))
        .states_([["Help.rebuildTree"]])
        .action_({ Help.rebuildTree });
        Button(window, Rect(0,0, w, h))
        .states_([["Quarks.gui"]])
        .action_({ Quarks.gui });
        // Button(window, Rect(0,0, w, h))
        // .states_([["Quarks.checkoutAll"]])
        // .action_({ Quarks.checkoutAll });
        Button(window, Rect(0,0, w, h))
        .states_([["SynthDescLib"]])
        .action_({ SynthDescLib.read.global.browse });
        Button(window, Rect(0,0, w, h))
        .states_([["List Nodes"]])
        .action_({
            "".postln;
            Server.default.name.postln;
            Server.default.queryAllNodes
        });
        Button(window, Rect(0,0, w, h))
        .states_([["SynthDescLib"]])
        .action_({ SynthDescLib.read.global.browse });
        Button(window, Rect(0,0, w, h))
        .states_([["Open scwork"]])
        .action_({ unixCmd("nautilus ~/scwork") });
        Button(window, Rect(0,0, w, h))
        .states_([["Open SuperCollider"]])
        .action_({ unixCmd("nautilus ~/.local/share/SuperCollider") });

        // Button(window, Rect(0, 0, w, h))
        // .states_([["browse class"]])
        // .action_({ ClassBrowser(EmacsDocument.currentWord({var value}))  });

        // Button(window, Rect(0,0, w, h))
        // .states_([["Post incoming osc"]])
        // .action_({ (thisProcess.recvOSCfunc = { |time, addr, msg|
        //  if(msg[0] != '/status.reply') {
        //      "time: % sender: %\nmessage: %\n".postf(time, addr, msg);
        //  }
        // }
        // );
        // });
        // Button(window, Rect(0,0, w, h))
        // .states_([["Stop posting osc"]])
        // .action_({ thisProcess.recvOSCfunc = nil; });
        // StaticText(window, Rect(0,0,w,h*4));
        // Button(window, Rect(0,0,w,h))
        // .states_([["minimize"]])
        // .action_({window.minimize});
        // Button(window, Rect(0, 0, w, h))
        // .states_([["close"]])
        // .action_({ window.close });
        // Button(window, Rect(0,0,w,h))
        // .states_([["Quit SwingOSC"]])
        // .action_({SwingOSC.default.quit});
        //window.view.bounds.height.postln;
    }
    minimize {
        window.minimize;
    }
}
