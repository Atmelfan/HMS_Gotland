package saveformat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HMG_Compound extends HMG_Basic {
	private static int hierarchy = -1;
	public int num_tags;
	public HashMap<String, HMG_Basic> data = new HashMap<String, HMG_Basic>();

	public HMG_Compound() {
	}

	public void setByteArray(String s, byte[] b) {
		HMG_ByteArray bytearray = new HMG_ByteArray();
		bytearray.name = s;
		bytearray.array = b;
		data.put(s, bytearray);
	}

	public byte[] getByteArray(String s) {
		HMG_Basic tag = data.get(s);
		if (tag instanceof HMG_ByteArray) {
			return ((HMG_ByteArray) tag).array;
		} else {
			return null;
		}
	}

	// Get/set for integer
	public void setInteger(String s, int i) {
		HMG_Integer t = new HMG_Integer();
		t.name = s;
		t.value = i;
		data.put(s, t);
	}

	public int getInteger(String s) {
		HMG_Basic tag = data.get(s);
		if (tag instanceof HMG_Integer) {
			return ((HMG_Integer) tag).value;
		} else {
			return 0;
		}
	}

	// Get/set for float
	public void setFloat(String s, float i) {
		HMG_Float t = new HMG_Float();
		t.name = s;
		t.value = i;
		data.put(s, t);
	}

	public float getFloat(String s) {
		HMG_Basic tag = data.get(s);
		if (tag instanceof HMG_Float) {
			return ((HMG_Float) tag).value;
		} else {
			return 0f;
		}
	}

	// Get/set for string
	public void setString(String s, String i) {
		HMG_String t = new HMG_String();
		t.name = s;
		t.value = i;
		data.put(s, t);
	}

	public String getString(String s) {
		HMG_Basic tag = data.get(s);
		if (tag instanceof HMG_String) {
			return String.valueOf(((HMG_String) tag).value);
		} else {
			return null;
		}
	}

	// Get/set for list
	public void setCompound(String s, HMG_Compound i) {
		i.name = s;
		data.put(s, i);
	}

	public HMG_Compound getCompound(String s) {
		HMG_Basic tag = data.get(s);
		if (tag instanceof HMG_Compound) {
			return (HMG_Compound) tag;
		} else {
			return null;
		}
	}

	public void setList(String s, HMG_List list) {
		list.name = s;
		data.put(s, list);
	}

	public HMG_List getList(String s) {
		HMG_Basic tag = data.get(s);
		if (tag instanceof HMG_List) {
			return (HMG_List) tag;
		} else {
			return null;
		}
	}

	@Override
	public int getID() {
		return 4;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		super.read(in);
		num_tags = in.readInt();
		for (int i = 0; i < num_tags; i++) {
			int id = in.readInt();
			HMG_Basic tag;
			try {
				tag = HMG_Format.tags.get(id).newInstance();
				tag.read(in);
				data.put(String.valueOf(tag.name), tag);
			} catch (InstantiationException e) {
				System.err.println("Skipped invalid tag in save file.");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		hierarchy++;
		StringBuilder sb = new StringBuilder("*HMG_List [name="
				+ String.valueOf(name) + ", num_tags=" + num_tags + "]\n");
		HMG_Basic[] tags = data.values().toArray(new HMG_Basic[0]);
		for (int i = 0; i < tags.length; i++) {
			for (int j = 0; j < hierarchy; j++) {
				sb.append("   ");
			}
			sb.append((i == tags.length - 1 ? "\\ " : "| ")
					+ tags[i].toString() + (i == tags.length - 1 ? "" : "\n"));
		}
		hierarchy--;
		return sb.toString();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(data.size());
		for (HMG_Basic tag : data.values()) {
			if (tag == null)
				continue;
			out.writeInt(tag.getID());
			tag.write(out);
		}
	}
}
