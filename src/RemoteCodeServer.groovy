import javax.script.ScriptEngineManager
import javax.script.ScriptException
import javax.script.SimpleScriptContext

final PORT = 6666

class ServerException extends Exception {
	public ServerException(String message) {
		super(message)
	}
}

boolean handleClient(Socket connection) throws IOException {
	println "Connection accepted"
	def input = new BufferedReader(new InputStreamReader(connection.inputStream))
	def output = new PrintStream(connection.outputStream)
	try {
		output.println "WELCOME"
		String header = input.readLine()
		if (header == null)
			throw new ServerException("NO BODY RECIEVED")
		if (header.equals("KILL")) {
			output.println "GOODBYE"
			return false
		}
		if (!header.matches("RUN \\d+"))
			throw new ServerException("INVALID MESSAGE HEADER")
		def numLines = Integer.parseInt(header.substring(4))
		def code = new StringBuilder()
		for (int i = 0; i < numLines; i++) {
			String line = input.readLine()
			if (line == null)
				throw new ServerException("NOT ENOUGH LINES SENT")
			code.append(line)
			code.append("\n")
		}
		output.println "RECIEVED"
		def engine = new ScriptEngineManager().getEngineByExtension("groovy")
		def context = new SimpleScriptContext()
		def codeOutput = new StringWriter()
		context.setWriter(codeOutput)
		context.setErrorWriter(codeOutput)
		Object result = engine.eval(code.toString(), context)
		if (result != null)
			codeOutput.append("RESULT: ${result.toString()}")
		def outputLines = codeOutput.toString().split("\n").length
		output.println "SUCCESS ${outputLines} LINES"
		output.println codeOutput.toString()
		println "Successfully sent response"
	} catch (ServerException e) {
		output.println "BAD-REQUEST : ${e.message}"
		println "Invalid client request"
	} catch (IOException e) {
		println "Transmission error"
	} catch (ScriptException e) {
		output.println "EXECUTION-ERROR : ${e.message}"
		println "Executed code caused exception"
	} finally {
		output.flush()
		connection.close()
	}
	return true
}

println "Starting socket"
def server = new ServerSocket(PORT)
try {
	while (handleClient(server.accept())) {}
} finally {
	println "Shutting down socket"
	server.close()
}