/*
GENERAL DEFINITION OF JUDGE SYNTHS:
A judge evaluates a signal, read from bus "testbus". Often the evaluation is done in reference to 
another signal - perhaps a "template" signal, or an aggregate. The judge must evaluate the signal's
deviation from the ideal, and output the integral of this deviation to bus "out".

Required parameters in the synth:
 * testbus - the audio bus to read from and evaluate
 * out     - the control bus on which to write the judgment value
 * active  - set to zero to "pause" the judge, and hold output constant
 * t_reset - trigger to reset the output integral back to zero

Typically you will also have some kind of "templatebus" to read from, etc.
 
*/


GABasicJudgeSynths {
	*initClass{
		StartUp.add{
			
			// This judge is one of the simplest I can think of (for demo purposes) -
			//  evaluates closeness of pitch to a reference value (660 Hz).
			SynthDef.writeOnce(\_ga_judge_targetpitch, { |testbus, out=0, active=0, t_reset=0, targetpitch=660|
				var testsig, comparison, integral, freq, hasFreq;
				
				testsig = LeakDC.ar(In.ar(testbus, 1));
				
				# freq, hasFreq = Pitch.kr(testsig);
				
				comparison = if(hasFreq, (freq - targetpitch).abs, 1); // "1" if hasFreq==false because we don't want to encourage quietness
				
				// Divide by the server's control rate to bring it within a sensible range.
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});

			
			
			
			// This judge aims for the fundamental pitch to vary as much as possible.
			// You may find that this tends towards white noise or similar...
			SynthDef.writeOnce(\_ga_judge_movingpitch, { |testbus, out=0, active=0, t_reset=0|
				var testsig, comparison, integral, freq, hasFreq;
				
				testsig = LeakDC.ar(In.ar(testbus, 1));
				
				freq = A2K.kr(ZeroCrossing.ar(testsig));
				
				//freq.poll(5, "freq");
				
				// "Moving" pitch - we want the slope of the pitch to be as high as possible
				comparison = 1 / max(Slope.kr(freq).abs /* .poll(5, "slope") */, 0.0000001);
				//comparison.poll(5, "comp");
				
				// We want the maximum difference to equate to a slope of 1.0 per second on the integral.
				// Assuming the comparison produces 0<=x<=1, all we need do is divide by the server's control rate.
				comparison = comparison / ControlRate.ir;
								
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});


			// Match a specific not-quite-trivial amplitude envelope
			// Env.new([0.001, 1, 0.3, 0.8, 0.001],[0.2,0.3,0.1,0.4],'welch').test.plot
			SynthDef.writeOnce(\_ga_judge_ampenv, { |testbus, out=0, active=0, t_reset=0|
				var testsig, comparison, integral, env;
				
				testsig = LeakDC.ar(In.ar(testbus, 1));
				
				// Alter this envelope to the one you're aiming for...
				env = EnvGen.kr(Env.new([0.001, 1, 0.3, 0.8, 0.001],[0.2,0.3,0.1,0.4],'welch'), t_reset);
												
				comparison = (Amplitude.kr(testsig) - env).abs;
				
				comparison = comparison / ControlRate.ir;
								
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});

			// Try and match amplitude envelope against a template signal
			SynthDef.writeOnce(\_ga_judge_ampmatch, {		|testbus, templatebus=0, out=0, active=0, t_reset=0|
				var othersig, testsig, comparison, integral, sigamp, oamp, 
					resynth;
				
				testsig  = In.ar(testbus, 1);
				othersig = In.ar(templatebus, 1);
				
				sigamp = Amplitude.kr(testsig);
				oamp = Amplitude.kr(othersig);
				
				comparison = (sigamp - oamp).abs;
				
				// Divide by the server's control rate to scale the output nicely
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});


			// Try and match pitch envelope against a template signal
			SynthDef.writeOnce(\_ga_judge_pitchmatch, {		|testbus, templatebus=0, out=0, active=0, t_reset=0|
				var othersig, testsig, comparison, integral, sigpitch, opitch, sighaspitch, ohaspitch,
					resynth;
				
				testsig  = In.ar(testbus, 1);
				othersig = In.ar(templatebus, 1);
								
				# sigpitch, sighaspitch = Pitch.kr(testsig);
				# opitch, ohaspitch = Pitch.kr(othersig);
				
				comparison = (sigpitch - opitch).abs * 0.1;
				
				// Divide by the server's control rate to scale the output nicely
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});
			
			// Try and match pitch envelope against a template signal - but using ZeroCrossing
			SynthDef.writeOnce(\_ga_judge_pitchmatch_zc, {		|testbus, templatebus=0, out=0, active=0, t_reset=0|
				var othersig, testsig, comparison, integral, sigpitch, opitch, 
					resynth;
				
				testsig  = In.ar(testbus, 1);
				othersig = In.ar(templatebus, 1);
								
				sigpitch = A2K.kr(ZeroCrossing.ar(testsig));
				opitch = A2K.kr(ZeroCrossing.ar(othersig));
				
				comparison = (sigpitch - opitch).abs * 0.1;
				
				// Divide by the server's control rate to scale the output nicely
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});

			// Try and match the FFT of the individual against some "template" signal - typically an audio sample.
			// NB - this uses a CUSTOM UGEN, available from http://www.mcld.co.uk/supercollider/
			SynthDef.writeOnce(\_ga_judge_fftmatch, {		|testbus, bfr1, bfr2, templatebus=0, out=0, active=0, t_reset=0|
				var othersig, testsig, comparison, integral, sigamp, oamp, 
					sigfft, offt, sigbufplay, obufplay, fftdiff,
					resynth;
				
				testsig  = LeakDC.ar(In.ar(testbus, 1));
				othersig = LeakDC.ar(In.ar(templatebus, 1));
				
				// Take a wideband FFT of the signals since we're interested in time-domain features rather than freq precision
				// (use buffers of ~64 or 128 size - NB 32 is too small - kills the server)
				sigfft = FFT(bfr1, testsig);
				offt =   FFT(bfr2, othersig);
				
				// Smear the FFT a little to avoid being trapped in bins
				sigfft = PV_MagSmear(sigfft, 5);
				  offt = PV_MagSmear(  offt, 5);

				comparison = FFTDiffMags.kr(sigfft, offt);

				// Divide by the server's control rate to scale the output nicely
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});

			// Really simple SynthDef to play a buffer when triggered
			SynthDef.writeOnce(\_ga_just_playbuf, {		|bufnum, out=0, t_trig=0|
				Out.ar(out, /* SinOsc.ar(440,0,0.1) + */ PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), t_trig));
			});

			// Like FFT but with normaliser
			SynthDef.writeOnce(\_ga_judge_fftmatch_norm, {		|testbus, bfr1, bfr2, templatebus=0, out=0, active=0, t_reset=0|
				var othersig, testsig, comparison, integral, sigamp, oamp, 
					sigfft, offt, sigbufplay, obufplay, fftdiff,
					resynth;
				
				testsig  = Normalizer.ar(LeakDC.ar(In.ar(testbus, 1)));
				othersig = Normalizer.ar(LeakDC.ar(In.ar(templatebus, 1)));
				
				// Take a wideband FFT of the signals since we're interested in time-domain features rather than freq precision
				// (use buffers of ~64 or 128 size - NB 32 is too small - kills the server)
				sigfft = FFT(bfr1, testsig);
				offt =   FFT(bfr2, othersig);
				
				comparison = FFTDiffMags.kr(sigfft, offt);

				// Divide by the server's control rate to scale the output nicely
				comparison = comparison / ControlRate.ir;
				
				// Default coefficient of 1.0 = no leak. When t_reset briefly hits nonzero, the integrator drains.
				integral = Integrator.kr(comparison * active, if(t_reset>0, 0, 1));
				
				Out.kr(out, integral);
			});
			
			
		} // End of StartUp.add
	} // End of initClass
}