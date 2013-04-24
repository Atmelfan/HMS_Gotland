package hms_gotland_client;

import hms_gotland_client.SoundEngine.SoundBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

public class SoundSource
{
	/**
	 * 
	 */
	private SoundEngine soundManager;
	int source;
	
	public SoundSource(SoundEngine soundManager, SoundBuffer sound)
	{
		this.soundManager = soundManager;
		source = AL10.alGenSources();
		connectBuffer(sound);
		setDefaultValues();
	}
	
	public void switchBuffer(String buffer)
	{
		SoundBuffer nbuffer = soundManager.getBuffer(buffer);
		if(nbuffer != null)
		{
			connectBuffer(nbuffer);
		}
	}
	
	public void setVolume(float f)
	{
		AL10.alSourcef(source, AL10.AL_GAIN, f);
	}
	
	public void connectBuffer(SoundBuffer sound)
	{
		AL10.alSourcei(source, AL10.AL_BUFFER,   sound.buffer);
	
	}
	
	private void setDefaultValues() {
		AL10.alSourcef(source, AL10.AL_PITCH,    1.0f);
		AL10.alSourcef(source, AL10.AL_GAIN,     1.0f);
		AL10.alSource3f(source, AL10.AL_POSITION, 0, 1.6f, 0);
		AL10.alSource3f(source, AL10.AL_VELOCITY, 0, 0, 0);
	}
	
	public void setLooping(boolean loop)
	{
		AL10.alSourcei(source, AL10.AL_LOOPING,  (loop ? AL10.AL_TRUE : AL10.AL_FALSE));
	}
	
	public void setPosition(Vector3f pos)
	{
		AL11.alBuffer3f(source, AL10.AL_POSITION, pos.x, pos.y, pos.z);
	}
	
	public void setVelocity(Vector3f pos)
	{
		AL11.alBuffer3f(source, AL10.AL_VELOCITY, pos.x, pos.y, pos.z);
	}
	
	public void setOrientation(Vector3f pos)
	{
		AL11.alBuffer3f(source, AL10.AL_ORIENTATION, pos.x, pos.y, pos.z);
	}
	
	public void destroy()
	{
		AL10.alDeleteSources(source);
	}
	
	public void play() 
	{
		AL10.alSourcePlay(source);
	}
	
	public void pause() 
	{
		AL10.alSourcePause(source);
	}
	
	public void stop() 
	{
		AL10.alSourceStop(source);
	}
	
	public void rewind() 
	{
		AL10.alSourceRewind(source);
	}
}