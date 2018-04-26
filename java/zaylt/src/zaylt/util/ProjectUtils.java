package zaylt.util;

public class ProjectUtils {

	public static boolean equalsWithAny(CharSequence cs, CharSequence... css) {
		for (CharSequence c : css) {
			if (cs == c || cs.equals(c))
				return true;
		}
		return false;
	}
}
