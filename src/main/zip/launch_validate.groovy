#!/usr/bin/env groovy
//------------------------------------------------------------------------------
// Utility methods and classes
//------------------------------------------------------------------------------

final def isWindows = (System.getProperty('os.name') =~ /(?i)windows/).find()
final def out = System.out

final def FS = File.separator
final def GROOVY_HOME = System.getenv()['GROOVY_HOME']
final def groovy = GROOVY_HOME + FS + "bin" + FS + (isWindows ? "groovy.bat" : "groovy")
final def PLUGIN_HOME = System.getenv()['PLUGIN_HOME']

final def groovyScript = PLUGIN_HOME + FS + "validate_deployment.groovy"

//------------------------------------------------------------------------------
// Load classes into classpath
//------------------------------------------------------------------------------

def classpathList = new ArrayList();
classpathList.add(PLUGIN_HOME + FS + "classes") // add library groovy classes to the classpath
def classpath = classpathList.join(File.pathSeparator)

//------------------------------------------------------------------------------
// Execute the sub-process with the Business Objects SDK in the classpath
//------------------------------------------------------------------------------

def cmdArgs = [groovy, "-cp", classpath, groovyScript, args[0], args[1]];
println cmdArgs.join(' ');
def process = cmdArgs.execute()

process.consumeProcessOutput(out, out);
process.getOutputStream().close() // close stdin

process.waitFor()

System.exit(process.exitValue())
