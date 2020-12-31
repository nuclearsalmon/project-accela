/**
 * This is meant to be used by the WM to store {@link net.accela.prisma.Drawable} attachment hierarchies
 * in a tree structure.<br>
 * It was designed mainly with security in mind,
 * such that {@link net.accela.prisma.Drawable}s by different plugins cannot interfere with each other.
 * Thanks to the simplicity of the design, it is both fast, powerful and easy to use.
 */
package net.accela.prisma.drawabletree;