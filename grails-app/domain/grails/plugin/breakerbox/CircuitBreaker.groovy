package grails.plugin.breakerbox

class CircuitBreaker {

	static hasMany = [ messages: Message ]

	String name
	Integer trigger = 5
	Integer count = 0
	List messages
	Date dateCreated
	Date lastUpdated

	static mapping = {
		messages sort: "dateCreated", order:"desc"
	}

	static constraints = {
		name nullable:false, blank:false, unique:true, maxSize:60
		trigger nullable:false, min:1
	}

	Integer getCount() {
		this.count
	}

	String toString() {
		"CircuitBreaker[$id] $name"
	}

	def protect(Closure closure) {
		try {
			if(!tripped) {
				closure.call()
			}
		} catch(e) {
			count++

			StringWriter errors = new StringWriter()
			e.printStackTrace new PrintWriter(errors)

			def message = new Message()
			message.name = e.message
			message.trace = errors.toString()

			addToMessages message

			save()
		}
	}

	Boolean getTripped() {
		count >= trigger
	}

	void reset() {
		count = 0
		save()
	}

	void verify() {
		if(tripped) {
			throw new CircuitBreakerException("CircuitBreaker $name tripped")
		}
	}

}
