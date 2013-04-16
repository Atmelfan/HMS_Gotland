package hms_gotland_client;

import java.io.IOException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.GLUtil;
import Util.ShaderUtils;

public class Wardos
{	
	int gui_id;
	private int vaoId;
	private int vsId;
	private int fsId;
	private int shader_id;
	private int indicesCount;
	
	private int vboiId;
	private int vbovId;
	private int vbocId;
	private FontRenderer font;
	private HMS_Gotland game;
	private int game_id;
	private int fboID;
	private int fbotexID;
	private int fborboID;
	
	public Wardos(HMS_Gotland game)
	{
		//Blablablabla code blabla and bla.
		//PS blablabla... bla!
		this.game = game;
		setupShader();
		setupQuad();
		gui_id = game.getRenderEngine().getTexture("generic://WardosScreenCracked.png", GL13.GL_TEXTURE0);
		game_id = game.getRenderEngine().getTexture("generic://WardosScreen.png", GL13.GL_TEXTURE0);
		font = new FontRenderer("Agency FB", 60);
		GLUtil.cerror("Wardos.Wardos-HUD init");
	}
	
	//private int movieTextureID;
	//private Movie movie;
	//private ByteBuffer textureBuffer;
	//private OpenALAudioRenderer audioRenderer;
	
	public void pauseMovie(boolean pause)
	{
		/*if(movie != null)
		if(pause)
		{
			audioRenderer.pause();
		}else
		{
			audioRenderer.resume();
		}*/
	}
	
	public void playMovie(String s) throws IOException
	{
		/*
		movie = Movie.open(game.getResourceManager().getResource(s));

		audioRenderer = new OpenALAudioRenderer();
		audioRenderer.init(movie.audioStream(), movie.framerate());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if(movieTextureID == 0)
			movieTextureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, movieTextureID);

		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		int wPot = movie.width();
		int hPot = movie.height();

		// 'tmpbuf' should be null, but some drivers are too buggy
		textureBuffer = BufferUtils.createByteBuffer(wPot * hPot * 3);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, wPot, hPot, 0, GL_RGB, GL_UNSIGNED_BYTE, textureBuffer);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		*/
	}
	
	public void stopMovie()
	{
		/*if(movie != null)
		{
			try
			{
				textureBuffer.clear();
				textureBuffer = null;
				movie.close();
				movie = null;
				audioRenderer.close();
				audioRenderer = null;
			} catch (IOException e)
			{
				System.err.println("Error: Wardos.stopMovie() - " + e.getMessage());
			}
		}*/
	}
	
	public void setupShader()
	{
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/wardos.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/wardos.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		shader_id = ShaderUtils.makeProgram(vsId, fsId);
		
		GL20.glBindAttribLocation(shader_id, 0, "in_Position");
		GL20.glBindAttribLocation(shader_id, 1, "in_TextureCoord");
		
		GL20.glValidateProgram(shader_id);
		
		GLUtil.cerror("HUD shader init");
	}
	
	public void setupVBO()
	{
		fboID = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		{
			fbotexID = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbotexID);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 1024, 512, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			fborboID = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, fborboID);
			{
				GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, 1024, 512);
				GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, fborboID);
				GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fbotexID, 0);
				GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			}
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("Error initializing FBO!");
	}
	
	public void draw()
	{
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		/*if(movie != null)
		{
			audioRenderer.tick(movie);
			outer:
			if(movie.isTimeForNextFrame()) 
			{
				final int maxFramesBacklog = 5;
				int framesRead = 0;
				do 
				{
					if (framesRead > 0) {
						// signal the AV-sync that we processed a frame
						movie.onUpdatedVideoFrame();
					}

					// grab the next frame from the video stream
					textureBuffer = movie.videoStream().readFrameInto(textureBuffer);

					if (textureBuffer == null) {
						break outer;
					}
					framesRead++;
				} while (movie.hasVideoBacklogOver(maxFramesBacklog));

				if (framesRead > 1) {
					System.out.println("video frames skipped: " + (framesRead - 1));
				}
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, movie.width(), movie.height(), GL_RGB, GL_UNSIGNED_BYTE, textureBuffer);
				// signal the AV-sync that we processed a frame
				movie.onUpdatedVideoFrame();
			}
			if(audioRenderer.getState() == State.CLOSED)
			{
				stopMovie();
			}
		}
		*/
		ShaderUtils.useProgram(shader_id);
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, game.playing ? game_id : gui_id);
			/*if(movie != null)
			{
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, movieTextureID);
			}*/
			// Bind to the VAO that has all the information about the vertices
			GL30.glBindVertexArray(vaoId);
			{
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				
				// Bind to the index VBO that has all the information about the order of the vertices
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
				
				// Draw wthe vertices
				GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
				
				// Put everything back to default (deselect)
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
			}
			GL30.glBindVertexArray(0);
			renderGUI();
		}
		ShaderUtils.useProgram(0);
		
		//font.drawString(4, 6, "Coord: N/A", 0f, 0.5f, 0f, 175/255f);
		//font.drawStringRightAdjusted(1024, 0, "Coord: " + game.getPlayer().getPos().toString(), 0f, 0.5f, 0f, 175/255f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	private void renderGUI()
	{
		if(game.playing)
		{
			//for()
		}
		
	}

	public void setupQuad() 
	{		
		//Vertices, the order is not important.
		float[] vertices = {
				-1f, 1f, 0f,	// Left top			ID: 0
				-1f, -1f, 0f,	// Left bottom		ID: 1
				1f, -1f, 0f,	// Right bottom		ID: 2
				1f, 1f, 0f		// Right left		ID: 3
		};
		
		//Vertices, the order is not important.
		float[] texture = {
				0, 0,	// Left top			ID: 0
				0, 1,	// Left bottom		ID: 1
				1, 1,	// Right bottom		ID: 2
				1, 0,	// Right left		ID: 3
		};
		
		//OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				// Left bottom triangle
				0, 1, 2,
				// Right top triangle
				2, 3, 0
		};
		indicesCount = indices.length;
		
		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		{
			// Create a new Vertex Buffer Object in memory and select it (bind)
			// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
			vbovId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbovId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(vertices), GL15.GL_STATIC_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			
			vbocId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(texture), GL15.GL_STATIC_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind)
		vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, GLUtil.buffer(indices), GL15.GL_STATIC_DRAW);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);	
		GLUtil.cerror("Wardos.setupQuad");
	}
	
	public void tick()
	{
		
	}
	
	public void command(String s)
	{
		
	}

	public boolean isPlayingMovie()
	{
		/*if(movie != null)
		{
			return audioRenderer.getState() == State.PLAYING;
		}*/
		return false;
	}
}
