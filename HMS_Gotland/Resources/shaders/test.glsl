&shader default
	common
		#version 150 core
	vertex
		uniform mat4 viewprojMatrix;
		uniform mat4 modelMatrix;
		
		in vec4 in_Position;
		in vec2 in_TextureCoord;
		in vec4 in_Normal;
		out vec4 pass_Normal;
		out vec2 pass_TextureCoord;
		
		void main(void) 
		{
			gl_Position = viewprojMatrix * modelMatrix * in_Position;
			pass_Normal = in_Normal;
			pass_TextureCoord = in_TextureCoord;
		}
	fragment
		uniform sampler2D texture_diffuse;
		uniform float time;
		
		in vec4 pass_Normal;
		in vec2 pass_TextureCoord;
		out vec4 out_Color;
		
		void main(void) 
		{
			out_Color = texture2D(texture_diffuse, pass_TextureCoord);
			out_Color.rgb *= min(sin((gl_FragCoord.y + time) * 16) + 0.9, 1);
		}
	geometry	
end