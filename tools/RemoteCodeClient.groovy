final PORT = 6666

class ClientException extends Exception {
	public ClientException(String message) {
		super(message)
	}
}

if (args == null || args.length == 0) {
	System.err.println "Usage: groovy RemoteCodeClient.groovy path/to/script.groovy"
	System.exit(1)
} else if (args[0].endsWith("RemoteCodeServer.groovy") ||
		   args[0].endsWith("RemoteCodeClient.groovy")) {
	System.err.println "Please do not try to run RemoteCodeServer or RemoteCodeClient using RemoteCodeClient!"
	System.err.println "Groovyception is a very bad idea!"
	System.exit(1)
}

Socket client
try {
	try {
		client = new Socket(InetAddress.loopbackAddress, PORT)
	} catch (ConnectException e) {
		throw new ClientException("Unable to connect to localhost:${PORT}\n"+
								  "Please ensure RemoteCodeServer is running in QuPath")
	}
	
	def request = new PrintStream(client.outputStream)
	def response = new BufferedReader(new InputStreamReader(client.inputStream))
	
	def status = response.readLine()
	if (status == null)
		throw new ClientException("Unable to communicate with remote server\n"+
								  "This can happen when you have started SSH forwarding,\n"+
								  "but nothing is listening to port ${PORT} on the remote")
	assert status == "WELCOME"
	
	if (args[0] == "--kill") {
		request.println "KILL"
		assert response.readLine() == "GOODBYE"
		println "Server exited properly"
	} else {
		def file = new File(args[0])
		if (!file.isFile() || !file.canRead())
			throw new ClientException("Unable to read file ${args[0]}\n"+
									  "Please make sure it exists and can be read")
		
		def codeLines = 0
		file.eachLine { codeLines++ }
		request.println "RUN ${codeLines}"
		file.eachLine { request.println it }
		assert response.readLine() == "RECIEVED"
		
		status = response.readLine().split(" (?:: )?", 2)
		if (status[0] == "SUCCESS")
			response.eachLine { println it }
		else if (status[0] == "EXECUTION-ERROR")
			System.err.println "Runtime error: ${status[1]}"
		else
			System.err.println "Unknown error (status: ${status})"
	}
} catch (ClientException e) {
	System.err.println e.getMessage()
} finally {
	if (client != null)
		client.close()
}