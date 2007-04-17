#
# This file generates a number of keys/certificates and keystores for 
# names to be used with corresponding CXF configuration files (*.cxf).
#

#
# Start with a clean slate. Remove all keystores.
#
rm -f *.jks

#
# This function generates a key/self-signed certificate with the following DN.
#  "CN=$1, OU=$2, O=ApacheTest, L=Syracuse, C=US" and adds it to 
# the truststore.
#
function genkey {
    keytool -genkey -alias $2 -keystore $2.jks -dname "CN=$1, OU=$2, O=ApacheTest, L=Syracuse, C=US" -keyalg RSA -keypass password -storepass password -storetype jks -validity 10000
    keytool -export -file $2.cer -alias $2 -keystore $2.jks -storepass password
    keytool -import -file $2.cer -alias $2 -noprompt -keystore Truststore.jks -storepass password
}

#
# We generate keys/certificates with the following CN=<name> OU=<name>
# The CN used to be "localhost" to conform to the default HostnameVerifier of
# HttpsURLConnection so it would work for tests. However, we have enhanced
# the HTTP Conduit logic to accept anything in the CN in favor of the 
# MessageTrustDecider callback making the verification determination.
#
for name in Bethal Gordy Tarpin Poltim Morpit
do
   genkey $name $name
done

