## Donation Reminder

The donation reminder base code is a fork of the project mozilla-mobile/fenix

## Install

To put the Donation Reminder codebase on your machine, run:

git clone https://github.com/giveasyoulive/gayl_android.git

To synchronise the codebase with the main firefox project, run:

cd gayl_android

git remote add upstream git@github.com:mozilla-mobile/fenix.git * Only need to run this once

git checkout main

git fetch upstream

git merge upstream/main

git push origin main

We leave the main branch unchanged - so, we can easily synchronise the Firefox project without merge issues and test the unadulterated version if necessary.

You should then change to your working branch and merge in main

All Donation Reminder alterations will be done in other branches (Noe: The Donation Reminder main branch is donation_reminder - deploys should be done using this branch).

## Donation Reminder Extension

The donation reminder extension repository needs to be installed in /var/everyclick/development/donation-reminder.  

Follow the readme installation instructions and then run ./build firefox to create a distribution version of the extension.

Gradle picks up the latest files from the distribution directory

## Build variants

Specific variants have been created for the donation reminder project.  You can select the variant under the menu Build - select build variant

Please use:
donationReminderDebug
donationReminderRelease

## local.properties

Please add the follow line file
    autosignReleaseWithDebugKey

If you are running debug builds add the following line
    debuggable

## Signing
Keys and passwords are not stored in this repository.  You will need to create your own to deploy the Donation Reminder.


## License

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
