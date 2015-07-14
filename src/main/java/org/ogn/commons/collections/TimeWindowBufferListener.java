/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.collections;

public interface TimeWindowBufferListener {
	void tick(final String msg, int elements);
}