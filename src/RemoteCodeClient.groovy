
final PORT = 6666

// DEBUG ONLY
args = ["src/HelloWorld.groovy"]
// END DEBUG ONLY

def file = new File(args[0])
def codeLines = 0
file.eachLine { codeLines++ }

def client = new Socket(InetAddress.localHost, 6666)
def request = new PrintStream(client.outputStream)
def response = new BufferedReader(new InputStreamReader(client.inputStream))

assert response.readLine() == "WELCOME"
request.println "RUN ${codeLines}"
file.eachLine { request.println it }
assert response.readLine() == "RECIEVED"

def status = response.readLine().split(" (?:: )?", 2)
if (status[0] == "SUCCESS") {
	response.eachLine { println it }
} else if (status[0] == "EXECUTION-ERROR") {
	System.err.println "Runtime error: ${status[1]}"
} else {
	System.err.println("Unknown error (status: ${status})")
}

client.close()