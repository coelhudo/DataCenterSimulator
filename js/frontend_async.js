var socket = null;
var isopen = false;
window.onload = function() {
    socket = new WebSocket("ws://127.0.0.1:8888");
    socket.binaryType = "arraybuffer";
    socket.onopen = function() {
        console.log("Connected!");
        isopen = true;
    }
    socket.onmessage = function(e) {
        if (typeof e.data == "string") {
            console.log("Text message received: " + e.data);
	    var jsonObject = JSON.parse(e.data)
	    $("#main").append(JSON.stringify(jsonObject));
        } else {
            var arr = new Uint8Array(e.data);
            var hex = '';
            for (var i = 0; i < arr.length; i++) {
                hex += ('00' + arr[i].toString(16)).substr(-2);
            }
            console.log("Binary message received: " + hex);
        }
    }
    socket.onclose = function(e) {
        console.log("Connection closed.");
        socket = null;
        isopen = false;
    }
};
function execute() {
    if (isopen) {
        socket.send("execute");
	console.log("Text message sent.");
    } else {
        console.log("Connection not opened.")
    }
};
