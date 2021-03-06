// Copyright (c) 2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * X.400 mail mapping record.
 * 
 * @author Brian Wellington
 */

public class PXRecord extends Record {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1811540008806660667L;

	/** The preference. */
	private int preference;

	/** The map822. */
	private Name map822;

	/** The map x400. */
	private Name mapX400;

	/**
	 * Instantiates a new pX record.
	 */
	PXRecord() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Record#getObject()
	 */
	Record getObject() {
		return new PXRecord();
	}

	/**
	 * Creates an PX Record from the given data.
	 * 
	 * @param name
	 *            the name
	 * @param dclass
	 *            the dclass
	 * @param ttl
	 *            the ttl
	 * @param preference
	 *            The preference of this mail address.
	 * @param map822
	 *            The RFC 822 component of the mail address.
	 * @param mapX400
	 *            The X.400 component of the mail address.
	 */
	public PXRecord(Name name, int dclass, long ttl, int preference,
			Name map822, Name mapX400) {
		super(name, Type.PX, dclass, ttl);

		this.preference = checkU16("preference", preference);
		this.map822 = checkName("map822", map822);
		this.mapX400 = checkName("mapX400", mapX400);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Record#rrFromWire(org.xbill.DNS.DNSInput)
	 */
	void rrFromWire(DNSInput in) throws IOException {
		preference = in.readU16();
		map822 = new Name(in);
		mapX400 = new Name(in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Record#rdataFromString(org.xbill.DNS.Tokenizer,
	 * org.xbill.DNS.Name)
	 */
	void rdataFromString(Tokenizer st, Name origin) throws IOException {
		preference = st.getUInt16();
		map822 = st.getName(origin);
		mapX400 = st.getName(origin);
	}

	/**
	 * Converts the PX Record to a String.
	 * 
	 * @return the string
	 */
	String rrToString() {
		StringBuffer sb = new StringBuffer();
		sb.append(preference);
		sb.append(" ");
		sb.append(map822);
		sb.append(" ");
		sb.append(mapX400);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Record#rrToWire(org.xbill.DNS.DNSOutput,
	 * org.xbill.DNS.Compression, boolean)
	 */
	void rrToWire(DNSOutput out, Compression c, boolean canonical) {
		out.writeU16(preference);
		map822.toWire(out, null, canonical);
		mapX400.toWire(out, null, canonical);
	}

	/**
	 * Gets the preference of the route.
	 * 
	 * @return the preference
	 */
	public int getPreference() {
		return preference;
	}

	/**
	 * Gets the RFC 822 component of the mail address.
	 * 
	 * @return the map822
	 */
	public Name getMap822() {
		return map822;
	}

	/**
	 * Gets the X.400 component of the mail address.
	 * 
	 * @return the map x400
	 */
	public Name getMapX400() {
		return mapX400;
	}

}
