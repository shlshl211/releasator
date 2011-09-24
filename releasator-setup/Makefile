

all:
#	svn up
	zer .
	scp target/releasator-setup-1-SNAPSHOT.jar bbuilder@b220.devlab.ad:tmp/
	ssh bbuilder@b220.devlab.ad 'java -jar tmp/releasator-setup-1-SNAPSHOT.jar; releasator --help'
