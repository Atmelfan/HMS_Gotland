#version 150 core

uniform mat4 viewprojMatrix;
uniform mat4 modelMatrix;

layout(std140) uniform nodes[]
{
    int id;
    int parent;
};

layout(std140) uniform bones[]
{
	int node;
    vec3 pos;
    vec4 ori;
};

in vec4 in_Position;
in vec2 in_TextureCoord;
in vec4 in_Normal;

out vec4 pass_Normal;
out vec2 pass_TextureCoord;

void main(void) 
{
	// Override gl_Position with our new calculated position
	gl_Position = viewprojMatrix * modelMatrix * in_Position;
	
	pass_Normal = in_Normal;
	pass_TextureCoord = in_TextureCoord;
}