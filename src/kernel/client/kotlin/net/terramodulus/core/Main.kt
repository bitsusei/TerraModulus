/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.core

import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec
import joptsimple.ValueConversionException
import joptsimple.ValueConverter
import net.terramodulus.common.core.ApplicationArgumentParsingError
import net.terramodulus.common.core.ApplicationInitializationFault
import net.terramodulus.common.core.run
import net.terramodulus.common.core.setupInit
import net.terramodulus.util.exception.CodeLogicFault
import net.terramodulus.util.exception.triggerGlobalCrash
import java.awt.Dimension
import java.nio.file.InvalidPathException
import java.nio.file.Path

fun main(args: Array<String>) {
	setupInit()
	try {
		parseArgs(args)
	} catch (e: ApplicationArgumentParsingError) {
		triggerGlobalCrash(ApplicationInitializationFault(e))
	}

	run(TerraModulus())
}

/**
 * Note that help message is not implemented here since it is not used and
 * should not be accessible normally.
 * Some features are then not used due to this, including but not limited to:
 * - [ValueConverter.valuePattern]
 * - [joptsimple.HelpFormatter]
 * - [OptionParser.printHelpOn]
 *
 * @throws ApplicationArgumentParsingError
 */
fun parseArgs(args: Array<String>) {
	val parser = OptionParser()
	val options = ArgumentOptions(parser)
	@Suppress("NAME_SHADOWING")
	val args = try {
		Arguments(options, parser.parse(*args))
	} catch (e: OptionException) {
		throw ApplicationArgumentParsingError(e)
	} catch (e: ClassCastException) {
		triggerGlobalCrash(CodeLogicFault(e))
	}
}

private object PathConverter : ValueConverter<Path> {
	override fun convert(value: String): Path = try {
		Path.of(value)
	} catch (e: InvalidPathException) {
		throw ValueConversionException("Invalid path", e)
	}

	override fun valueType(): Class<Path> = Path::class.java

	override fun valuePattern(): String? = null
}

internal class ArgumentOptions(parser: OptionParser) {
	val fullscreen: OptionSpec<Void> = parser.accepts("fullscreen")
	val screenSize: OptionSpec<Dimension> = parser.accepts("screen-size")
		.withRequiredArg()
		.withValuesConvertedBy(object : ValueConverter<Dimension> {
			private val REGEX = Regex("^([1-9][0-9]*)x([1-9][0-9]*)$")

			override fun convert(value: String): Dimension {
				val result = REGEX.find(value)
				if (result == null) {
					throw ValueConversionException("Invalid value (pattern unmatched): $value")
				} else {
					fun parseInt(value: String): Int {
						try {
							return value.toInt()
						} catch (e: NumberFormatException) {
							throw ValueConversionException("Invalid value: $value", e)
						}
					}

					try {
						return Dimension(parseInt(result.groups[1]!!.value), parseInt(result.groups[2]!!.value))
					} catch (e: NullPointerException) {
						triggerGlobalCrash(CodeLogicFault(e))
					}
				}
			}

			override fun valueType() = Dimension::class.java

			override fun valuePattern() = null
		})
// 	val dataDir: OptionSpec<Path> = parser.accepts("data-dir")
// 		.withRequiredArg()
// 		.required()
// 		.withValuesConvertedBy(PathConverter)
// 	val resources: OptionSpec<Path> = parser.accepts("resources")
// 		.withRequiredArg()
// 		.required()
// 		.withValuesConvertedBy(PathConverter)
}

/**
 * @throws OptionException
 * @throws ClassCastException regarded as code logic problems
 */
class Arguments internal constructor(options: ArgumentOptions, args: OptionSet) {
	val fullscreen = args.has(options.fullscreen)
	val screenSize: Dimension? = args.valueOf(options.screenSize)
// 	val dataDir: Path = args.valueOf(options.dataDir)
// 	val resources: Path = args.valueOf(options.resources)
}
