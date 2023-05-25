package com.logicaldoc.util.config;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.logicaldoc.util.io.FileUtil;

import junit.framework.Assert;

/**
 * Test case for <code>WebConfigurator</code>
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.5
 */
public class ContextConfiguratorTest {

	File contextXml = new File("target/context.xml");

	@Before
	public void setUp() throws Exception {
		FileUtil.copyResource("/context.xml", contextXml);
	}

	@Test
	public void testAddTrigger() {
		String notThrownTest = null;
		try {
			ContextConfigurator config = new ContextConfigurator(contextXml.getPath());
			config.addTrigger("TestTrigger");
			notThrownTest = "ok";
		} catch (Exception t) {
			// Nothing to do
		}
		Assert.assertNotNull(notThrownTest);
	}
}