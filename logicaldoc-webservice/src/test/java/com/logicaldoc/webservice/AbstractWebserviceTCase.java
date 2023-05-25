package com.logicaldoc.webservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlTool.SqlToolException;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.Assert;

/**
 * Abstract test case for the Web Service module. This class initialises a test
 * database and prepares the spring test context.
 * <p>
 * 
 * @author Matteo Caruso - LogicalDOC
 * @since 5.2
 */
public abstract class AbstractWebserviceTCase {

	protected ApplicationContext context;

	protected DataSource ds;

	protected File tempDir = new File("target/tmp");

	protected File coreSchemaFile;

	protected File wsSchemaFile;

	protected File dataFile;

	private String userHome;

	@Before
	public void setUp() throws Exception {
		System.setProperty("solr.http1", "true");
		
		userHome = System.getProperty("user.home");
		System.setProperty("user.home", tempDir.getPath());
		createTestDirs();
		
		context = new ClassPathXmlApplicationContext(new String[] { "/context.xml" });
		createTestDatabase();
	}

	protected void createTestDirs() throws IOException {
		// Create test dirs
		if (tempDir.exists() && tempDir.isDirectory())
			FileUtils.deleteDirectory(tempDir);
		tempDir.mkdirs();

		Assert.assertTrue(tempDir.exists() && tempDir.isDirectory());

		coreSchemaFile = new File(tempDir, "logicaldoc-core.sql");
		wsSchemaFile = new File(tempDir, "logicaldoc-webservice.sql");
		dataFile = new File(tempDir, "data.sql");

		// Copy sql files
		copyResource("/sql/logicaldoc-core.sql", coreSchemaFile.getCanonicalPath());
		copyResource("/sql/logicaldoc-webservice.sql", wsSchemaFile.getCanonicalPath());
		copyResource("/data.sql", dataFile.getCanonicalPath());
	}

	protected void copyResource(String classpath, String destinationPath) throws IOException {
		copyResource(classpath, new File(destinationPath));
	}

	/**
	 * Copy a resource from the classpath into a file
	 * 
	 * @param classpath The classpath specification
	 * @param out The target file
	 * @throws IOException
	 */
	protected void copyResource(String classpath, File out) throws IOException {
		InputStream is = new BufferedInputStream(this.getClass().getResource(classpath).openStream());
		OutputStream os = new BufferedOutputStream(new FileOutputStream(out));
		try {
			for (;;) {
				int b = is.read();
				if (b == -1)
					break;
				os.write(b);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	@After
	public void tearDown() throws Exception {
		destroyDatabase();
		try {
			((AbstractApplicationContext) context).close();
		} catch (Exception t) {
			// Nothing to do
		}
		// Restore user home system property
		System.setProperty("user.home", userHome);
	}

	/**
	 * Destroys the in-memory database
	 * 
	 * @throws SQLException error at database level
	 */
	private void destroyDatabase() throws SQLException {
		Connection con = null;
		try {
			con = ds.getConnection();
			con.createStatement().execute("shutdown");
		} catch (Exception e) {
			if (con != null)
				con.close();
			e.printStackTrace();
		}
	}

	/**
	 * Creates an in-memory test database
	 * 
	 * @throws SqlToolException
	 * 
	 */
	private void createTestDatabase() throws Exception {
		ds = (DataSource) context.getBean("DataSource");

		Connection con = null;
		try {
			con = ds.getConnection();

			// Load schema
			SqlFile sqlFile = new SqlFile(coreSchemaFile, "Cp1252", false);
			sqlFile.setConnection(con);
			sqlFile.execute();

			// Load webservice
			sqlFile = new SqlFile(wsSchemaFile, "Cp1252", false);
			sqlFile.setConnection(con);
			sqlFile.execute();

			// Load data
			sqlFile = new SqlFile(dataFile, "Cp1252", false);
			sqlFile.setConnection(con);
			sqlFile.execute();

			// Test the connection
			ResultSet rs = con.createStatement().executeQuery("select * from ld_menu where ld_id=2");
			rs.next();

			Assert.assertEquals(2, rs.getInt(1));
		} finally {
			if (con != null)
				con.close();
		}
	}

	// To avoid error on maven test
	public void testDummy() throws Exception {
		Connection con = null;
		try {
			con = ds.getConnection();
			// Test the connection
			ResultSet rs = con.createStatement().executeQuery("select * from ld_menu where ld_id=1");
			rs.next();

			Assert.assertEquals(1, rs.getInt(1));
		} finally {
			if (con != null)
				con.close();
		}
	}
}