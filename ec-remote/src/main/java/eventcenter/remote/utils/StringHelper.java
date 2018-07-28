package eventcenter.remote.utils;

public class StringHelper {

	public static boolean isEmpty(String value){
		return null == value || "".equals(value.trim());
	}
	
	public static boolean isNotEmpty(String value){
		return !isEmpty(value);
	}
	
	public static boolean equals(String s1, String s2){
		if(s1 == null && s2 == null)
			return true;
		if(s1 == null || s2 == null)
			return false;
		return s1.equals(s2);
	}
}
