package hms_gotland_client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.GLUtil;
import Util.ShaderUtils;

public class FontRenderer
{
	private int[] character_textures;
	private int[] character_width;
	private int indicesCount;
	private int vbovId;
	private int vbocId;
	private int vboiId;
	private int vaoId;
	private float font_height;
	private static int vsId;
	private static int fsId;
	private static int shader_id;
	private Font font;
	
	public FontRenderer(String font, int size)
	{
		setup(font, size);
		setupQuad();
		if(shader_id <= 0)
		{
			setupShader();
		}
	}
	
	public void setupShader()
	{
		vsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/font_shader.vert"), GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = ShaderUtils.makeShader(ShaderUtils.loadText("Resources/shaders/font_shader.frag"), GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		shader_id = ShaderUtils.makeProgram(vsId, fsId);
		
		GL20.glBindAttribLocation(shader_id, 0, "in_Position");
		GL20.glBindAttribLocation(shader_id, 1, "in_TextureCoord");
		
		GL20.glValidateProgram(shader_id);
		GLUtil.cerror("FontRenderer.setupShader-shader setup");
	}
	
	public void setup(String font_name, int size)
	{
		BufferedImage textImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = textImage.createGraphics();
		font = new Font(font_name, Font.BOLD, size);
		g.setFont(font);
		g.setBackground(new Color(0, 0, 0, 0));
		g.setColor(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		if(!g.getFontRenderContext().isAntiAliased()) System.err.println("Fontrenderer could not be antialiased!");
		font_height = size;
		character_textures = new int['~' - ' '];
		character_width = g.getFontMetrics().getWidths();
		for(char character = ' '; character < '~'; character++)
		{
		    //Generate new texture
			character_textures[character - ' '] = GL11.glGenTextures();
			
			//Draw character
			g.clearRect(0, 0, character_width[character], size);
			g.drawChars(new char[]{character}, 0, 1, 0, size);
			
			//Save character to new buffered image
			BufferedImage image = textImage.getSubimage(0, 0, character_width[character], size);
			
	        int[] pixels = new int[image.getWidth() * image.getHeight()];
	        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

	        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
	        
	        //Transfer image to buffer
	        for(int y = 0; y < image.getHeight(); y++)
	        {
	            for(int x = 0; x < image.getWidth(); x++)
	            {
	                int pixel = pixels[y * image.getWidth() + x];
	                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
	                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
	                buffer.put((byte) (pixel & 0xFF));               // Blue component
	                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
	            }
	        }

	        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

	  		//Generate texture
	        GLUtil.cerror("FontRenderer.setupt-" + (character - ' '));
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, character_textures[character - ' ']);
			// All RGB bytes are aligned to each other and each component is 1 byte
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

			// Setup the ST coordinate system
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

			// Setup what to do when the texture has to be scaled
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GLUtil.cerror("FontRenderer.setup-" + (character - ' '));
		}
		GLUtil.cerror("FontRenderer.setup-font texture setup");
	}
	
	public void setupQuad() 
	{		
		// Vertices, the order is not important.
		float[] vertices = {
				-1f, 1f, 0f,	// Left top			ID: 0
				-1f, -1f, 0f,	// Left bottom		ID: 1
				1f, -1f, 0f,	// Right bottom		ID: 2
				1f, 1f, 0f		// Right left		ID: 3
		};
		
		// Vertices, the order is not important.
		float[] texture = {
				0, 0,	// Left top			ID: 0
				0, 1,	// Left bottom		ID: 1
				1, 1,	// Right bottom		ID: 2
				1, 0,	// Right left		ID: 3
		};
		
		// OpenGL expects to draw vertices in counter clockwise order by default
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
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(vertices), GL15.GL_STREAM_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			
			vbocId = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, GLUtil.buffer(texture), GL15.GL_STATIC_DRAW);
			// Put the VBO in the attributes list at index 0
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		}
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind)
		vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, GLUtil.buffer(indices), GL15.GL_STATIC_DRAW);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);		
		GLUtil.cerror("FontRenderer.setupQuad-VBO setup");
	}
	
	public float point(int coord, int size)
	{
		return coord/size;
	}
	
	public void drawString(int x, int y, String s, float r, float g, float b, float a)
	{
		//Text offset on screen
		float dx = (x / (Display.getWidth() * 0.5f)) - 1f;
		float dy = (y / (Display.getHeight() * 0.5f)) - 1f;
		
		
		ShaderUtils.useProgram(shader_id);
		{
			ShaderUtils.setUniformVar(shader_id, "color", r, g, b, a);
			GL30.glBindVertexArray(vaoId);
			{
				for (int i = 0; i < s.length(); i++)
				{
					char c = s.charAt(i);
					if (c < ' ' || c > '~')
						continue;

					float cw = ((float) character_width[c]) / Display.getWidth();
					float ch = (font_height) / Display.getWidth();

					GL11.glBindTexture(GL11.GL_TEXTURE_2D, character_textures[c - ' ']);

					float[] vertices = 
					{
							dx, dy + ch, 0f, 	// Left top ID: 0
							dx, dy, 0f, 		// Left bottom ID: 1
							dx + cw, dy, 0f, 	// Right bottom ID: 2
							dx + cw, dy + ch, 0f// Right top ID: 3
					};
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbovId);
					{
						// Update vertices position
						GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, GLUtil.buffer(vertices));
					}
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
					// Bind to the index VBO that has all the information about
					// the order of the vertices
					GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

					// Draw the vertices
					GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
					dx += cw;
				}
			}
			GL30.glBindVertexArray(0);
		}
		ShaderUtils.useProgram(0);

	}

	public void drawStringRightAdjusted(int i, int j, String string, float r, float g, float b, float a)
	{
		int xoffset = 0;
		for (int l = 0; l < string.length(); l++)
		{
			if(string.charAt(l) > ' ' && string.charAt(l) < '~') xoffset += character_width[string.charAt(l)];
		}
		drawString(i - xoffset, j, string, r, g, b, a);
	}
}
