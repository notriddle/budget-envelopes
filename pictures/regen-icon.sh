#!/bin/sh
#
# This file is a part of Budget with Envelopes.
# Copyright 2013 Michael Howell <michael@notriddle.com>
#
# Budget is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Budget is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Budget. If not, see <http://www.gnu.org/licenses/>.

inkscape -e ../res/drawable-mdpi/ic_launcher.png -w 48 -h 48 -z ic_launcher.svg
inkscape -e ../res/drawable-hdpi/ic_launcher.png -w 72 -h 72 -z ic_launcher.svg
inkscape -e ../res/drawable-xhdpi/ic_launcher.png -w 96 -h 96 -z ic_launcher.svg
inkscape -e ../res/drawable-xxhdpi/ic_launcher.png -w 144 -h 144 -z ic_launcher.svg

for i in color_menuitem spend_menuitem earn_menuitem transfer_menuitem; do
	inkscape -e ../res/drawable-mdpi/$i.png -w 32 -h 32 -z $i.svg
	inkscape -e ../res/drawable-hdpi/$i.png -w 48 -h 48 -z $i.svg
	inkscape -e ../res/drawable-xhdpi/$i.png -w 64 -h 64 -z $i.svg
    #inkscape -e res/drawable-xxhdpi/$i.png -w 192 -h 192 -z $i.svg
done
