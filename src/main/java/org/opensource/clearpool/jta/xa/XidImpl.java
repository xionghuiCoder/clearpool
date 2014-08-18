package org.opensource.clearpool.jta.xa;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.xa.Xid;

/**
 * XidImpl used to represent a global unique transaction.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public final class XidImpl implements Xid {
	private static final int FORMAT_ID = 0xffff;

	private static final AtomicLong UNIQUE_LONG = new AtomicLong();

	// cache the hash code for Xid
	private int hash;

	byte[] bqual;

	int formatId;

	byte[] gtrid;

	public XidImpl() {
		this.bqual = this.buildUnique();
		this.gtrid = this.buildUnique();
		this.formatId = FORMAT_ID;
	}

	public XidImpl(Xid xid) {
		byte[] anoBqual = xid.getBranchQualifier();
		byte[] anoGtrid = xid.getGlobalTransactionId();
		this.bqual = Arrays.copyOf(anoBqual, anoBqual.length);
		this.gtrid = Arrays.copyOf(anoGtrid, anoGtrid.length);
		this.formatId = xid.getFormatId();
	}

	public XidImpl(byte[] bqual, int formatId, byte[] gtrid) {
		this.bqual = Arrays.copyOf(bqual, bqual.length);
		this.gtrid = Arrays.copyOf(gtrid, gtrid.length);
		this.formatId = formatId;
	}

	/**
	 * Build a unique bytes:year+UUID+unique_long.
	 */
	private byte[] buildUnique() {
		ByteBuffer uuidBys = ByteBuffer.allocate(24);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		uuidBys.putLong(year);
		uuidBys.putLong(UUID.randomUUID().node());
		long ul = UNIQUE_LONG.getAndIncrement();
		uuidBys.putLong(ul);
		return uuidBys.array();
	}

	@Override
	public byte[] getBranchQualifier() {
		return this.bqual;
	}

	@Override
	public int getFormatId() {
		return this.formatId;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		return this.gtrid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof XidImpl) {
			XidImpl anoXid = (XidImpl) obj;
			if (this.formatId == anoXid.formatId) {
				if (this.checkBytesEquals(this.bqual, anoXid.bqual)) {
					if (this.checkBytesEquals(this.gtrid, anoXid.gtrid)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check if byte[] is equaled
	 */
	private boolean checkBytesEquals(byte[] b1, byte[] b2) {
		int len = b1.length;
		if (len == b2.length) {
			for (int i = 0; i < len; i++) {
				if (b1[i] != b2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (this.hash == 0) {
			int h = this.formatId;
			for (int i = 0, len = this.bqual.length; i < len; i++) {
				h = 31 * h + this.bqual[i];
			}
			for (int i = 0, len = this.gtrid.length; i < len; i++) {
				h = 31 * h + this.gtrid[i];
			}
			this.hash = h;
		}
		return this.hash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{formatId:");
		builder.append(this.formatId);
		builder.append("\n");
		builder.append("bqual:");
		this.formatBytes(this.bqual, builder);
		builder.append("\n");
		builder.append("gtrid:");
		this.formatBytes(this.gtrid, builder);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Format the byte array.
	 */
	private void formatBytes(byte[] bytes, StringBuilder builder) {
		builder.append("[");
		for (byte b : bytes) {
			builder.append(b);
			builder.append(",");
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append("]");
	}
}
