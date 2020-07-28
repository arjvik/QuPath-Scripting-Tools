if (args == null || args.length == 0) {
	System.err.println "Usage: groovy RemoteCodeClient.groovy path/to/script.groovy"
	System.exit(1)
} else if (args[0].endsWith("RemoteCodeServer.groovy") ||
		   args[0].endsWith("RemoteCodeClient.groovy")) {
	System.err.println "Please do not try to run RemoteCodeServer or RemoteCodeClient using RemoteCodeClient!"
	System.err.println "Groovyception is a very bad idea!"
	System.exit(1)
}

final PORT = 6666

def client = new Socket(InetAddress.localHost, PORT)
try {
	def request = new PrintStream(client.outputStream)
	def response = new BufferedReader(new InputStreamReader(client.inputStream))
	
	assert response.readLine() == "WELCOME"
	
	if (args[0] == "--kill") {
		request.println "KILL"
		assert response.readLine() == "GOODBYE"
		println "Server exited properly"
	} else {
		def file = new File(args[0])
		if (!file.isFile() || !file.canRead()) {
			System.err.println "Unable to read file ${args[0]}"
			System.err.println "Please make sure it exists and can be read"
			System.exit(1)
		}
		
		def codeLines = 0
		file.eachLine { codeLines++ }
		request.println "RUN ${codeLines}"
		file.eachLine { request.println it }
		assert response.readLine() == "RECIEVED"
		
		def status = response.readLine().split(" (?:: )?", 2)
		if (status[0] == "SUCCESS")
			response.eachLine { println it }
		else if (status[0] == "EXECUTION-ERROR")
			System.err.println "Runtime error: ${status[1]}"
		else
			System.err.println "Unknown error (status: ${status})"
	}
} finally {
	client.close()
}