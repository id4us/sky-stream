#!/bin/sh
echo "" >> ./src/main/resources/version.properties
echo "project.name=cis-sstv-streaming" >>  ./src/main/resources/version.properties
echo "jenkins.build.number=$BUILD_NUMBER" >> ./src/main/resources/version.properties
echo "jenkins.build.id=$BUILD_ID" >> ./src/main/resources/version.properties
echo "jenkins.build.date=$(date)" >> ./src/main/resources/version.properties
echo "svn.revision=$SVN_REVISION" >> ./src/main/resources/version.properties
