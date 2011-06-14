MicroJIAC - A Lightweight Agent Framework (Version 3.0)
=======================================================

*	Meaning of "MicroJIAC"
*	Features of MicroJIAC
*	Deploying MicroJIAC
*	Building SunSPOT-related modules

* * *

Meaning of "MicroJIAC"
----------------------

"Micro" is supposed to mean small, lightweight and is similiar to the
meaning in "Java Microedition".
"JIAC" is a collective name and stands for "Java-based Intelligent Agent
Componentware".

For more information see:

*	[http://www.jiac.de]
*	[http://www.dai-labor.de]


Features of MicroJIAC
---------------------

*	low memory footprint
*	allows implementation of scalable and device independent agents
*	support for several Java environments
*	great extensibility
*	provides build and deployment utilities


Deploying MicroJIAC
-------------------

The deployment of MicroJIAC requires specific variables to be
available in your settings.xml. This is done by creating a profile
and declaring the following properties:

- **release-repo-id**
	id of a maven release repository

- **release-repo-url**
	and its deployment url

- **snapshot-repo-id**
	id of a maven snapshot repository

- **snapshot-repo-url**
	and its deployment url

For example, to deploy to the sonatype staging repositories, I 
specified the following:

```xml
	<!-- ... -->
	<profile>
		<id>sonatype</id>
		<properties>
			<release-repo-id>oss-sonatype-staging</release-repo-id>
			<release-repo-url>https://oss.sonatype.org/service/local/staging/deploy/maven2</release-repo-url>
			<snapshot-repo-id>oss-sonatype-snapshots</snapshot-repo-id>
			<snapshot-repo-url>https://oss.sonatype.org/content/repositories/snapshots</snapshot-repo-url>
		</properties>
	</profile>
	<!-- ... -->
```

Deploying the site require two more variables:

- **site-repo-id**
	id of a site repository

- **site-repo-url**
	and its deployment url


Building SunSPOT-related modules
--------------------------------

To build all SunSPOT related modules (like the microjiac-sunspot-extensions)
you have to install a recent SunSPOT SDK (red or later) and specify a
property in your settings.xml:

- **sunspot-sdk-path**
	the path to you SunSPOT SDK

For example, I specified the following:

```xml
	<!-- ... -->
	<activeProfiles>
		<activeProfile>global-properties</activeProfile>
	</activeProfiles>
	<profiles>
		<profile>
			<id>global-properties</id>
			<properties>
				<sunspot-sdk-path>/home/marcel/SunSPOT/red</sunspot-sdk-path>
			</properties>
		</profile>
	<!-- ... -->
```

If specified correctly all SunSPOT related modules will be automatically
included into the maven build.