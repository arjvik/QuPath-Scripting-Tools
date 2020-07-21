import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

import javax.script.ScriptContext
import javax.script.ScriptEngine
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
	def input = new BufferedReader(new InputStreamReader(connection.getInputStream()))
	def output = new PrintStream(connection.getOutputStream())
	try {
		output.println "WELCOME"
		String header = input.readLine()
		if (header == null)
			throw new ServerException("NO BODY RECIEVED")
		if (header.equals("KILL")) {
			output.println "GOODBYE"
			return false;
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
			codeOutput.append("\nRESULT: ${result.toString()}")
		def outputLines = codeOutput.toString().split("\n").length
		output.println "SUCCESS ${outputLines} LINES"
		output.println codeOutput.toString()
		output.println "END"
		println "Successfully sent response"
	} catch (ServerException e) {
		output.println "ERROR : ${e.message}"
		println "Error sending response"
	} catch (ScriptException e) {
		output.println "EXECUTION-ERROR : ${e.message}"
		println "Executed code caused exception"
	} finally {
		output.flush()
		connection.close()
	}
	return true;
}

println "Starting socket"
def server = new ServerSocket(PORT)
while (handleClient(server.accept())) {}
println "Shutting down socket"