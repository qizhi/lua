
Run with:
$ mvn clean package firebase:run \
        -Dcom.cubeia.network.userservice.base-url=http://localhost:9090/user-service-rest/rest \
        -Dcom.cubeia.network.walletservice.base-url=http://localhost:9091/wallet-service-rest/rest \

Where the property "com.cubeia.network.userservice.base-url" should point to your user service instance,
"com.cubeia.network.walletservice.base-url" to your wallet service instance.



