RELEASATOR=$(PWD)/bin/releasator.sh
TESTDIR=$(PWD)/target/test

all: test

mkrepo:
	rm -rf "$(TESTDIR)"
	mkdir -p "$(TESTDIR)"
	cd "samples/aimexample"; cp -a -t "$(TESTDIR)" * .??*
	cd "$(TESTDIR)"; git init; git add -A .; git commit -am 'initial commit';

test: mkrepo
	cd "$(TESTDIR)"; $(RELEASATOR) prepare "1.0.0"

setup:
	ln -s $(PWD)/bin/releasator.sh $(HOME)/bin/releasator
