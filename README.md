SuperCollider classes
==========

A collection of more or less useful classes.


## HerlperWindow ##

Small window with shortcut buttons for often used commands.

    GUI.swing;
    g = SwingOSC.default
    g.waitForBoot({
        Server.internal.makeGui;
        Server.local.makeGui;
        HelperWindow.new;
    });

