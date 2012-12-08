package Util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class Sound
{
	private final String SOUND_PATH = "/Resources/sounds/";
	private HashMap<String, Sound.SoundObject> sounds = new HashMap<String, Sound.SoundObject>();
	private FloatBuffer listenerVel;
	private FloatBuffer listenerPos;
	private FloatBuffer sourcePos;
	private FloatBuffer sourceVel;
	
	public void init()
	{
		/** Position of the source sound. */
		sourcePos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		/** Velocity of the source sound. */
		sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		/** Position of the listener. */
		listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		/** Velocity of the listener. */
		listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		/** Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
		FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });
	}
	
	private void play(String name, float[] listPos, float[] listOri, float[] sourcPos, float[] sourcOri)
	{
		
		Sound.SoundObject snd = sounds.get(name);
		if(snd != null)
		{
			snd.play();
		}else
		{
			load(SOUND_PATH + name);
		}
	}
	
	private void load(String name)
	{
		SoundObject load = new SoundObject();
		// Load wav data into a buffer.
		AL10.alGenBuffers(load.buffer);

		if(AL10.alGetError() != AL10.AL_NO_ERROR)
			return;

		WaveData waveFile = WaveData.create(name);
		AL10.alBufferData(load.buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
	}
	
	private class SoundObject
	{
		public int id;
		
		/** Buffers hold sound data. */
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		
		/** Sources are points emitting sound. */
		IntBuffer source = BufferUtils.createIntBuffer(1);
		
		public void play()
		{
			
		}
	}
}
