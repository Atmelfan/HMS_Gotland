package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HMG_Integer extends HMG_Basic {
	public int value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HMG_Integer [name=" + String.valueOf(name) + ", value=" + value
				+ "]";
	}

	public HMG_Integer() {
	}

	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		super.read(in);
		value = in.readInt();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(value);
	}
}
