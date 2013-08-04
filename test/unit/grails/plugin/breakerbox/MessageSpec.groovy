package grails.plugin.breakerbox

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Message)
@Mock([CircuitBreaker,Message])
class MessageSpec extends Specification {

	def cb1 

	def setup() {
		cb1 = new CircuitBreaker(name:"cb1").save()
		assert cb1
	}

	def cleanup() {
	}

	void "name can't be null"() {
		given:
		def m1 = new Message(name:null, trace:"x")
		cb1.addToMessages m1
		
		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"messages[0].name" == cb1.errors.fieldError.field
		"nullable" == cb1.errors.fieldError.code
	}
	
	void "name can't be blank"() {
		given:
		def m1 = new Message(name:"", trace:"x")
		cb1.addToMessages m1

		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"messages[0].name" == cb1.errors.fieldError.field
		"blank" == cb1.errors.fieldError.code
	}	

	void "name stores 500 characters"() {
		given:
		def m1 = new Message(name:"x"*500, trace:"x")
		cb1.addToMessages m1

		expect:
		cb1.save()
	}

	void "trace can't be null"() {
		given:
		def m1 = new Message(name:"x", trace:null)
		cb1.addToMessages m1

		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"messages[0].trace" == cb1.errors.fieldError.field
		"nullable" == cb1.errors.fieldError.code
	}

	void "trace can't be blank"() {
		given:
		def m1 = new Message(name:"x", trace:"")
		cb1.addToMessages m1

		expect:
		!cb1.save()

		and:
		1 == cb1.errors.errorCount
		"messages[0].trace" == cb1.errors.fieldError.field
		"blank" == cb1.errors.fieldError.code
	}

	void "trace stores 50,000 characters"() {
		given:
		def m1 = new Message(name:"x", trace:"x"*50_000)
		cb1.addToMessages m1

		expect:
		cb1.save()
	}

	void "trace is trimmed to 50,000 chars"() {
		given:
		def m1 = new Message(name:"x", trace:"x"*51_000)
		cb1.addToMessages m1

		when:
		m1.beforeValidate()
		
		then:
		50_000 == m1.trace.size()
	}

}