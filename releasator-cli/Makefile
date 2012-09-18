all:

clean:
	rm -rf target/wc target/repo

mkrepo:
	svnadmin create target/repo
	svn checkout file://$$PWD/target/repo target/wc
	svn mkdir target/wc/trunk target/wc/branches target/wc/tags
	cd target/wc && svn ci -m 'default layout'

fillrepo: mkrepo
	mkdir -p target
	svn export . target/wc/trunk/releasator
	cd target/wc/trunk;svn add *;svn ci -m 'cloned releasator'

rls:
	rm -rf target/tmp
	ant -v -buildfile test/antbased/releasator-core.xml

deploy:
	rsync -azi --delete target/dist/ bbuilder@b220.devlab.ad:opt/releasator-1-SNAPSHOT/
