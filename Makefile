RELEASATOR=$(PWD)/bin/releasator.sh

all: test

mkrepo:
	rm -rf "target"
	mkdir "target/test"
	cp -a "samples/aimexample" "target/test"
	cd "target/test"; git init; git add -A .; git commit -am 'initial commit';

test:
	cd "target/test"; $(RELEASATOR) prepare "1.0.0"
