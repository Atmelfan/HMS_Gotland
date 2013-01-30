#version 150 core

uniform vec4 color;//Text color

uniform sampler2D texture_diffuse;

in vec2 pass_TextureCoord;

out vec4 out_Color;

void main(void) 
{
	// Multiply the white character texture color with the text color
	out_Color = texture2D(texture_diffuse, pass_TextureCoord) * color;
	
}