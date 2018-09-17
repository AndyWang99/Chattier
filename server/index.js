var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);

var players = [];

server.listen(8080, function() {
	console.log("Server is now running...");
});

io.on('connection', function(socket) {
	console.log("Player Connected!")
	// give the new player their socket id
	socket.emit("givePlayerID", { id: socket.id });
	socket.on('disconnect', function() {
	    // when a player disconnects, search for their id, and send their name to the client
        var disconnectedName = "";
        for (var i = 0; i < players.length; i++) {
            if (socket.id == players[i].id) {
                disconnectedName = players[i].name;
                players.splice(i, 1);
                break;
            }
        }
	    socket.broadcast.emit('playerLeft', { name: disconnectedName });
		console.log("Player Disconnected");
	});

    // when the new player joins, they give the server their name. the server stores their name and id together
	socket.on('newPlayerName', function(data) {
	    var newPlayer = new player(socket.id, data);
	    players.push(newPlayer);
	    socket.broadcast.emit('newPlayerJoined', data);
	});

	socket.on('updatePlayerName', function(data) {
         for (var i = 0; i < players.length; i++) {
            if (socket.id == players[i].id) {
                var oldName = players[i].name;
                players[i].name = data;
                var nameSwapped = new nameChange(oldName, data);
                socket.broadcast.emit('broadcastNewName', nameSwapped);
            }
        }
	});
});

function player(id, name) {
    this.id = id;
    this.name = name;
}

function nameChange(oldName, newName) {
    this.oldName = oldName;
    this.newName = newName;
}