package com.campuscompanion.api.util

/** Thrown when a request body fails validation. Mapped to HTTP 400 by StatusPagesConfig. */
class BadRequestException(message: String) : RuntimeException(message)

/** Thrown when a request has no/invalid auth credentials. Mapped to HTTP 401. */
class UnauthorizedException(message: String = "Missing or invalid authentication token") : RuntimeException(message)

/** Thrown when an authenticated user tries to act on a resource they don't own. Mapped to HTTP 403. */
class ForbiddenException(message: String = "You do not have permission to perform this action") : RuntimeException(message)

/** Thrown when a requested resource does not exist. Mapped to HTTP 404. */
class NotFoundException(message: String) : RuntimeException(message)
