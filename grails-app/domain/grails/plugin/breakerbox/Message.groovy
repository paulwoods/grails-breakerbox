package grails.plugin.breakerbox

class Message {

	static final TRACE_SIZE = 50_000

	static belongsTo = [ circuitBreaker: CircuitBreaker ]

	String name
	String trace
	Date dateCreated
	Date lastUpdated
	
	static constraints = {
		name blank:false, maxSize: 1000
		trace blank:false, maxSize: TRACE_SIZE
	}

	String toString() {
		"Message[$id] $circuitBreaker?.name | $name"
	}

	def beforeValidate() {
		if(trace && trace.size() > TRACE_SIZE)
			trace = trace[0..<TRACE_SIZE]
	}
	
}
