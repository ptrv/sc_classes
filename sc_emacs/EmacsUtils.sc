EmacsUtils : Object {

    *initClass {
        StartUp.add {
            // this.generateCompletionFile();
            // this.generateTagsFile();
        }
    }

    *generateCompletionFile {
        var dt = {
            var sccompPath;
            var sccompfile;

            sccompPath = "SC_COMPLETION_FILE".getenv ? "~/.sc_completion";
            sccompPath = sccompPath.standardizePath;

            sccompfile = File.open(sccompPath, "w");

            Class.allClasses.do {
                arg klass;
                var klassName;

                klassName = klass.asString;

                sccompfile.write(klassName ++ Char.nl);

                klass.methods.do{|meth|
                    var methName;
                    methName     = meth.name;

                    sccompfile.write(methName ++ Char.nl);
                }
            };

            sccompfile.close();

        }.bench(false);

        "Generated sc_completion file in % seconds\n".postf(dt.asStringPrec(3));
    }

    // *generateTagsFile {
    //     var tagPath;
    //     var tagfile;

    //     tagPath = "SCV_TAGFILE".getenv ? "~/.sctags";
    //     tagPath = tagPath.standardizePath;

    //     tagfile = File.open(tagPath, "w");

    //     tagfile.write('!_TAG_FILE_FORMAT    2   /extended format; --format=1 will not append ;" to lines/'.asString ++ Char.nl);
    //     tagfile.write("!_TAG_FILE_SORTED    0   /0=unsorted, 1=sorted, 2=foldcase/" ++ Char.nl);
    //     tagfile.write("!_TAG_PROGRAM_AUTHOR Stephen Lumenta /stephen.lumenta@gmail.com/" ++ Char.nl);
    //     tagfile.write("!_TAG_PROGRAM_NAME   SCVim.sc//" ++ Char.nl);
    //     tagfile.write("!_TAG_PROGRAM_URL    https://github.com/sbl/scvim" ++ Char.nl);
    //     tagfile.write("!_TAG_PROGRAM_VERSION    1.0//" ++ Char.nl);

    //     Class.allClasses.do {
    //         arg klass;
    //         var klassName, klassFilename, klassSearchString;

    //         klassName         = klass.asString;
    //         klassFilename     = klass.filenameSymbol;
    //         klassSearchString = format("/^%/;\"", klassName);

    //         tagfile.write(klassName ++ Char.tab ++ klassFilename ++ Char.tab ++ klassSearchString ++ Char.nl);

    //         klass.methods.do{|meth|
    //             var methName, methFilename, methSearchString;
    //             methName     = meth.name;
    //             methFilename = meth.filenameSymbol;
    //             // this strange fandango dance is necessary for sc to not complain
    //             // when compiling 123 is the curly bracket
    //             methSearchString = format('/% %/;"'.asString, methName, 123.asAscii);

    //             tagfile.write(methName ++ Char.tab ++ methFilename ++ Char.tab ++ methSearchString ++ Char.nl);
    //         }
    //     };

    //     tagfile.close();
    //     "finished generating tagsfile".postln;
    // }
}
