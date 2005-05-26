/*
 *  Created on Feb 3, 2005
 */
package org.gusdb.schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.gusdb.dbadmin.model.Database;
import org.gusdb.dbadmin.model.Schema;
import org.gusdb.dbadmin.model.Table;
import org.gusdb.dbadmin.model.GusTable;
import org.gusdb.dbadmin.model.Index;
import org.gusdb.dbadmin.reader.XMLReader
;
import org.gusdb.dbadmin.util.GusClassHierarchyConverter;
import org.gusdb.dbadmin.util.JDBCStreamWriter;
import org.gusdb.dbadmin.util.MetadataPopulator;

import org.gusdb.dbadmin.writer.PostgresWriter;
import org.gusdb.dbadmin.writer.OracleWriter;
import org.gusdb.dbadmin.writer.SchemaWriter;


/**
 *@author     msaffitz
 *@created    May 2, 2005
 *@version    $Revision$ $Date$
 */
public class InstallSchemaTask extends Task {

	private static Log log      = LogFactory.getLog( InstallSchemaTask.class );

	private Database db;
	private Connection conn;

	private String projectHome;
	private String schema;
	private String dbVendor;
	private String dbDsn;
	private String dbUsername;
	private String dbPassword;
	private String tablespace;

	public void setProjectHome( String projectHome ) {
		this.projectHome = projectHome;
	}


	public void setSchema( String schema ) {
		this.schema = schema;
	}


	public void execute() throws BuildException {
		initialize();

		XMLReader xr           = new XMLReader( schema );
		SchemaWriter dbWriter  = null;

		if ( dbVendor.compareToIgnoreCase( "Postgres" ) == 0 ) {
			dbWriter = new PostgresWriter();
		}
		else if ( dbVendor.compareToIgnoreCase( "Oracle" ) == 0 ) {
			dbWriter = new OracleWriter();
		}
		else {
			log.error( "Unknown DB Vendor: '" + dbVendor + "'" );
			throw new BuildException( "Unknown DB Vendor: '" + dbVendor + "'" );
		}

		log.info( "Reading database from " + schema );
		db = xr.read();

		FileWriter ddl;
		FileWriter rows;

		convertSubclasses( db );
		setTablespace( db );
		
		try {
			ddl = new FileWriter( projectHome + "/install/config/objects.sql" );
			rows = new FileWriter( projectHome + "/install/config/rows.sql" );

			JDBCStreamWriter rdbms  = new JDBCStreamWriter( conn );
			MetadataPopulator mp    = new MetadataPopulator( rows, db, dbVendor);

			dbWriter.write( ddl, db );
			dbWriter.write( rdbms, db );

			mp.writeDatabaseAndTableInfo();
			mp.writeBootstrapData();
			mp.writeDatabaseVersion("3.5");

			mp = new MetadataPopulator( rdbms, db, dbVendor );

			mp.writeDatabaseAndTableInfo();
			mp.writeBootstrapData();
			mp.writeDatabaseVersion("3.5");
			
			rows.close();
			ddl.close();
			rdbms.close();
		}
		catch ( IOException e ) {
			throw new BuildException( e );
		}
	}


	private void convertSubclasses( Database db ) {
		Collection superClasses  = new HashSet();
		Collection newTables     = new HashSet();

		for ( Iterator i = db.getSchemas().iterator(); i.hasNext();  ) {
		Schema schema  = (Schema) i.next();

			for ( Iterator j = schema.getTables().iterator(); j.hasNext();  ) {
			Table table  = (Table) j.next();

				if ( !table.getSubclasss().isEmpty() &&
					table.getClass() == GusTable.class ) {
					superClasses.add( table );
				}
			}
		}
		for ( Iterator i = superClasses.iterator(); i.hasNext();  ) {
			GusClassHierarchyConverter converter  = new GusClassHierarchyConverter( (GusTable) i.next() );

			converter.convert();
		}
	}

	private void setTablespace( Database db ) {
		log.info("Setting tablespaces to " + tablespace );
		for ( Iterator i = db.getSchemas().iterator(); i.hasNext();  ) {
			for ( Iterator j = ((Schema) i.next()).getTables().iterator(); j.hasNext(); ) {
				Table table = (Table) j.next();
				table.setTablespace(tablespace);
				
				if ( table.getClass() == GusTable.class ) {
					for ( Iterator k = ((GusTable)table).getIndexs().iterator(); k.hasNext(); ) {
						((Index) k.next()).setTablespace(tablespace);
					}
				}
			}
		}		
	}
	

	private void initialize() throws BuildException {
		System.setProperty( "XMLDATAFILE", schema );
		System.setProperty( "PROPERTYFILE", projectHome + "/install/gus.config" );

		Properties props  = new Properties();

		try {
			File propertyFile  = new File( System.getProperty( "PROPERTYFILE" ) );

			props.load( new FileInputStream( propertyFile ) );
		}
		catch ( IOException e ) {
			log.error( "Unable to get properties from gus.config", e );
			throw new BuildException( "Unable to get properties from gus.config", e );
		}

		this.dbVendor = props.getProperty( "dbVendor" );
		this.dbDsn = props.getProperty( "jdbcDsn" );
		this.dbUsername = props.getProperty( "databaseLogin" );
		this.dbPassword = props.getProperty( "databasePassword" );
		this.tablespace = props.getProperty( "tablespace" );
		
		try {
			Class.forName( "org.postgresql.Driver" );
			Class.forName( "oracle.jdbc.driver.OracleDriver" );
		}
		catch ( ClassNotFoundException e ) {
			log.fatal( "Unable to locate class", e );
			throw new BuildException( "Unable to locate class", e );
		}
		try {
			conn = DriverManager.getConnection( dbDsn, dbUsername, dbPassword );
		}
		catch ( SQLException e ) {
			log.error( "Unable to connect to database.  DSN='" + dbDsn + "'", e );
			throw new BuildException( "Unable to connect to database", e );
		}
	}

}

