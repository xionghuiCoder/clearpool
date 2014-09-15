package org.opensource.clearpool.core.chain;

import org.opensource.clearpool.log.PoolLog;
import org.opensource.clearpool.log.PoolLogFactory;

/**
 * This factory used to create atomic chain or lock chain wisely.
 * 
 * @author xionghui
 * @date 16.08.2014
 * @version 1.0
 */
public class ChainFactory {
	private static final PoolLog LOG = PoolLogFactory
			.getLog(ChainFactory.class);

	private static final String CLAZZ = "sun.misc.Unsafe";

	private static boolean unsafeExist;

	static {
		try {
			Class.forName(CLAZZ);
			unsafeExist = true;
		} catch (ClassNotFoundException e) {
			LOG.info("\"" + CLAZZ + "\" is not existed");
		}
	}

	public static <E> CommonChain<E> createSingleChain(E e) {
		if (unsafeExist) {
			return new AtomicSingleChain<E>();
		}
		return new LockSingleChain<E>();
	}

	public static <E> CommonChain<E> createCircleChain(E e) {
		return new LockCircleChain<E>();
	}
}
