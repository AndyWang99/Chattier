var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);

var users = [];

server.listen(8080, function() {
	console.log("Server is now running...");
});

io.on('connection', function(socket) {
	console.log("User Connected!")
	socket.on('disconnect', function() {
	    // when a user disconnects, search for their id, remove them from the list of users, and send their name to the client
        var disconnectedName = "";
        for (var i = 0; i < users.length; i++) {
            if (socket.id == users[i].id) {
                disconnectedName = users[i].name;
                users.splice(i, 1);
                break;
            }
        }

	    socket.broadcast.emit('userLeft', { name: disconnectedName });      // notify all other clients that the user has left
		console.log("User Disconnected");
	});

    // when the new user joins, they give the server their name. the server stores their name and id together
	socket.on('newUserName', function(data) {
	    var newPlayer = new user(socket.id, data);
	    users.push(newPlayer);
	    socket.broadcast.emit('newUserJoined', data);       // notify all other clients that the user has joined
	});

	socket.on('updateUserName', function(data) {
        for (var i = 0; i < users.length; i++) {
            if (socket.id == users[i].id) {                              	    // search for the user by their socket id
                var oldName = users[i].name;
                users[i].name = data;                                           // replace their old name with their new one onto the server
                var nameSwapped = new nameChange(oldName, data);
                socket.broadcast.emit('broadcastNewName', nameSwapped);         // notify all other clients that the user changed their name
            }
        }
	});

	socket.on('sendMessage', function(data) {
	    var name = "";
        for (var i = 0; i < users.length; i++) {
            if (socket.id == users[i].id) {
                name = users[i].name;                                               // find the name of the user who sent the message
            }
        }
	    socket.broadcast.emit('receiveMessage', { name: name, message: data });    // tell other users message that was sent and the user who sent it
	});
});

function user(id, name) {
    this.id = id;
    this.name = name;
}

function nameChange(oldName, newName) {
    this.oldName = oldName;
    this.newName = newName;
}