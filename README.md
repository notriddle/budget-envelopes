Budget with envelopes
======================================
Take control of your money. It's easy.
--------------------------------------

Avoid overdraft fees, unexpected shortages, the minimum payment treadmill, and that sinking dread when you realize you actually couldn't afford to go out.

That's what Budget is for, and it's easy. You put money into the envelopes at payday, and take it out as you spend. An envelope can represent anything — a bank account, money set aside for groceries, or the cash you're saving to buy that new Android phone off-contract. Think of a checkbook, only more flexible.

Managing money is a drag, but it doesn't have to be painful or difficult.


Compiling
=========

TL;DR : Just like every other Android project.


Debug
-----

To compile in debug mode, run this the first time:

    android update project -p .

And run this every time.

    ant clean
    ant debug


Release
-------

Making a release build is a bit harder, because you need a release key. A howto is available at <https://developer.android.com/tools/publishing/app-signing.html>. Here's an example for generating the key (only do this once):

    android update project -p . # Unless you've already done it, of course.
    keytool -genkey -keystore budget.keystore -alias budget -keyalg RSA -keysize 2048 -validity 10000
    echo "key.store=budget.keystore" > ant.properties
    echo "key.alias=budget" >> ant.properties

And to actually build the program:

    ant clean
    ant release

