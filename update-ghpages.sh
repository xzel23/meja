#!/bin/bash
./gradlew dist || exit
cp -r doc /tmp/meja-javadoc
git checkout gh-pages
rm -r javadoc
mv /tmp/meja-javadoc ./javadoc
git add javadoc
git commit -m "updated javadoc"
