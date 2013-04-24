package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HMG_List extends HMG_Basic {
	public List<HMG_Basic> stuff = new ArrayList<HMG_Basic>();

	@Override
	public void read(DataInputStream in) throws IOException {
		super.read(in);
		int tags = in.readInt();
		for (int i = 0; i < tags; i++) {
			int id = in.readInt();
			try {
				HMG_Basic tag = HMG_Format.tags.get(id).newInstance();
				tag.read(in);
				stuff.add(tag);
			} catch (InstantiationException e) {
				System.err
						.println("Skipped invalid tag in save file! Following data might be corrupted.");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(stuff.size());
		for (int i = 0; i < stuff.size(); i++) {
			HMG_Basic tag = stuff.get(i);
			out.writeInt(tag.getID());
			tag.write(out);
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int getID() {
		return 5;
	}

	public void setByteArray(String name, byte[] generateMD5) {
		HMG_ByteArray array = new HMG_ByteArray();
		array.array = generateMD5;
		array.name = name;
		stuff.add(array);
	}

	public void setInteger(String name, int value) {
		HMG_Integer integer = new HMG_Integer();
		integer.name = name;
		integer.value = value;
		stuff.add(integer);
	}

	public void setFloat(String name, int value) {
		HMG_Float floate = new HMG_Float();
		floate.name = name;
		floate.value = value;
		stuff.add(floate);
	}

	public void setCompound(String name, HMG_Compound compound) {
		compound.name = name;
		stuff.add(compound);
	}

}
