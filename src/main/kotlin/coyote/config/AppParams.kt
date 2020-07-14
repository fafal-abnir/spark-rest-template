package coyote.config


import com.xenomachina.argparser.ArgParser

class ArgumentParser(parser: ArgParser) {
    val setup by parser.flagging("-s", "--setup", help = "enable setup mode for creating and ")
    val ignoreSync by parser.flagging("-i", "--ignore-sync", help = "ignore database's tables synchronization and checking.")
}