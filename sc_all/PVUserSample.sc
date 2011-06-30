//CNToolsSC3 should be installed
PVUserSample : UserSample {
	classvar mybase = "/Users/peter/scwork/snd/";
	var <>debugMode=true;
	*new { |path, basepath|
		super.base_(mybase);
		^super.new(path);
	}
}