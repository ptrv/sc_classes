Utils : Object {

    *initClass {
        StartUp.add {
            this.generateCompletionFile();
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
}
