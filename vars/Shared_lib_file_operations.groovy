#!/usr/bin/env groovy

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

def call(File file, File remoteFile, boolean verbose, boolean silent, boolean skipNewer) {

coppy(){
println "Method Called"
if(file.isFile()) {
	copyFile(file,  remoteFile,  verbose,  silent,  skipNewer)
	}
else if (file.isDirectory()){
	copyDir(from, File to, verbose, silent, skipNewer)
}
println "Method Called"	
}
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

