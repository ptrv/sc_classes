+ PathName {

    asFolder {
        var path = this.fullPath;
        if(path.last.isPathSeparator, {
            ^this;
        }, {
            ^PathName(path ++ "/");
        });
    }
    
}
