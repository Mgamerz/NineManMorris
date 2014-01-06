package com.cs471.ninemanmill;

import java.io.InputStream;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

/**
 * The MIDIPlayer class uses Java's built in MIDI synthesizer to handle playing background music, including starting, stopping, and looping. 
 * @author Michael Perez
 *
 */
public class MIDIPlayer implements MetaEventListener {
	public static final int END_OF_TRACK_MESSAGE = 47;
    public static boolean useExternalSynth = false;
    private boolean loop = true;
    protected static Sequencer sequencer;
    
    public void playBGMusic() throws Exception  {
    	// Obtains the default Sequencer connected to a default device.
    			sequencer = MidiSystem.getSequencer();

    			// Opens the device, indicating that it should now acquire any
    		    // system resources it requires and become operational.
    			sequencer.open();

    		    // create a stream from a file
    		    InputStream is = getClass().getResourceAsStream("resources/bgmusic2.mid");

    		    // Sets the current sequence on which the sequencer operates.
    		    // The stream must point to MIDI file data.
    		    sequencer.setSequence(is);
    		    sequencer.addMetaEventListener(this);
    		    // Starts playback of the MIDI data in the currently loaded sequence.
    		    sequencer.start();

    }
    
    public boolean isActive(){
    	if (sequencer == null) return false;
    	return sequencer.isRunning();
    }
    
    public void stopMusic(){
    	sequencer.stop();
    	sequencer.close();
    	System.out.println("Music stop: Garbage collecting.");
    	System.gc();
    }
    
    public void meta(MetaMessage event) {
    	  if (event.getType() == END_OF_TRACK_MESSAGE) {
    	   if (sequencer != null && sequencer.isOpen() && loop) {
    		   System.out.println("MIDI restart: Garbage collecting.");
    		   System.gc();
    	       sequencer.setTickPosition(0);
    	       sequencer.start();
    	   }
    	  }
    	}
}