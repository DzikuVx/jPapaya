package pl.spychalski;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {

	@SuppressWarnings("rawtypes")
	final public static void printMap(Map mp) {
		Iterator it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	final public static void removeDuplicates(ArrayList list) {
		HashSet set = new HashSet(list);
		list.clear();
		list.addAll(set);
	}

	final public static <K, V> void shuffleMap(Map<K, V> map) {
		List<V> valueList = new ArrayList<V>(map.values());
		Collections.shuffle(valueList);
		Iterator<V> valueIt = valueList.iterator();
		for (Map.Entry<K, V> e : map.entrySet()) {
			e.setValue(valueIt.next());
		}
	}

	/**
	 * Compute MD5
	 * @param md5
	 * @return
	 */
	final public static String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	/**
	 * Sortowanie Mapy Malejąco
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
	
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue()) * -1;
			}
		});
	
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sortowanie Mapy Rosnąco
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAsc(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
	
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
	
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * @param inputArray
	 * @param glueString
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String implode(List<String> inputArray, String glueString) {
	
		StringBuilder sb = new StringBuilder();
	
		Iterator i;
		i = inputArray.iterator();
	
		while (i.hasNext()) {
	
			sb.append(i.next().toString());
			sb.append(glueString);
	
		}
	
		String output = sb.toString().trim();
	
		return output;
	}

}
