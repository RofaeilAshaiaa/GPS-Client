GPS client

this translates (at least initially) into an android project called glient.  this is a 'gradle wrapper' project
it currently has 1 module, called gactivity, which renders the buttons and displays the lat and long and also directly calls the google play services API
eventually there will be the following modules in here

1. G a module which contains my fully javadoc'ed library API. in this module there will be the usual unit test folder (test) and the usual android integration folder too (androidTest) and the main library will be in the 'main' folder
2. gactivity - this will be the 'on phone' activity which exercises the G library live on a phone

The code for the geofencing which you will find in the gactivity can eventually
be commented out, as I was only looking to see how geofencing worked in the
google play services api.  I don't have any initial plan to incorporate 
this code into the G library or use it in the current project