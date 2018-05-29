package pravda.cli

import java.io.File

import pravda.cli.Config.PravdaCompile
import pravda.common.bytes
import scopt.OptionParser

object Parser extends OptionParser[Config]("pravda") {

  head("Pravda Command Line Interface")

  cmd("gen")
    .text("Useful stuff generators.")
    .children(
      cmd("address")
        .text("Generate ed25519 key pair. It can be used as regualr wallet or validator node identifier.")
        .action((_, _) => Config.GenAddress())
        .children(
          opt[File]('o', "output")
            .text("Output file")
            .action {
              case (file, Config.GenAddress(_)) =>
                Config.GenAddress(Some(file.getAbsolutePath))
              case (_, otherwise) => otherwise
            }
        )
    )

  cmd("run")
    .text("Run bytecode given from stdin or file on Pravda VM.")
    .action((_, _) => Config.RunBytecode())
    .children(
      opt[String]('e', "executor")
        .validate {
          case s if bytes.isHex(s) && s.length == 64 => Right(())
          case s                                     => Left(s"`$s` is not valid address. It should be 32 bytes hex string.")
        }
        .text("Executor address HEX representation")
        .action {
          case (address, config: Config.RunBytecode) =>
            config.copy(executor = address)
          case (_, otherwise) => otherwise
        },
      opt[File]('i', "input")
        .text("Input file")
        .action {
          case (file, config: Config.RunBytecode) =>
            config.copy(input = Some(file.getAbsolutePath))
          case (_, otherwise) => otherwise
        },
      opt[File]("storage")
        .text("Storage name")
        .action {
          case (file, config: Config.RunBytecode) =>
            config.copy(storage = Some(file.getAbsolutePath))
          case (_, otherwise) => otherwise
        }
    )

  cmd("compile")
    .action((_, _) => Config.Compile(PravdaCompile.Nope))
    .children(
      opt[File]('i', "input")
        .text("Input file")
        .action {
          case (file, config: Config.Compile) =>
            config.copy(input = Some(file.getAbsolutePath))
          case (_, otherwise) => otherwise
        },
      opt[File]('o', "output")
        .text("Output file")
        .action {
          case (file, config: Config.Compile) =>
            config.copy(output = Some(file.getAbsolutePath))
          case (_, otherwise) => otherwise
        },
      cmd("asm")
        .text("Assemble Pravda VM bytecode from text presentation.")
        .action {
          case (_, config: Config.Compile) =>
            config.copy(compiler = Config.PravdaCompile.Asm)
          case (_, otherwise) => otherwise
        },
      cmd("disasm")
        .text("Disassemble Pravda VM bytecode to text presentation.")
        .action {
          case (_, config: Config.Compile) =>
            config.copy(compiler = Config.PravdaCompile.Disasm)
          case (_, otherwise) => otherwise
        },
      cmd("forth")
        .text("Compile Pravda pseudo-forth to Pravda VM bytecode.")
        .action {
          case (_, config: Config.Compile) =>
            config.copy(compiler = Config.PravdaCompile.Forth)
          case (_, otherwise) => otherwise
        },
      cmd("dotnet")
        .text("Compile .exe produced byt .NET compiler to Pravda VM bytecode.")
        .action {
          case (_, config: Config.Compile) =>
            config.copy(compiler = Config.PravdaCompile.DotNet)
          case (_, otherwise) => otherwise
        },
    )

  override def showUsageOnError: Boolean = false
}