Hi,

As I said in the description of this case , I share (paste) the code here (I cannot upload it via the url because the system blocked - bank security issue).

Tomorrow unfortunatly  I can't meet you. Let's talk on Monday May 31 at 3PM - Israeli timezone if you can.

 

import java.io.*

import java.security.MessageDigest

import java.nio.file.Files

import java.nio.file.Paths

import groovy.io.FileType

import java.nio.file.StandardCopyOption

/*

This script copies files or folders by checksum

File will be copied only in source and destination files are different

See more in 'help' below

*/

class cscopy {

static void main(String[] args) {

if ( args.size ( ) == 0 ) {

showHelp()

} else if ( args.size ( ) == 1 && args [ 0 ] in [ "h", "help" ] ) {

showHelp()

} else if ( args.size ( ) == 2 ) {

println("Flags : default")

File source = new File(args[0])

File target = new File(args[1])

if (source.isDirectory()) {

returnExitCode(copyDir(source, target, false, false, false))

} else {

returnExitCode(copyFile(source, target, false, false, false))

}

} else if ( args.size ( ) == 3 ) {

String flags = args[2]

println("Flags : '${flags}'")

boolean verbose = (flags.contains("v")) ? true : false

boolean silent = (flags.contains("s")) ? true : false

boolean skipNewer = (flags.contains("d")) ? true : false

File source = new File(args[0])

File target = new File(args[1])

if (source.isDirectory()) {

returnExitCode(copyDir(source, target, verbose, silent, skipNewer))

} else {

returnExitCode(copyFile(source, target, verbose, silent, skipNewer))

}

} else {

showHelp()

println("Bad user input")

returnExitCode(1)

}

}

static void showHelp() {

println("""

Copy file or folder by checksum ver. 07.2020

Parameters : source file or folder, target file or folder , (optional) flags

Supported flags : 

v - Verbose mode. More details in log

s - Silent mode. No log

d - Skip copy if destination file is newer than source

Example: 'c:\\temp\\33 c:\\temp\\44 sd' , 'c:\\temp\\3.txt c:\\temp\\4.txt sd'

Internal flags:

SKIP_DATE - File skipped due 'd' flag

COPY_HASH - File copied due HASH difference (file changed)

COPY_NEWF - File copied because not exists on destination

SKIP_COPY - File skipped because source and destination both same checksum

""")

}

static int copyFile(File file, File remoteFile, boolean verbose, boolean silent, boolean skipNewer) {

println("Copy file by checksum")

int success = 0

if (!file.exists()) {

println("Error. Source file not found ${file.getAbsolutePath()}")

return 2

}

if (!file.isFile()) {

println("Error. Source must be file. ${file.getAbsolutePath()}")

return 4

}

printLine(" Copy : ${file} -> ${remoteFile}", verbose)

String res = takeCareOnOneFile(1, 1, file, remoteFile, verbose, silent, skipNewer)

printLine(" Result : ${res}", verbose)

return (success == "FAILED") ? 1 : 0

}

static int copyDir(File from, File to, boolean verbose, boolean silent, boolean skipNewer) {

println("Copy folder by checksum")

int success = 0

int filesCopied = 0

int filesSkipped = 0

int filesSKIP_DATE = 0

int filesSKIP_COPY = 0

int filesCOPY_HASH = 0

int filesCOPY_NEWF = 0

if (!from.exists()) {

println("Error. Source folder not found ${from.getAbsolutePath()}")

return 2

}

if (!from.isDirectory()) {

println("Error. Source must be folder. ${from.getAbsolutePath()}")

return 4

}

Vector<File> filesList = new Vector()

from.eachFileRecurse(FileType.FILES) { File file ->

filesList.add(file)

}

println("Files in source folder : ${filesList.size()}")

int count = 0

for (File file in filesList) {

count++

File remoteFile = new File(file.getAbsolutePath().replace(from.getAbsolutePath(), to.getAbsolutePath()))

String res = takeCareOnOneFile(count, filesList.size(), file, remoteFile, verbose, silent, skipNewer)

switch (res) {

case "SKIP_DATE": filesSKIP_DATE++; filesSkipped++; break

case "SKIP_COPY": filesSKIP_COPY++; filesSkipped++; break

case "COPY_HASH": filesCOPY_HASH++; filesCopied++; break

case "COPY_NEWF": filesCOPY_NEWF++; filesCopied++; break

case "FAILED":

println("Copy failed : ${file}")

return 1

break

}

}

printLine(" Summary : " +

"\n Files in source folder : ${filesList.size()}" +

"\n Files copied ${filesCopied}" +

"\n Files skipped ${filesSkipped}" +

"\n Reasons : SKIP_DATE = ${filesSKIP_DATE}, SKIP_COPY = ${filesSKIP_COPY}, COPY_HASH = ${filesCOPY_HASH}, COPY_NEWF = ${filesCOPY_NEWF}", silent)

return success

}

static String takeCareOnOneFile(int count, int filesNumber, File file, File remoteFile, boolean verbose, boolean silent, boolean skipNewer) {

// Copy one file to destination

if (remoteFile.exists()) {

// Skip copy because remote file is never than source. (Use this only if flag passed by user)

if (skipNewer && remoteFile.lastModified() > file.lastModified()) {

printLine("File [${count}/${filesNumber}] : SKIP_DATE : ${file} ", silent)

return "SKIP_DATE"

}

String hash1 = getMD5(file)

String hash2 = getMD5(remoteFile)

if (hash1 == hash2) {

// Skip because source and destination files are same (hashes are same)

if (verbose) {

printLine("File [${count}/${filesNumber}] : SKIP_COPY : ${file} ", silent)

}

return "SKIP_COPY"

} else {

// Copy because destination is different (has different hash)

printLine("File [${count}/${filesNumber}] : COPY_HASH : ${file}", silent)

return (copyOneFile(file, remoteFile)) ? "COPY_HASH" : "FAILED"

}

} else {

// Copy because destination file is missing

printLine("File [${count}/${filesNumber}] : COPY_NEWF : ${file}", silent)

return (copyOneFile(file, remoteFile)) ? "COPY_NEWF" : "FAILED"

}

}

static void printLine(String line, boolean silent) {

if (!silent) {

println(line)

}

}

static String getMD5(File f) {

try {

byte[] b = Files.readAllBytes(Paths.get(f.getAbsolutePath()))

// byte[] hash = MessageDigest.getInstance("MD5").digest(b)

BigInteger bigInt = new BigInteger(1, MessageDigest.getInstance("MD5").digest(b))

return bigInt.toString(16).padLeft(32, '0')

} catch (Exception e) {

println("Failed to calculate MD5 for ${f}")

println(e)

returnExitCode (7)

}

}

static boolean copyOneFile(File source, File target) {

try {

File parent = new File(target.getParent())

if (!parent.exists()) {

boolean createdParent = parent.mkdirs()

}

Files.copy(Paths.get(source.getAbsolutePath()), Paths.get(target.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING)

println("Done to copy ${target}")

} catch (Exception e) {

println("Error. Failed to copy file ${source}. \n ${e}")

return false

}

return true

}

static void returnExitCode(int error) {

System.exit(error)

}

}