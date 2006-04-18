C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -genkey -alias defaultkey -keyalg RSA -storepass def
aultkeypass -keystore defaultkeystore
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
systest\securebasic>keytool -export  -alias defaultkey -file defaultkey.cer -sto
repass defaultkeypass -keystore defaultkeystore
Certificate stored in file <defaultkey.cer>

C:\CeltixDevelopmement\trunk\celtix-systests\src\test\java\org\objectweb\celtix\
systest\securebasic>keytool -import  -alias testdefaultkey -file defaultkey.cer
-keystore defaulttruststore
Enter keystore password:  defaulttruststorepass
Owner: CN=DefaultKey, OU=DefaultUnit, O=DefaultOrg, L=DefaultCity, ST=DefaultSta
te, C=DF
Issuer: CN=DefaultKey, OU=DefaultUnit, O=DefaultOrg, L=DefaultCity, ST=DefaultSt
ate, C=DF
Serial number: 443631c3
Valid from: Fri Apr 07 10:32:51 BST 2006 until: Thu Jul 06 10:32:51 BST 2006
Certificate fingerprints:
         MD5:  AC:8A:2C:65:EB:6E:C6:18:F9:C8:4B:24:08:A8:EC:7F
         SHA1: D9:7D:08:AB:18:EC:33:D2:AB:EA:7B:1B:62:D8:7B:8F:5F:B9:BB:66
Trust this certificate? [no]:  y
Certificate was added to keystore
