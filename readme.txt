

A circuit breaker is a object that can stop a section of code from executing.
It keeps track of the number of exceptions it throws. If it throws too many
then the breaker is considered tripped, and will stop the section of 
code from running.





requirements
circuit breaker protects code from throwing too many exceptions
circuit breakers have names so multiple can be used in an app.
each breaker is programmable to the number of exceptions before tripping.


definitions
protected code - a section of code wrapped in a closure that will execute only
when the breaker is not tripped


tripped - a breaker state that stops the protected code from running.

trigger - the number of exceptions that will occurr before the breaker is tripped.

reset - the act of un-tripping the breaker, and allowing the protected code to run.





create a breaker box

create a breaker from the box

https://code.google.com/p/spock/wiki/SpockBasics





=========================================================================


# create the plugin

grails create-plugin grails-breakerbox




# configure for spock tests



in buildconfig
	add dependency: 
	test "org.spockframework:spock-grails-support:0.7-groovy-2.0"



add spock plugin
	test(":spock:0.7") {
	    exclude "spock-grails-support"
	}


create the domain
	create-domain-class grails.plugin.breakerbox.CircuitBreaker






# create the service

create-service grails.plugin.breakerbox.BreakerBox


