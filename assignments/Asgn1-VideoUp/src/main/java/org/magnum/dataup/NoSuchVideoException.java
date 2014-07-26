package org.magnum.dataup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class NoSuchVideoException extends Exception {
	private static final long serialVersionUID = -5678621037688559356L;

	public NoSuchVideoException(long id) {
	}
}
