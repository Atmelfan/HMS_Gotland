package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

public class HMG_ByteArray extends HMG_Basic {

	public byte[] array;

	@Override
	public void read(DataInputStream in) throws IOException {
		super.read(in);
		int size = in.readInt();
		array = new byte[size];
		in.read(array);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(array.length);
		out.write(array);
	}

	@Override
	public String toString() {
		return "HMG_ByteArray [name=" + String.valueOf(name) + ", value="
				+ DatatypeConverter.printHexBinary(array) + "]";
	}

	@Override
	public int getID() {
		return 6;
	}

}
