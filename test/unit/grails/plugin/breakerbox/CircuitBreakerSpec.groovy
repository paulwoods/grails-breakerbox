package grails.plugin.breakerbox

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CircuitBreaker)
@Mock([CircuitBreaker,Message])
class CircuitBreakerSpec extends Specification {

	def setup() {
	}

	def cleanup() {
	}

	void "save fails if name is null"() {
		given:
		def cb1 = new CircuitBreaker(name:null)

		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"name" == cb1.errors.fieldError.field
		"nullable" == cb1.errors.fieldError.code
	}

	void "save fails if name is blank"() {
		given:
		def cb1 = new CircuitBreaker(name:"")

		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"name" == cb1.errors.fieldError.field
		"blank" == cb1.errors.fieldError.code
	}

	void "save succeeds if name is valid"() {
		given:
		def cb1 = new CircuitBreaker(name:"cb1")

		expect:
		cb1.save()
	}

	void "save fails if name is duplicated"() {
		given:
		def cb1 = new CircuitBreaker(name:"cb1")
		def cb2 = new CircuitBreaker(name:"cb1")

		expect:
		cb1.save()
		!cb2.save()

		and:
		1 == cb2.errors.errorCount
		"name" == cb2.errors.fieldError.field
		"unique" == cb2.errors.fieldError.code
	}

	void "toString is built correctly"() {
		given:
		def cb1 = new CircuitBreaker(name:"cb1")

		when:
		cb1.save()

		then:
		"CircuitBreaker[1] cb1" == cb1.toString()
	}

	void "tigger cannot be null"() {
		given:
		def cb1 = new CircuitBreaker(name:"cb1", trigger:null)

		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"trigger" == cb1.errors.fieldError.field
		"nullable" == cb1.errors.fieldError.code
	}

	void "minimum trigger value is 1"() {
		given:
		def cb1 = new CircuitBreaker(name:"cb1", trigger:0)
		def cb2 = new CircuitBreaker(name:"cb2", trigger:1)
		
		expect:
		!cb1.save()
		cb2.save()
	
		and:
		1 == cb1.errors.errorCount
		"trigger" == cb1.errors.fieldError.field
		"min.notmet" == cb1.errors.fieldError.code
	}

	void "the default trigger is 5"() {
		expect:
		5 == new CircuitBreaker().trigger
	}

	void "created circut breakers can be fetched"() {
		given:
		def cb1 = new CircuitBreaker(name:"cb1").save()

		when:
		def cb = CircuitBreaker.findByName("cb1")

		then:
		cb1 == cb
	}

	private create(String name, Integer trigger=5, Boolean resetOnVerify=false) {
		def cb = new CircuitBreaker(name:name,trigger:trigger, resetOnVerify:resetOnVerify).save()
		assert cb
		cb
	}

	void "protected code is executed"() {
		given:
		def cb1 = create("cb1")
		def a

		when:
		cb1.protect {
			a = 100
		}

		then:
		100 == a
	}

	void "protected code's return value is returned"() {
		given:
		def cb1 = create("cb1")

		when:
		def b = cb1.protect {
			200
		}

		then:
		200 == b
	}

	void "new circuit breakers count is zero"() {
		given:
		def cb1 = create("cb1")

		expect:
		0 == cb1.count
	}

	void "when protected code throws, the count is incremented"() {
		given:
		def cb1 = create("cb1")

		when:
		cb1.protect {
			throw new RuntimeException("Boom!")
		}

		then:
		1 == cb1.count
	}

	void "new breakers are not in the tripped state"() {
		given:
		def cb1 = create("cb1")

		expect:
		!cb1.tripped
	}

	void "breaker is tripped after it throws 'trigger' number of exceptions"() {
		given:
		def cb1 = create("cb1", 2)

		when:
		5.times {
			cb1.protect {
				throw new RuntimeException("Boom!")
			}
		}

		then:
		cb1.tripped
	}

	void "protected code stops running after it throws 'trigger' number of exceptions"() {
		given:
		def cb1 = create("cb1", 2)
		def timesRan = 0

		when:
		5.times {
			cb1.protect {
				timesRan++
				throw new RuntimeException("Boom!")
			}
		}

		then:
		2 == timesRan
	}

	void "unprotected code continues to run"() {
		given:
		def cb1 = create("cb1", 2)
		def countBefore = 0
		def countAfter = 0

		when:
		5.times {
			countBefore++
			cb1.protect {
				throw new RuntimeException("Boom!")
			}
			countAfter++
		}

		then:
		5 == countBefore
		5 == countAfter
	}

	void "reset resets the count"() {
		given:
		def cb1 = create("cb1", 2)

		cb1.protect {
			throw new RuntimeException("Boom!")
		}
		
		when:
		cb1.reset()

		then:
		0 == cb1.count
	}

	void "reset un-tripps a tripped breaker"() {
		given:
		def cb1 = create("cb1", 2)

		3.times {
			cb1.protect {
				throw new RuntimeException("Boom!")
			}
		}
		
		when:
		cb1.reset()

		then:
		!cb1.tripped
	}

	void "verifing a un-tripped breaker does not throw"() {
		given:
		def cb1 = create("cb1", 2)

		when:
		cb1.verify()

		then:
		notThrown(CircuitBreakerException)
	}

	void "verifing a tripped breaker throws a circuit breaker exception"() {
		given:
		def cb1 = create("cb1", 1)
		cb1.protect {
			throw new RuntimeException("Boom!")
		}

		when:
		cb1.verify()

		then:
		thrown(CircuitBreakerException)
	}

	void "the circuit breaker name is part of the exception message"() {
		given:
		def cb1 = create("cb1", 1)
		cb1.protect {
			throw new RuntimeException("Boom!")
		}

		when:
		cb1.verify()

		then:
		CircuitBreakerException e = thrown()
		"CircuitBreaker cb1 tripped" == e.message
	}

	void "the exception is retrievable"() {
		given:
		def cb1 = create("cb1", 1)
		cb1.protect {
			throw new RuntimeException("Boom!")
		}

		when:
		1 == cb1.messages.size()
		def message = cb1.messages[0]

		then:
		cb1 == message.circuitBreaker
		"Boom!" == message.name
		message.trace.contains("java.lang.RuntimeException: Boom!")
		message.trace.contains("at java.lang.reflect.Constructor.newInstance(Constructor.java:526)")
		message.trace.contains("at org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:77)")
	}

	void "multiple exceptions are retrievable"() {
		given:
		def cb1 = create("cb1", 2)
		cb1.protect {
			throw new RuntimeException("Boom 1!")
		}
		cb1.protect {
			throw new RuntimeException("Boom 2!")
		}

		when:
		2 == cb1.messages.size()

		then:
		println "0 = " + cb1.messages[0]
		println "1 = " + cb1.messages[1]

		"Boom 1!" == cb1.messages[0].name
		"Boom 2!" == cb1.messages[1].name
	}

	void "by default, breaker is not reset during verify"() {
		given:
		def cb1 = create("cb1", 1)
		cb1.protect {
			throw new RuntimeException("Boom!")
		}

		when:
		cb1.verify()

		then:
		CircuitBreakerException e = thrown()
		cb1.tripped
	}

	void "if resetOnVerify, breaker is reset during verify"() {
		given:
		def cb1 = create("cb1", 1, true)
		cb1.protect {
			throw new RuntimeException("Boom!")
		}

		when:
		cb1.verify()

		then:
		CircuitBreakerException e = thrown()
		!cb1.tripped
		0 == cb1.count
	}

}
