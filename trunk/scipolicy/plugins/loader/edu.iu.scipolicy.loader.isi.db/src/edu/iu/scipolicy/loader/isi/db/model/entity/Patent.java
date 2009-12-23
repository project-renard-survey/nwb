package edu.iu.scipolicy.loader.isi.db.model.entity;

import java.util.Dictionary;
import java.util.Hashtable;

import org.cishell.utilities.StringUtilities;

import edu.iu.cns.database.loader.framework.Entity;
import edu.iu.cns.database.loader.framework.utilities.DatabaseTableKeyGenerator;
import edu.iu.nwb.shared.isiutil.database.ISIDatabase;

public class Patent extends Entity<Patent> {
	private String patentNumber;

	public Patent(DatabaseTableKeyGenerator keyGenerator, String patentNumber) {
		super(keyGenerator, createAttributes(patentNumber));
		this.patentNumber = patentNumber;
	}

	public String getPatentNumber() {
		return this.patentNumber;
	}

	public boolean shouldMerge(Patent otherPatent) {
		return StringUtilities.validAndEquivalent(
			this.patentNumber, otherPatent.getPatentNumber());
	}

	public void merge(Patent otherPatent) {
	}

	public static Dictionary<String, Comparable<?>> createAttributes(String patentNumber) {
		Dictionary<String, Comparable<?>> attributes = new Hashtable<String, Comparable<?>>();
		attributes.put(ISIDatabase.PATENT_NUMBER, patentNumber);

		return attributes;
	}
}