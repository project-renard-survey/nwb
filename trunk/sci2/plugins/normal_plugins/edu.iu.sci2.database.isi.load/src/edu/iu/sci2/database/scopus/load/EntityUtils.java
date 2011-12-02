package edu.iu.sci2.database.scopus.load;

import java.lang.reflect.Array;
import java.util.Dictionary;
import java.util.Hashtable;

import org.cishell.utilities.StringUtilities;

import edu.iu.cns.database.load.framework.DBField;
import edu.iu.cns.database.load.framework.DerbyFieldType;
import edu.iu.cns.database.load.framework.Entity;
import edu.iu.sci2.database.scholarly.FileField;

public class EntityUtils {

	public static void putValue(Dictionary<String, Object> attribs, DBField field, Object value) {
		// TODO: log if it's null?
		if (value != null) {
			attribs.put(field.name(), value);
		}
	}

	public static void putPK(Dictionary<String, Object> attribs, DBField field, Entity<?> entity) {
		putValue(attribs, field, entity.getAttributes().get("PK"));
	}
	
	/**
	 * Prefuse Tables are annoying because if a field is sometimes present and
	 * sometimes not, it will wrap that field in an array. So instead of Integer
	 * objects and nulls, you get one-element int arrays and empty int arrays.
	 * 
	 * @param o
	 * @return
	 */
	static Object removeArrayWrapper(Object o, Object ifEmpty) {
		Object toInsert;
	
		if (o.getClass().isArray()) {
			try {
				int length = Array.getLength(o);
				if (length == 0) {
					toInsert = ifEmpty;
				} else if (length == 1) {
					toInsert = Array.get(o, 0);
				} else {
					throw new AssertionError("Bleh.");
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Should be array, but doesn't have length", e);
			}
		} else {
			toInsert = o;
		}
		return toInsert;
	}

	/**
	 * May not actually put the field if the source is empty!  Since Hashtables can't hold nulls,
	 * and the database knows that something is null if the .get(key) is null.
	 * @param dest
	 * @param destKey
	 * @param source
	 * @param sourceKey
	 */
	static <S extends FileField> void putField(Dictionary<String, Object> dest,
			DBField destKey, FileTuple<S> source, S sourceKey) {
		if (destKey.type() == DerbyFieldType.TEXT) {
			putStringField(dest, destKey, source, sourceKey);
		} else if (destKey.type() == DerbyFieldType.INTEGER) {
			putIntegerField(dest, destKey, source, sourceKey);
		}
	}

	private static <S extends FileField> void putIntegerField(Dictionary<String, Object> dest,
			DBField destKey, FileTuple<S> source, S sourceKey) {
		Integer someObject = getNullableInteger(source, sourceKey);
		if (someObject != null) {
			dest.put(destKey.name(), someObject);
		}
	}

	// TODO: do better handling if the object is wrong?
	static <S extends FileField> Integer getNullableInteger(FileTuple<S> source,
			S sourceKey) {
		return (Integer) removeArrayWrapper(source.get(sourceKey.getName()), null);
	}

	private static <S extends FileField> void putStringField(Dictionary<String, Object> dest,
			DBField destKey, FileTuple<S> source, S sourceKey) {
		dest.put(destKey.name(),
				cleanString(removeArrayWrapper(source.get(sourceKey.getName()), "")));
	}

	private static String cleanString(Object obj) {
		String raw = obj.toString();
		return StringUtilities.simpleClean(raw);
	}

	public static <K,V> Dictionary<K,V> newDictionary() {
		return new Hashtable<K,V>();
	}

}