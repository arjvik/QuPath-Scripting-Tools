
final PORT = 6666

// DEBUG ONLY
args = ["src/HelloWorld.groovy"]
// END DEBUG ONLY

def client = new Socket(InetAddress.localHost, 6666)
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
		assert file.canRead()
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