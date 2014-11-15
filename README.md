isotypes
========

Isotypes ISO8583 Message Translator

Intended for integration with Apache Camel, this Java library provides a Spring XSD custom configuration for defining ISO8583 messages and the message translation utilities to create and parse messages. This library does not address the transport aspects of working with the ISO8583 protocol (e.g., sync vs. async, wire codec), but rather should be stage in a Camel route, as, for example, marshalling and unmarshalling.

Features
 * Declarative message definition via custom Spring XSD
 * Content Types supported: ASCII, EBCDIC, UTF-8, BCD (or any JVM supported encoding)
 * Automatic type conversions
 * Primary, Secondary & Tertiary Hex and Binary Bitmaps supported
 * Track1 & Track2 data supported
 * Custom Type Formatters (inc. overridden standard field formatters)
 * Field values can also be provided in name-keyed maps and bean objects
 * Message content reporting (see Output, below)
 * 85%+ unit test coverage
 * Field Value Auto-generation
 * OSGi bundled
 * Camel Integration (full camel-iso8583 component coming soon)
