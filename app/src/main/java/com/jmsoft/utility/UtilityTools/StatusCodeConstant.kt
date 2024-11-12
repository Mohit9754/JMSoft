package com.jmsoft.basic.UtilityTools

interface StatusCodeConstant {
    companion object {
        // Note: Only the widely used HTTP status codes are documented
        // Informational
        const val CONTINUE = 100
        const val SWITCHING_PROTOCOLS = 101
        const val PROCESSING = 102 // RFC2518
        // Success
        /**
         * The request has succeeded
         */
        const val OK = 200

        /**
         * The server successfully created a new resource
         */
        const val CREATED = 201
        const val ACCEPTED = 202
        const val NON_AUTHORITATIVE_INFORMATION = 203

        /**
         * The server successfully processed the request, though no content is returned
         */
        const val NO_CONTENT = 204
        const val RESET_CONTENT = 205
        const val PARTIAL_CONTENT = 206
        const val MULTI_STATUS = 207 // RFC4918
        const val ALREADY_REPORTED = 208 // RFC5842
        const val IM_USED = 226 // RFC3229

        // Redirection
        const val MULTIPLE_CHOICES = 300
        const val MOVED_PERMANENTLY = 301
        const val FOUND = 302
        const val SEE_OTHER = 303

        /**
         * The resource has not been modified since the last request
         */
        const val NOT_MODIFIED = 304
        const val USE_PROXY = 305
        const val RESERVED = 306
        const val TEMPORARY_REDIRECT = 307
        const val PERMANENTLY_REDIRECT = 308 // RFC7238
        // Client Error
        /**
         * The request cannot be fulfilled due to multiple errors
         */
        const val BAD_REQUEST = 400

        /**
         * The user is unauthorized to access the requested resource
         */
        const val UNAUTHORIZED = 401
        const val PAYMENT_REQUIRED = 402

        /**
         * The requested resource is unavailable at this present time
         */
        const val FORBIDDEN = 403

        /**
         * The requested resource could not be found
         *
         *
         * Note: This is sometimes used to mask if there was an UNAUTHORIZED (401) or
         * FORBIDDEN (403) error, for security reasons
         */
        const val NOT_FOUND = 404

        /**
         * The request method is not supported by the following resource
         */
        const val METHOD_NOT_ALLOWED = 405

        /**
         * The request was not acceptable
         */
        const val NOT_ACCEPTABLE = 406
        const val PROXY_AUTHENTICATION_REQUIRED = 407
        const val REQUEST_TIMEOUT = 408

        /**
         * The request could not be completed due to a conflict with the current state
         * of the resource
         */
        const val CONFLICT = 409
        const val GONE = 410
        const val LENGTH_REQUIRED = 411
        const val PRECONDITION_FAILED = 412
        const val REQUEST_ENTITY_TOO_LARGE = 413
        const val REQUEST_URI_TOO_LONG = 414
        const val UNSUPPORTED_MEDIA_TYPE = 415
        const val REQUESTED_RANGE_NOT_SATISFIABLE = 416
        const val EXPECTATION_FAILED = 417
        const val I_AM_A_TEAPOT = 418 // RFC2324
        const val UNPROCESSABLE_ENTITY = 422 // RFC4918
        const val LOCKED = 423 // RFC4918
        const val FAILED_DEPENDENCY = 424 // RFC4918
        const val RESERVED_FOR_WEBDAV_ADVANCED_COLLECTIONS_EXPIRED_PROPOSAL = 425 // RFC2817
        const val UPGRADE_REQUIRED = 426 // RFC2817
        const val PRECONDITION_REQUIRED = 428 // RFC6585
        const val TOO_MANY_REQUESTS = 429 // RFC6585
        const val REQUEST_HEADER_FIELDS_TOO_LARGE = 431 // RFC6585
        // Server Error
        /**
         * The server encountered an unexpected error
         *
         *
         * Note: This is a generic error message when no specific message
         * is suitable
         */
        const val INTERNAL_SERVER_ERROR = 500

        /**
         * The server does not recognise the request method
         */
        const val NOT_IMPLEMENTED = 501
        const val BAD_GATEWAY = 502
        const val SERVICE_UNAVAILABLE = 503
        const val GATEWAY_TIMEOUT = 504
        const val VERSION_NOT_SUPPORTED = 505
        const val VARIANT_ALSO_NEGOTIATES_EXPERIMENTAL = 506 // RFC2295
        const val INSUFFICIENT_STORAGE = 507 // RFC4918
        const val LOOP_DETECTED = 508 // RFC5842
        const val NOT_EXTENDED = 510 // RFC2774
        const val NETWORK_AUTHENTICATION_REQUIRED = 511
    }
}