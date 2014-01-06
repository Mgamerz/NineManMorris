package com.cs471.ninemanmill;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

import org.ini4j.Wini;

/**
 * Driver class. 
 * It holds some static variables related to sound so they are not loaded multiple times, and reads the settings. This class spawns the interface object rather than defining it, as it is a static class (required).
 * @author Michael Perez
 * @author Sean Wright
 * @author Melissa Neibaur
 * @author Sasa Rkman
 *
 */
public class NineManMill {

	public final static boolean DEBUG_VERSION = true; /* Used to make sure debugging only code does not ship in the final version */
	public final static String settingsfile = "nmm_settings.ini";
	protected static boolean PLAY_SFX = true; //default to true
	protected static boolean PLAY_MUSIC = true; //default to true
	public static MIDIPlayer bgplayer;
	public static WavePlayer sfxplayer;
	public static AudioInputStream aisClick;
	public static AudioInputStream aisMill;
	public static AudioInputStream aisHammer;
	public static Clip sfxClick;
	public static Clip sfxMill;
	public static Clip sfxHammer;
	
	/**
	 * Main entry point for the program.
	 * @param args array of command line parameters. This program does not process any of them.
	 */
	public static void main(String args[]){
		//Before we load anything, read our settings file.
		readSettings();
		/* Run the UI---
		 * We don't make our UI code in this class because this is a static method and it makes doing things with it a pain.
		 * So we put it in its own class, which makes everything easier. */
		UINew window = new UINew();
		window.setVisible();
	}
	
	/**
	 * Reads and loads the settings file defined by the filename NineManMill.settingsfile, which default sto "nmm_settings.ini".
	 * The ini4j library is used to parse and read this as well as store changes. If a file is not found, one is made with default settings in the working directory.
	 */
	private static void readSettings(){
		//Checkbox was changed: Save changes to wini
		Wini ini; //Ini handler
		boolean firstSetup = false;
		try{
			File settings = new File(NineManMill.settingsfile);
			if (!settings.exists()) {
				//If settings file does not exist, leave the default values
				settings.createNewFile();
				firstSetup = true;
				return;
			}
			//load settings file
			ini = new Wini(settings);
			if (firstSetup){
				//set values instead of reading them
				ini.put("Settings", "playmusic", 1);
				ini.put("Settings", "playsfx", 1);
			} else {
				//read values
				PLAY_SFX = (ini.get("Settings", "playsfx", int.class) == 1) ? true : false; //if the settings has 1 for playsfx, play, otherwise don't
				PLAY_MUSIC = (ini.get("Settings", "playmusic", int.class) == 1) ? true : false; //if the settings has 1 for playsfx, play, otherwise don't
			}
			ini.store();
		} catch (IOException e1) {
			System.err.println("ERROR: Error with I/O when operating on settings file. Settings may not save.");
			e1.printStackTrace();
		}
	}
}
