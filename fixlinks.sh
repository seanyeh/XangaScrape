#!/bin/sh

for file in *.html; do 
    newnum=$(( `echo $file | grep -Po '\d+'` + 1 ))
    sed -i "s/[^\"]*direction=n[^\"]*/page${newnum}.html/g" $file
done

for file in *.html; do 
    newnum=$(( `echo $file | grep -Po '\d+'` - 1 ))
    sed -i "s/[^\"]*direction=p[^\"]*/page${newnum}.html/g" $file
done

for file in *.html; do 
    num=`echo $file | grep -Po '\d+'`
    sed -i -r "s/[^\"]*${1}.xanga.com\/([0-9]+)[^\"]*/page${num}\/\1.html/g" $file
done
