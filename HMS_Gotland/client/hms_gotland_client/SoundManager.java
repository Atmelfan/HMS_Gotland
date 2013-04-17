package hms_gotland_client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import Util.GLUtil;

public class SoundManager 
{
	/** Position of the source sound. */
	FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
	/** Velocity of the source sound. */
	FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
	/** Position of the listener. */
	FloatBuffer listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
	/** Velocity of the listener. */
	FloatBuffer listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
	/** Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
	FloatBuffer listenerOri =
	    BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });
	
	private HashMap<String, SoundBuffer> sounds = new HashMap<>();
	private List<SoundSource> sources = new ArrayList<>();

	public SoundManager()
	{
		try {
			AL.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setListenerValues();
	}
	
	public SoundSource getNewSource(String s)
	{
		SoundBuffer son = sounds.get(s);
		if(son == null)
		{
			son = new SoundBuffer();
			son.buffer = AL10.alGenBuffers();
			if(AL10.alGetError() != AL10.AL_NO_ERROR)
			{
				System.err.println("Couldn't load: Resources/assets/sounds/" + s);
				return null;
			}
			WaveData wave = null;
			try {
				wave = WaveData.create(new BufferedInputStream(
						new FileInputStream("Resources/assets/sounds/" + s)));
			} catch (FileNotFoundException e) {
				System.err.println("Couldn't load: Resources/assets/sounds/" + s);
				e.printStackTrace();
			}
			if(wave == null)
			{//WaveData.create doesn't always raise an exception when it fails...
				System.err.println("Couldn't load: Resources/assets/sounds/" + s);
				return null;
			}
			AL10.alBufferData(son.buffer, wave.format, wave.data, wave.samplerate);
			wave.dispose();
			// Bind the buffer with the source.
			sounds.put(s, son);
		}
		return new SoundSource(this, son);
	}
	
	void setListenerValues()
	{
		listenerPos.flip();
		AL10.alListener(AL10.AL_POSITION,    listenerPos);
		listenerVel.flip();
		AL10.alListener(AL10.AL_VELOCITY,    listenerVel);
		listenerOri.flip();
		AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
	}
	
	class SoundBuffer
	{
		public int buffer;
		
		public void destroy()
		{
			AL10.alDeleteBuffers(buffer);
		}
	}
	
	public void destroy() {
		for (SoundBuffer so : sounds.values()) {
			so.destroy();
		}
		for (SoundSource so : sources) {
			so.destroy();
		}
		AL.destroy();
	}
}
