package com.logicaldoc.core.security;

import org.junit.Before;
import org.junit.Test;

import com.logicaldoc.core.AbstractCoreTCase;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.config.ContextProperties;

import junit.framework.Assert;

/**
 * Test case for the <code>SessionManager</code>
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.6
 */
public class SessionManagerTest extends AbstractCoreTCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testNewSession() {
		SessionManager sm = SessionManager.get();
		sm.clear();
		Session session1 = sm.newSession("admin", "admin", null);
		Assert.assertNotNull(session1);
		Session session2 = sm.newSession("admin", "admin", null);
		Assert.assertNotNull(session2);
		Assert.assertNotSame(session1, session2);
		Assert.assertEquals(2, sm.getSessions().size());
	}

	@Test
	public void testKill() {
		SessionManager sm = SessionManager.get();
		sm.clear();
		Session session1 = sm.newSession("admin", "admin", null);
		Assert.assertNotNull(session1);
		Session session2 = sm.newSession("admin", "admin", null);
		Assert.assertNotNull(session2);
		Assert.assertNotSame(session1, session2);
		Assert.assertEquals(2, sm.getSessions().size());

		sm.kill(session1.getSid());
		Assert.assertTrue(sm.isOpen(session2.getSid()));
		Assert.assertTrue(!sm.isOpen(session1.getSid()));
		Assert.assertEquals(2, sm.getSessions().size());
	}

	@Test
	public void testTimeout() {
		ContextProperties conf = Context.get().getProperties();
		int timeout = 1;
		conf.setProperty("default.session.timeout", "" + timeout);

		SessionManager sm = SessionManager.get();
		sm.clear();
		Session session1 = sm.newSession("admin", "admin", null);
		Assert.assertNotNull(session1);

		waiting(timeout);

		Assert.assertFalse(sm.isOpen(session1.getSid()));
	}
	
	private void waiting(int timeout) {
		synchronized (this) {
			try {
				wait(1000 * 60 * timeout);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
