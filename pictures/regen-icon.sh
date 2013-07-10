#!/bin/sh
inkscape -e ../res/drawable-mdpi/ic_launcher.png -w 48 -h 48 -z ic_launcher.svg
inkscape -e ../res/drawable-hdpi/ic_launcher.png -w 72 -h 72 -z ic_launcher.svg
inkscape -e ../res/drawable-xhdpi/ic_launcher.png -w 96 -h 96 -z ic_launcher.svg
inkscape -e ../res/drawable-xxhdpi/ic_launcher.png -w 144 -h 144 -z ic_launcher.svg

for i in spend_menuitem paycheck_menuitem transfer_menuitem; do
	inkscape -e ../res/drawable-mdpi/$i.png -w 32 -h 32 -z $i.svg
	inkscape -e ../res/drawable-hdpi/$i.png -w 48 -h 48 -z $i.svg
	inkscape -e ../res/drawable-xhdpi/$i.png -w 64 -h 64 -z $i.svg
    #inkscape -e res/drawable-xxhdpi/$i.png -w 192 -h 192 -z $i.svg
done
