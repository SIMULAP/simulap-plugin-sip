//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.utils;

public class DialogCreatingRequests {

	private enum CreatingRequests {	REGISTER, NOTIFY, PUBLISH }
	
	public static boolean isCreating ( String aMethod) {
		try {
			CreatingRequests.valueOf(aMethod);
			return false;
		} catch (Exception e) {
			return true;
		}
	}
	
}
