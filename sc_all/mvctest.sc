ModelTest {
	var <wert;
	
	*new { arg value;
		^super.new.init(value);
	}	

	init { arg value;
		// wenn value==nil, dann wert=0.0, ansonsten wert=value:
		wert = value ? 0.0;
	}
		
	// setter fuer die Variable wert:	
	wert_ { arg value;
		// setzen der Variable:
		wert = value;
		// und informieren aller Dependants:
		this.changed(\set);
	}	

	// jedes Objekt, das Dependant eines anderen Objekts ist
	// sollte eine update-Methode implementiert haben:
	update { arg changer, what;
		switch (what)
		{ \set } { wert = changer.wert }
		;
	}
}

