#version 150 core

in vec4 pass_Color;

out vec4 out_Color;

void main(void) 
{
	// Multiply the white character texture color with the text color
	out_Color = pass_Color;
	out_Color.rgb *= min(sin(gl_FragCoord.y * 16) + 0.9f, 1f);
}