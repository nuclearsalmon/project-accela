/**
 * A set of different control sequences, mostly ANSI.<br><br>
 * <p>
 * A statement is sometimes but not always comprised of attributes.
 * A sequence is comprised of either a single statement or multiple statements.<br><br>
 * <p>
 * For example "^[0;1m" is an SGR ANSISequence, the "0;1" is an SGR Statement,
 * and the "0" and "1"s are SGR Attributes.
 */
package net.accela.prisma.sequence;