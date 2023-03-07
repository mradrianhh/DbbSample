import com.ibm.dbb.build.*

def COMPILER = "SYS1.IGY.V6R3M0.SIGYCOMP"
def SCEELKED = "SYS1.CEE.SCEELKED"
def SCEELKEX = "SYS1.CEE.SCEELKEX"

def HLQ = "AD07288.DBBSAMP.BUILD"
def COBSRC = "${HLQ}.COBSRC"
def COBCOPY = "${HLQ}.COBCOPY"
def JOB = "${HLQ}.JOB"
def OBJ = "${HLQ}.OBJ"
def LOAD = "${HLQ}.LOAD"
def LINKPARM = "LIST,MAP"

def ENTRYPOINT = "HELLO"


println("Running build script. . .")

def CURRENT_DIR = new File(getClass().protectionDomain.codeSource.location.path).parentFile
println("Current directory: ${CURRENT_DIR}")

def COBOL_DIR = new File(CURRENT_DIR, 'source/cbl')
println("COBOL directory: ${COBOL_DIR}")

def JCL_DIR = new File(CURRENT_DIR, 'source/jcl')
println("JCL directory: ${JCL_DIR}")

def LOG_DIR = new File(CURRENT_DIR, 'logs')
println("Log directory: ${LOG_DIR}")

// Create all PDS.
CreatePDS createPDSCmd = new CreatePDS();

println("Creating ${COBSRC}. . .")
new CreatePDS().dataset("${COBSRC}").options("cyl space(1,1) lrecl(80) dsorg(PO) recfm(F,B) dsntype(library)").create();

println("Creating ${COBCOPY}. . .")
new CreatePDS().dataset("${COBCOPY}").options("cyl space(1,1) lrecl(80) dsorg(PO) recfm(F,B) dsntype(library)").create();

println("Creating ${JOB}. . .")
new CreatePDS().dataset("${JOB}").options("cyl space(1,1) lrecl(80) dsorg(PO) recfm(F,B) dsntype(library)").create();

println("Creating ${OBJ}. . .")
new CreatePDS().dataset("${OBJ}").options("cyl space(1,1) lrecl(80) dsorg(PO) recfm(F,B) dsntype(library)").create();

println("Creating ${LOAD}. . .")
new CreatePDS().dataset("${LOAD}").options("cyl space(1,1) dsorg(PO) recfm(U) dsntype(library)").create();

// Copy all files to their appropriate PDS.
println("Copying COBOL source from zFS to PDS. . .")
COBOL_DIR.eachFile { file -> 
	
	def member = file.name.take(file.name.lastIndexOf('.'))
	println("\t--> Copying '${file}' to '${COBSRC}(${member})'.")
	new CopyToPDS().file(file).dataset(COBSRC).member(member).execute()
	
}

println("Copying JCL source from zFS to PDS. . .")
JCL_DIR.eachFile { file ->
	
	def member = file.name.take(file.name.lastIndexOf('.'))
	println("\t--> Copying '${file}' to '${JOB}(${member})'.")
	new CopyToPDS().file(file).dataset(JOB).member(member).execute()
	
}

// Compile all source.
println("Compiling COBOL source. . .")

COBOL_DIR.eachFile { file ->
	
	def member = file.name.take(file.name.lastIndexOf('.'))
	println("\t--> Compiling ${COBSRC}(${member}).")
	def logFile = new File(LOG_DIR, "${member}.log")
	println("\t--> Logging to '${logFile}'.")
	
	def compile = new MVSExec().pgm("IGYCRCTL")
	compile.dd(new DDStatement().name("SYSIN").dsn("${COBSRC}(${member})").options("shr"))
	compile.dd(new DDStatement().name("SYSLIN").dsn("${OBJ}(${member})").options("shr"))
	
	(1..17).toList().each { num ->
		compile.dd(new DDStatement().name("SYSUT$num").options("cyl space(5,5) unit(vio) new"))
		   }
	
	compile.dd(new DDStatement().name("SYSMDECK").options("cyl space(5,5) unit(vio) new"))
	compile.dd(new DDStatement().name("TASKLIB").dsn("${COMPILER}").options("shr"))
	compile.dd(new DDStatement().name("SYSPRINT").options("cyl space(5,5) unit(vio)  new"))
	compile.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile))
	
	def rc = compile.execute()
	if (rc > 4)
		println("\t--> Compile failed.	RC=$rc")
	else
		println("\t--> Compile successful.  RC=$rc")
	
}

// Link.
println("Linking. . .")
def logFile = new File(LOG_DIR, "linkstep.log")
println("\t--> Logging to '${logFile}'.")
def link = new MVSExec().pgm("IEWL").parm(LINKPARM)
link.dd(new DDStatement().name("SYSLMOD").dsn("${LOAD}(${ENTRYPOINT})").options("shr").output(true).deployType("LOAD"))
link.dd(new DDStatement().name("SYSUT1").options("cyl space(5,5) unit(vio) blksize(80) lrecl(80) recfm(f,b) new"))
link.dd(new DDStatement().name("SYSLIB").dsn(OBJ).options("shr"))
link.dd(new DDStatement().dsn(SCEELKED).options("shr"))
link.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile))

def rc = link.execute()
if (rc > 4)
	println("\t--> Linking failed.	RC=$rc")
else
	println("\t--> Linking successful.  RC=$rc")

// Run tests.