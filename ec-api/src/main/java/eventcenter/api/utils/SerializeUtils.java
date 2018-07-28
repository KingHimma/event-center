package eventcenter.api.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeUtils {

	public static byte[] serialize(Serializable evt) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		try{
			oos.writeObject(evt);
			oos.flush();
			return os.toByteArray();
		}finally{
			oos.close();
		}
	}

	public static Serializable unserialize(byte[] evt) throws IOException {
		if(null == evt || evt.length == 0)
			return null;
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(evt));
		try{
			return (Serializable)ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}finally{
			ois.close();
		}
	}
}
