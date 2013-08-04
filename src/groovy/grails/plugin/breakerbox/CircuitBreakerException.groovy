package grails.plugin.breakerbox

class CircuitBreakerException extends RuntimeException {

	CircuitBreakerException() {
		super()
	}

	CircuitBreakerException(String message) {
		super(message.toString())
	}

	// CircuitBreakerException(Throwable cause) {
	// 	super(cause)
	// }

	// CircuitBreakerException(String message, Throwable cause) {
	// 	super(message.toString(), cause)
	// }

}
