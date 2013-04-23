package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HMG_String extends HMG_Basic {
	public String value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HMG_String [name=" + String.valueOf(name) + ", value=" + value
				+ "]";
	}

	public HMG_String(String s) {
		value = s;
	}

	public HMG_String() {
	}

	@Override
	public int getID() {
		return 3;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		super.read(in);
		byte[] buffer = new byte[in.readInt()];
		in.read(buffer);
		value = new String(buffer, "UTF-8");
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		byte[] buffer = value.getBytes("UTF-8");
		out.writeInt(buffer.length);
		out.write(buffer);
	}
}
