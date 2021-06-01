#!/usr/bin/env groovy

import java.io.*

import java.security.MessageDigest

import java.nio.file.Files

import hudson.FilePath;
import java.nio.file.Path;
import java.nio.file.Paths

import groovy.io.FileType

import java.nio.file.StandardCopyOption

/*

This script copies files or folders by checksum

File will be copied only in source and destination files are different

See more in 'help' below

*/

def call(String from, String to, boolean verbose, boolean silent, boolean skipNewer) {

println "Method Called"
println ("From file: "+from)
def  filesList =  listFiles(createFilePath(from));
copyDir(from, to, verbose, silent, skipNewer)
println "Method Call Finished"	

}

def int copyDir(String from, String to, boolean verbose, boolean silent, boolean skipNewer) {

println("Copy folder by checksum: ${from}")

int success = 0

int filesCopied = 0

int filesSkipped = 0

int filesSKIP_DATE = 0

int filesSKIP_COPY = 0

int filesCOPY_HASH = 0

int filesCOPY_NEWF = 0


def  filesList =  listFiles(createFilePath(from));

println("Files in source folder : ${filesList.size()}")

int count = 0
int size = filesList.size()

def retString =  (copyOneFile(file, destFilePath)) ? "SUCCESS" : "FAILED")

println(" Summary : " +

"\n Files in source folder : ${filesList.size()}" +

"\n Files copied ${filesCopied}" +

"\n Files skipped ${filesSkipped}" +

"\n Reasons : SKIP_DATE = ${filesSKIP_DATE}, SKIP_COPY = ${filesSKIP_COPY}, COPY_HASH = ${filesCOPY_HASH}, COPY_NEWF = ${filesCOPY_NEWF}", silent)

return retString

}

def takeCareOnOneFile(int count, int filesNumber, String file, String destFilePath, boolean verbose, boolean silent, boolean skipNewer) {

println ("We are inside takeCareOnOneFile")
//printLine("File [${count}/${filesNumber}] : COPY_NEWF : ${file}", silent)
def retString =  (copyOneFile(file, destFilePath)) ? "COPY_NEWF" : "FAILED"
println ("We are inside retString: ${retString}")
return retString
}

def copyOneFile(String source, String target) {
println "We are inside copyOneFile ${source} || ${target}"
try{
FilePath sourceFile = createFilePath(source)
FilePath targetDir = createFilePath(target);
if(!targetDir.exists()){
	targetDir.mkdirs()
}
println("Moving all children to target dir")
//targetDir.chmod(0777)

sourceFile.copyRecursiveTo(targetDir);

println("Done to copy ${target}")

//} catch (Exception e) {

//println("Error. Failed to copy file ${source}. \n ${e}")

//return false

//}

return true

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



static void returnExitCode(int error) {

System.exit(error)

}
@NonCPS
def createFilePath(path) {
    if (env['NODE_NAME'] == null) {
        error "envvar NODE_NAME is not set, probably not inside an node {} or running an older version of Jenkins!";
    } else if (env['NODE_NAME'].equals("master")) {
        return new FilePath(path);
    } else {
        return new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), path);
    }
}

@NonCPS
def List<String> listFiles(rootPath) {
    print "Files in ${rootPath}:";
    List<String> filesList = new ArrayList<String>();
    for (subPath in rootPath.list()) {
        println "Files found: ${subPath.getName()}";
        filesList.add("${subPath.getName()}");
    }
    return filesList
}

