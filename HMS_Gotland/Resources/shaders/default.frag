#version 150 core

uniform sampler2D texture_diffuse;

in vec4 pass_Normal;
in vec2 pass_TextureCoord;

out vec4 out_Color;

void main(void) 
{
	// Override out_Color with our texture pixel
	out_Color = texture2D(texture_diffuse, pass_TextureCoord);
	out_Color.rgb *= min(sin(gl_FragCoord.y * 16) + 0.9f, 1f);
}

