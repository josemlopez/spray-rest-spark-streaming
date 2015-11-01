# Spray Actor Per Request REST API for Spark Streaming

This project is based in this other Project: https://github.com/NET-A-PORTER/spray-actor-per-request and try to be a simple
example of a REST API for events that will be send to Spark Streaming. 

Actor Per Request pattern has many advantages over other patterns based on ASK
If you want to know more about Actor Per Request: 

 * Net A Porter beautiful example: ([link](https://github.com/NET-A-PORTER/spray-actor-per-request))  
 * Scala Exchange Presentation ([video](http://skillsmatter.com/podcast/scala/scala-does-the-catwalk))
 * Mathias describes the actor per request approach against others.
   ([mailing list](https://groups.google.com/forum/#!msg/spray-user/5x9kba7j1FI/r_aaDTPWHFkJ))

## App

### Overview

This example application provides an API that let you send an Event to a (one or more) Spark Streaming Job(s) and show how
 you can manage a list of Jobs sending each type of event to the corresponding Spark Streaming Job. 

Is out of discussion that is far better to do this with a kafka, but sometimes you want to test a solution before enter in 
a deeper development.