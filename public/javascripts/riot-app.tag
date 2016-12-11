<chat-mgs>
    <div class={ msg: true, juliet: (opts.user == 'Juliet'), others: (opts.user != opts.name) }>
        { opts.time }<br/>
        <strong>{ opts.user } says: </strong>
        { opts.text }
    </div>

</chat-mgs>


<msg-list>
    <div id="chat">
        <chat-mgs each={ opts.data } user={ this.user } time={ this.time } text={ this.text } name={ this.name } />
    </div>
</msg-list>


<name-room-box>
    <div id="header">
        Your Name: <input type="text" name="user" class="userField" value={ opts.name }
                          onChange={ opts.handle_name_change } />
        <select id="roomSelect" onChange={ opts.handle_room_change } value={ opts.room }>
            <option each={ r in [1,2,3,4,5] } value={ r }>Room { r }</option>
        </select>
    </div>
</name-room-box>

<say-something-box>
    <div id="footer">
        <form onSubmit={ handleSubmit }>
            <input type="text" id="textField" ref="text" placeholder="Say something" class="input-block-level" />
            <input type="button" class="btn btn-primary" value="Submit" onClick={ handleSubmit } />
        </form>
    </div>

    handleSubmit() {
        var msg = {
            text: $("#textField")[0].value,
            user: opts.name,
            time: (new Date()).toUTCString(),
            room: "room" + opts.room
        };
        // console.log(msg);
        $.ajax({
            url: "/chat",
            type: "POST",
            data: JSON.stringify(msg),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        });
        $("#textField")[0].value = "" // empty text field
        return false
    }
</say-something-box>

<riot-app>

    <div id="chat-app"></div>

    <div>
        <name-room-box name={ this.state.name } room={ this.state.room }
                       handle_name_change={ handleNameChange } handle_room_change={ handleRoomChange } />
        <msg-list data={ this.state.data } name={ this.state.name } />
        <say-something-box name={ this.state.name } room={ this.state.room } />
    </div>

    // randomly generate initial user name
    initialName() {
        return "John Doe #" + Math.floor((Math.random() * 100) + 1)
    }

    // creates initial application state
    this.state = {
        data: [],
        room: 1,
        name: this.initialName()
    }

    handleNameChange(event) {
        this.state.name = event.target.value         // update name state with new value in text box
    }

    handleRoomChange(event) {
        this.state.room = event.target.value         // update room state with the newly selected value
        this.listen(event.target.value)              // re-initialize SSE stream with new room
        //$("#chat").empty()
    }

    addMsg(msg) {
        // console.log(msg)
        if (msg.data == '{}') return
        data = _.extend(JSON.parse(msg.data), {name: this.state.name})
        this.state.data.push(data)   // push message into state.data array
        this.state.data = _.last(this.state.data, 4); // replace state.data with up to last 5 entries
        this.update()
    }

    this.listen = function () {
        var chatFeed                                                 // holds SSE streaming connection for chat messages for current room
        return function(room) {                                      // returns function that takes room as argument
            if (chatFeed) { chatFeed.close() }                       // if initialized, close before starting new connection
            chatFeed = new EventSource("/chatFeed/room" + room)      // (re-)initializes connection
            chatFeed.addEventListener("message", this.addMsg, false) // attach addMsg event handler
        }
    }()

    this.on('mount', function() {
        // right after the tag is mounted on the page
        this.listen(this.state.room)
    })

</riot-app>