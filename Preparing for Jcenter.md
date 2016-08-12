#Preparing SDK for Release

##Bintray Account

1. First, setup a Bintray account. Create a new package under the maven repo
2. Enter all required information for the new package
3. Under your profile, there should be an API key. Save it for later



##Android Studio

1. In the root build.gradle file, add 

	```
	classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.4'
	classpath 'com.github.dcendents:android-maven-gradle-plugin:1.3'
	```

2. In the sdk module's build.gradle, underneath the first library apply plugin, add in

	```
	apply plugin: 'com.github.dcendents.android-maven'
	apply plugin: 'com.jfrog.bintray'
	```

3. If, when building, there is a lint error, add in the following underneath the `android` block

	```
	lintOptions {
	        abortOnError false
	    }
	```

4. Define a bintray block. Fill in the appropriate fields. Add this to the sdk module's build.gradle

	```
	def siteUrl = 'http://airmap.com'
	bintray {
	    user = 'your_username'
	    key = 'your_api_key'
	    configurations = ['archives']
	    pkg {
	        repo = 'maven'
	        version 'insert_version_#_here'
	        name = 'insert_name'
	        desc = 'insert_description'
	        licenses = ['Apache-2.0']
	        labels = ['aar', 'android', 'airmap']
	    }
	}
	```

5. For JCenter to accept the library, it requires a POM file. Add this to the sdk module's build.gradle 
	
	```
	install {
	    repositories.mavenInstaller {
	        pom.project {
	                packaging 'aar'
	                name 'insert_name'
	                url siteUrl
	                licenses {
	                    license {
	                        name 'The Apache Software License, Version 2.0'
	                        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
	                    }
	                }
	                developers {
	                    developer {
	                        id 'your_id'
	                        name 'your_name'
	                        email 'your_email'
	                    }
	                }
	                scm {
	                    connection 'insert_git_link'
	                    developerConnection 'insert_git_link'
	                    url siteUrl
	
	                }
	        }.writeTo("$buildDir/pom.xml")
	    }
	}
	```
	
6. JCenter also needs a Javadoc and sources Jar. Gradle manages all that as well. Add this to the sdk module's build.gradle

	```
	task sourcesJar(type: Jar) {
	    from android.sourceSets.main.java.srcDirs
	    classifier = 'sources'
	}
	
	task javadoc(type: Javadoc) {
	    source = android.sourceSets.main.java.srcDirs
	    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
	}
	
	task javadocJar(type: Jar, dependsOn: javadoc) {
	    classifier = 'javadoc'
	    from javadoc.destinationDir
	}
	
	artifacts {
	    archives javadocJar
	    archives sourcesJar
	}
	```

7. In the command line, navigate to the project directory and run `./gradlew install` and then `./gradlew bintrayUpload`

8. The library should be on Bintray now. Navigate to the project on Bintray, and publish the library.
9. Then, click the button on the bottom right to add to JCenter
10. `./gradlew install` also installs the repo to your local maven repository (which should be located at `~/.m2/repository`). You can use it by adding `mavenLocal()` to your `repositories` block in your project's `build.gradle`


##Relevant Links

`https://github.com/bintray/gradle-bintray-plugin#readme`
`https://inthecheesefactory.com/blog/how-to-upload-library-to-jcenter-maven-central-as-dependency/en`
`https://bintray.com/`