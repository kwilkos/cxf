========= Default Key ====================

C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -genkey -alias defaultkey -keyalg RSA -storepass def
aultkeypass -keystore defaultkeystore -validity 10000
What is your first and last name?
  [Unknown]:  DefaultKey
What is the name of your organizational unit?
  [Unknown]:  DefaultUnit
What is the name of your organization?
  [Unknown]:  DefaultOrg
What is the name of your City or Locality?
  [Unknown]:  DefaultCity
What is the name of your State or Province?
  [Unknown]:  DefaultState
What is the two-letter country code for this unit?
  [Unknown]:  DF
Is CN=DefaultKey, OU=DefaultUnit, O=DefaultOrg, L=DefaultCity, ST=DefaultState,
C=DF correct?
  [no]:  y

Enter key password for <defaultkey>
        (RETURN if same as keystore password):


C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -import  -alias testdefaultkey -file defaultkey.cer
-keystore defaulttruststore -storepass defaulttruststorepass
Owner: CN=DefaultKey, OU=DefaultUnit, O=DefaultOrg, L=DefaultCity, ST=DefaultSta
te, C=DF
Issuer: CN=DefaultKey, OU=DefaultUnit, O=DefaultOrg, L=DefaultCity, ST=DefaultSt
ate, C=DF
Serial number: 44742adf
Valid from: Wed May 24 10:43:59 BST 2006 until: Sun Oct 09 10:43:59 BST 2033
Certificate fingerprints:
         MD5:  A4:04:DE:74:EC:8B:70:80:A0:75:4E:0D:08:D7:DF:54
         SHA1: A8:8D:57:74:29:1F:68:0C:0E:2B:A6:C5:1F:9C:0F:9F:85:CB:D4:87
Trust this certificate? [no]:  y
Certificate was added to keystore



========= Client Key ====================

C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -genkey -alias clientkey -keyalg RSA -storepass clie
ntpass -keystore .clientkeystore -validity 10000
What is your first and last name?
  [localhost]:
What is the name of your organizational unit?
  [Celtixorg]:  Celtixunit
What is the name of your organization?
  [CeltixOrg]:
What is the name of your City or Locality?
  [Celtixcity]:
What is the name of your State or Province?
  [Cestixstate]:
What is the two-letter country code for this unit?
  [CS]:
Is CN=localhost, OU=Celtixunit, O=CeltixOrg, L=Celtixcity, ST=Cestixstate, C=CS
correct?
  [no]:  y

Enter key password for <clientkey>
        (RETURN if same as keystore password):

C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -export  -alias clientkey -file clientkey.cer -store
pass clientpass -keystore .clientkeystore
Certificate stored in file <clientkey.cer>

C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -import  -alias testclientkey -file clientkey.cer -k
eystore truststore -storepass truststorepass
Owner: CN=localhost, OU=Celtixunit, O=CeltixOrg, L=Celtixcity, ST=Cestixstate, C
=CS
Issuer: CN=localhost, OU=Celtixunit, O=CeltixOrg, L=Celtixcity, ST=Cestixstate,
C=CS
Serial number: 44742bed
Valid from: Wed May 24 10:48:29 BST 2006 until: Sun Oct 09 10:48:29 BST 2033
Certificate fingerprints:
         MD5:  85:DE:A5:1C:BC:27:69:C3:77:31:8D:24:9E:71:47:46
         SHA1: 69:EC:A5:7E:67:53:E4:0F:40:00:15:58:04:23:DE:EF:94:55:85:BA
Trust this certificate? [no]:  y
Certificate was added to keystore

===== Server Key =======

C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>cp .clientkeystore .serverkeystore
