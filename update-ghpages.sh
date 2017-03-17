#!/bin/bash
./gradlew dist || exit
cp -r doc /tmp/meja-javadoc
git checkout gh-pages
rm -r doc/
mv /tmp/meja-javadoc ./doc
git add ./doc
git commit -m "updated javadoc"

