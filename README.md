# Genomics Unified Schema (GUS)

Definition files for a relational database schema that has been deployed in Oracle and PostgreSQL

See original documentation [here](https://www.cbil.upenn.edu/node/86).

## Dependencies

   + yarn / npm / ant
   + environment variables for GUS_HOME and PROJECT_HOME

## Installation instructions

   + [EuPathDB-DevTools | Installing the GUS and ApiDB Schemas](https://docs.google.com/document/d/1w8DJPMoNh31cTSStuDjlNoZDFj3A4SfX6z0eNH0TsNw/edit#heading=h.hs6rwi3xu3ea)

## Operating instructions

   + the standard GUS schemas: CORE, DoTS, SRes, etc.
   + the GUS Perl and Java object layers (objects that represent the standard GUS tables)

## Manifest

   + Definition/bin :: scripts to generate XML files that can be used with LoadGusXml plugin or describing tables, columns, primary keys, and foreign keys
   + Definition/config :: GUS schema definition files
   + images :: diagrams of standard GUS schemas 



<!--
Gus Schema
==========

These figures describe the 'sres' and 'study' schemas:
<br><img alt="GUS4 sres schema" src="images/GUS4-1.pdf" width=50/> 
<br><img alt="GUS4 study schema" src="images/GUS4-3.pdf" width=50/>
-->

