// Triangular distribution, Gauss distribution

+SimpleNumber {

	trirand { arg val2=1.0;
			
		^( (rrand(this, val2) + rrand(this, val2)) * 0.5 )
	}
	
	gaussrand  { arg dev=1;  // receiver is mean!
	 	var r1, r2, q=0, p;
		 while { ( q <= 0) || ( q > 1) } {
			 r1 =  1.0.rand2;
			 r2 =  1.0.rand2;
			 q =  r1.squared + r2.squared;
		};
		p = (-2 * q.log * q.reciprocal).sqrt;
		^ (r1 * p * dev + this)
	}
	

}



+SequenceableCollection {
	
	*trirand { arg size, minVal, maxVal;
		var i=0;
		var obj = this.new(size);
		while ({ i < size },{
			//obj.add( (rrand(minVal, maxVal) + rrand(minVal, maxVal)) * 0.5 );
			obj.add( trirand(minVal, maxVal));
			i = i + 1;
		});
		^obj
	}
	
	// dito for gaussrand ...

}



