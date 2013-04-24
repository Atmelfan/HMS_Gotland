package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HMG_Float extends HMG_Basic {
	public float value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HMG_Float [name=" + String.valueOf(name) + ",value=" + value
				+ "]";
	}

	public HMG_Float() {
	}

	@Override
	public int getID() {
		return 2;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		super.read(in);
		value = in.readFloat();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeFloat(value);
	}
}
