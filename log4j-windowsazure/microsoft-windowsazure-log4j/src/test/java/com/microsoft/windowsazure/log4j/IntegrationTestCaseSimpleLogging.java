package com.microsoft.windowsazure.log4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class IntegrationTestCaseSimpleLogging {

	private static final Log logger = LogFactory.getLog(IntegrationTestCaseSimpleLogging.class);
	
	@Test
	public void testSimpleDebugMessage() {
		logger.debug("This is a sample debug message");
	}

}
