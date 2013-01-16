/**
 * HMS_Gotland/VertexData.java - 16 dec 2012:15:17:05
 */
package Util;

/**
 * @author LWJGL Wiki
 */
public class VertexData {
	// Vertex data
	private float[] xyzw = new float[] {0f, 0f, 0f, 1f};
	private float[] norm = new float[] {1f, 1f, 1f, 1f};
	private float[] st = new float[] {0f, 0f};
	
	// The amount of bytes an element has
	public static final int elementBytes = 4;
	
	// Elements per parameter
	public static final int positionElementCount = 4;
	public static final int normalElementCount = 4;
	public static final int textureElementCount = 2;
	
	// Bytes per parameter
	public static final int positionBytesCount = positionElementCount * elementBytes;
	public static final int normalByteCount = normalElementCount * elementBytes;
	public static final int textureByteCount = textureElementCount * elementBytes;
	
	// Byte offsets per parameter
	public static final int positionByteOffset = 0;
	public static final int normalByteOffset = positionByteOffset + positionBytesCount;
	public static final int textureByteOffset = normalByteOffset + normalByteCount;
	
	// The amount of elements that a vertex has
	public static final int elementCount = positionElementCount + 
			normalElementCount + textureElementCount;	
	// The size of a vertex in bytes, like in C/C++: sizeof(Vertex)
	public static final int stride = positionBytesCount + normalByteCount + 
			textureByteCount;
	
	// Setters
	public void setXYZ(float x, float y, float z) {
		this.setXYZW(x, y, z, 1f);
	}
	
	public void setNormal(float r, float g, float b) {
		norm = new float[]{r, g, b, 1f};
	}
	
	public void setST(float s, float t) {
		this.st = new float[] {s, t};
	}
	
	public void setXYZW(float x, float y, float z, float w) {
		this.xyzw = new float[] {x, y, z, w};
	}
	
	public void setNormal(float r, float g, float b, float a) {
		norm = new float[] {r, g, b, a};
	}
	
	// Getters	
	public float[] getElements() {
		float[] out = new float[VertexData.elementCount];
		int i = 0;
		
		// Insert XYZW elements
		out[i++] = this.xyzw[0];
		out[i++] = this.xyzw[1];
		out[i++] = this.xyzw[2];
		out[i++] = this.xyzw[3];
		// Insert RGBA elements
		out[i++] = this.norm[0];
		out[i++] = this.norm[1];
		out[i++] = this.norm[2];
		out[i++] = this.norm[3];
		// Insert ST elements
		out[i++] = this.st[0];
		out[i++] = this.st[1];
		
		return out;
	}
	
	public float[] getXYZW() {
		return new float[] {this.xyzw[0], this.xyzw[1], this.xyzw[2], this.xyzw[3]};
	}
	
	public float[] getXYZ() {
		return new float[] {this.xyzw[0], this.xyzw[1], this.xyzw[2]};
	}
	
	public float[] getRGBA() {
		return new float[] {this.norm[0], this.norm[1], this.norm[2], this.norm[3]};
	}
	
	public float[] getRGB() {
		return new float[] {this.norm[0], this.norm[1], this.norm[2]};
	}
	
	public float[] getST() {
		return new float[] {this.st[0], this.st[1]};
	}
}
