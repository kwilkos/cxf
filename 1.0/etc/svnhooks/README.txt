This directory contains the subversion hooks that the Celtix project
uses. Installing new versions of the hooks requires the use of "scp".

I don't know if this can be done from Windows.  The scripts MUST have 
the executable bit set on the server for the scripts to work.  scp on 
Linux will keep that bit set.

scp commit-email.pl username@svn.forge.objectweb.org:/svnroot/celtix/hooks
scp post-commit username@svn.forge.objectweb.org:/svnroot/celtix/hooks

