# Orchestrator
Manage instances of game servers. Allows for automatic registration of game servers, manages their availability, and balances game requests across the network.

About
Entirely written in kotlin, connectivity between servers handled over TCP (Netty).

* [Hermes](https://github.com/utfunderscore/Hermes) - Event/Listener Driven network stack
* [Kryo](https://github.com/EsotericSoftware/kryo) - Object graph byte array serializer 
* [Javalin](https://github.com/javalin/javalin) - Rest HTTP server based on Jetty

