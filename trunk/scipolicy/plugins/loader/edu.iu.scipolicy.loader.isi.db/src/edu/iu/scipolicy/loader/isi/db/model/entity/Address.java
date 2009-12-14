package edu.iu.scipolicy.loader.isi.db.model.entity;

import java.util.Dictionary;
import java.util.Hashtable;


import edu.iu.cns.database.loader.framework.Entity;
import edu.iu.cns.database.loader.framework.utilities.DatabaseTableKeyGenerator;
import edu.iu.scipolicy.loader.isi.db.ISIDatabase;

public class Address extends Entity {
	private String streetAddress;
	private String city;
	private String stateOrProvince;
	private String postalCode;
	private String country;
	private String rawAddress;

	public Address(DatabaseTableKeyGenerator keyGenerator, String rawAddress) {
		this(keyGenerator, "", "", "", "", "", rawAddress);
	}

	public Address(
			DatabaseTableKeyGenerator keyGenerator,
			String streetAddress,
			String city,
			String stateOrProvince,
			String postalCode,
			String country,
			String rawAddress) {
		super(keyGenerator);
		this.streetAddress = streetAddress;
		this.city = city;
		this.stateOrProvince = stateOrProvince;
		this.postalCode = postalCode;
		this.country = country;
		this.rawAddress = rawAddress;
	}

	public String getStreetAddress() {
		return this.streetAddress;
	}

	public String getCity() {
		return this.city;
	}

	public String getStateOrProvince() {
		return this.stateOrProvince;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public String getCountry() {
		return this.country;
	}

	public String getRawAddress() {
		return this.rawAddress;
	}

	public Dictionary<String, Object> createAttributes() {
		Dictionary<String, Object> attributes = new Hashtable<String, Object>();
		attributes.put(ISIDatabase.STREET_ADDRESS, this.streetAddress);
		attributes.put(ISIDatabase.ADDRESS_CITY, this.city);
		attributes.put(ISIDatabase.STATE_OR_PROVINCE, this.stateOrProvince);
		attributes.put(ISIDatabase.POSTAL_CODE, this.postalCode);
		attributes.put(ISIDatabase.COUNTRY, this.country);
		attributes.put(ISIDatabase.RAW_ADDRESS, this.rawAddress);

		return attributes;
	}
}