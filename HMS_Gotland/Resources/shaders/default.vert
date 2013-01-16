#version 150 core

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

in vec4 in_Position;
in vec2 in_TextureCoord;
in vec4 in_Normal;

out vec4 pass_Normal;
out vec2 pass_TextureCoord;

void main(void) 
{
	// Override gl_Position with our new calculated position
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * in_Position;
	
	pass_Normal = in_Normal;
	pass_TextureCoord = in_TextureCoord;
}

