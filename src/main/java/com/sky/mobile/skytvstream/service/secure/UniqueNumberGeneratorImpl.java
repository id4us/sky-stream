package com.sky.mobile.skytvstream.service.secure;

import org.springframework.stereotype.Service;

@Service
public class UniqueNumberGeneratorImpl implements UniqueNumberGenerator {
	
	private Long initial;

	public UniqueNumberGeneratorImpl() {
		initial = new Long(0);
	}

	/**TODO: make this random yet unique within a time period of two mins**/
	@Override
	public synchronized String getId() {		
		if (initial > (Long.MAX_VALUE - 10)) {
			initial = new Long(0);
		}
		initial++;
		return initial.toString();		
	}
}
