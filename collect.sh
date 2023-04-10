#!/bin/sh

# dig out all the stuff from the various ./build/libs folders
rm -rfv collect
mkdir -pv collect
cp -v ./**/build/libs/* collect

# move the "common" artifacts into their own folder because they're not super useful
mkdir -v ./collect/common
mv -v ./collect/*common*.jar ./collect/common
mv -v ./collect/*core*.jar ./collect/common

# ditto for sources artifacts
mkdir -v ./collect/sources
mv -v ./collect/*-sources.jar ./collect/sources