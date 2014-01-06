package com.cs471.ninemanmill;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;    

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class contains methods relating to playing .wav files as sound effects, and managing memory for them efficiently. It is based on an example from StackOverflow.com.
 * @author Michael Perez
 * @see http://stackoverflow.com/questions/6627730/problem-with-javas-audio-clips-on-frequent-playback-of-beep-sounds
 *
 */
public class WavePlayer{

private Runnable clickRunner;
private Runnable hammerRunner;
private Runnable millRunner;
private Runnable criticalRunner;
public final static int SOUND_CLICK = 0;
public final static int SOUND_HAMMER = 1;
public final static int SOUND_MILL = 2;
public final static int SOUND_CRITICAL = 3;

ExecutorService pool = Executors.newCachedThreadPool();

/**
 * Constructs a new Wav player. This plays the wav sounds in a threadpool, preventing spinning up and spinning down threads which causes a miniscule, but noticable, lag in the interface.
 */
public WavePlayer(){
	reloadSettings();
}

/**
 * Call this after initialization and after every change in your config at runtime.
 */
public void reloadSettings() {
    // put your configuration here
    this.hammerRunner = new WaveThread(this.getClass().getResource("resources/hammer_hit.wav"));
    this.clickRunner = new WaveThread(this.getClass().getResource("resources/click.wav"));
    this.millRunner = new WaveThread(this.getClass().getResource("resources/mill.wav"));
    this.criticalRunner = new WaveThread(this.getClass().getResource("resources/criticalfly.wav")); //currently not used
}

/**
 * Plays a sound file, click.wav.
 * Sadly this method can't be static cause of getClass() being required and Java's really poor audio support.
 */
public void playSound(int sound){
	//System.out.println("BP");
	if (NineManMill.PLAY_SFX){
		//AudioInputStream audioInputStream = null;
			switch(sound){
			case WavePlayer.SOUND_HAMMER:
				NineManMill.sfxplayer.eventHammer();
				break;
			case WavePlayer.SOUND_MILL:
				//this is a debug item
				NineManMill.sfxplayer.eventMill();
				break;
			case WavePlayer.SOUND_CRITICAL:
				NineManMill.sfxplayer.eventCritical();
				break;
			default:
				//this is default, so we don't get a null pointer if for some reason it comes in. SOUND_CLICK should still be used for future proofing.
				NineManMill.sfxplayer.eventClick();
				break;
			}
		}
}

/**
 * Call this to safely shutdown the thread pool.
 */
public void shutdown() {
    this.pool.shutdown();
}

/**
 * Listener method called on success. 
 */
public void eventMill() {
    this.pool.execute(this.millRunner);
}

/**
 * Listener method called on fail. 
 */
public void eventHammer() {
    this.pool.execute(this.hammerRunner);
}

/**
 * Plays the critical music, showing that fly mode has just begun.
 */
public void eventCritical(){
	this.pool.execute(this.criticalRunner);
}

/**
 * Listener method called on fail. 
 */
public void eventClick() {
    this.pool.execute(this.clickRunner);
}

/**
 * 
 * @author mjperez
 *
 */
private class WaveThread implements Runnable {

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb 

    private URL soundFile;

    public WaveThread(URL soundFile) {
        this.soundFile = soundFile;
    }

    @Override
    public void run() {

        try {
            // check if the URL is still accessible!
            this.soundFile.openConnection().connect();
            this.soundFile.openStream().close();
        } catch (Exception e) {
            return;
        }

        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem
                    .getAudioInputStream(this.soundFile);
        } catch (UnsupportedAudioFileException e) {
            return;
        } catch (IOException e) {
            return;
        }

        AudioFormat format = audioInputStream.getFormat();
        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            return;
        } catch (Exception e) {
            return;
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[this.EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream
                        .read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    auline.write(abData, 0, nBytesRead);
                }
            }
        } catch (IOException e) {
            return;
        } finally {
            auline.drain();
            auline.close();
        }
    }
}
}