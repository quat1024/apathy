#!/bin/sh
shopt -s failglob

alias rm="rm -v"
alias mkdir="mkdir -v"
alias cp="cp -v"
alias mv="mv -v"

rm -rf collect
mkdir collect
mkdir collect/common
mkdir collect/sources

cp ./**/build/libs/*.jar collect

mv ./collect/*sources*.jar collect/sources

mv ./collect/*crummyconfig*.jar collect/common
mv ./collect/*common*.jar collect/common
mv ./collect/*core*.jar collect/common