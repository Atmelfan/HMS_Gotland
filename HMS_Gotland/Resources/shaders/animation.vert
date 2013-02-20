#version 150 core

uniform mat4 viewprojMatrix;
uniform mat4 modelMatrix;

in vec4 in_Position_0;
in vec2 in_TextureCoord_0;
in vec4 in_Normal_0;

in vec4 in_Position_1;
in vec2 in_TextureCoord_1;
in vec4 in_Normal_1;

uniform float frame_interpolated;

out vec4 pass_Normal;
out vec2 pass_TextureCoord;

void main(void) 
{
	// Override gl_Position with our new calculated position
	gl_Position = viewprojMatrix * modelMatrix * (in_Position_0 + ((in_Position_0 - in_Position_1) * frame_interpolated));
	
	pass_Normal = normalize(in_Normal_0 + ((in_Normal_0 - in_Normal_1) * frame_interpolated));
	pass_TextureCoord = in_TextureCoord_0 + ((in_TextureCoord_0 - in_TextureCoord_1) * frame_interpolated);
}