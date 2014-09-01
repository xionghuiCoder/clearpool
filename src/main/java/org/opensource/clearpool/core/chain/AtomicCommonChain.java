package org.opensource.clearpool.core.chain;

import java.lang.reflect.Field;

import org.opensource.clearpool.exception.ConnectionPoolException;

import sun.misc.Unsafe;

/**
 * This class used to init Unsafe for atomic chain.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
abstract class AtomicCommonChain<E> extends CommonChain<E> {
	protected static final Unsafe UNSAFE;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
		} catch (Exception e) {
			throw new ConnectionPoolException("create Unsafe error");
		}
	}
}
